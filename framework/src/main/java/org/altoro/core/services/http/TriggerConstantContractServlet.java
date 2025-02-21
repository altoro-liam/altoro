package org.altoro.core.services.http;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.altoro.api.GrpcAPI.Return;
import org.altoro.api.GrpcAPI.Return.response_code;
import org.altoro.api.GrpcAPI.TransactionExtention;
import org.altoro.common.utils.ByteArray;
import org.altoro.core.Wallet;
import org.altoro.core.capsule.TransactionCapsule;
import org.altoro.core.exception.ContractValidateException;
import org.altoro.protos.Protocol.Transaction;
import org.altoro.protos.Protocol.Transaction.Contract.ContractType;
import org.altoro.protos.contract.SmartContractOuterClass.TriggerSmartContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.stream.Collectors;


@Component
@Slf4j(topic = "API")
public class TriggerConstantContractServlet extends RateLimiterServlet {

  private final String functionSelector = "function_selector";

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
  }

  protected void validateParameter(String contract) {
    JSONObject jsonObject = JSONObject.parseObject(contract);
    if (!jsonObject.containsKey("owner_address")
            || StringUtil.isNullOrEmpty(jsonObject.getString("owner_address"))) {
      throw new InvalidParameterException("owner_address isn't set.");
    }
    if (!jsonObject.containsKey("contract_address")
            || StringUtil.isNullOrEmpty(jsonObject.getString("contract_address"))) {
      throw new InvalidParameterException("contract_address isn't set.");
    }
    if (!jsonObject.containsKey(functionSelector)
            || StringUtil.isNullOrEmpty(jsonObject.getString(functionSelector))) {
      throw new InvalidParameterException("function_selector isn't set.");
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    TriggerSmartContract.Builder build = TriggerSmartContract.newBuilder();
    TransactionExtention.Builder trxExtBuilder = TransactionExtention.newBuilder();
    Return.Builder retBuilder = Return.newBuilder();
    boolean visible = false;
    try {
      String contract = request.getReader().lines()
              .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(contract);
      visible = Util.getVisiblePost(contract);
      validateParameter(contract);
      JsonFormat.merge(contract, build, visible);
      JSONObject jsonObject = JSONObject.parseObject(contract);
      String selector = jsonObject.getString(functionSelector);
      String parameter = jsonObject.getString("parameter");
      String data = Util.parseMethod(selector, parameter);
      build.setData(ByteString.copyFrom(ByteArray.fromHexString(data)));
      long feeLimit = Util.getJsonLongValue(jsonObject, "fee_limit");

      TransactionCapsule trxCap = wallet
              .createTransactionCapsule(build.build(), ContractType.TriggerSmartContract);

      Transaction.Builder txBuilder = trxCap.getInstance().toBuilder();
      Transaction.raw.Builder rawBuilder = trxCap.getInstance().getRawData().toBuilder();
      rawBuilder.setFeeLimit(feeLimit);
      txBuilder.setRawData(rawBuilder);

      Transaction trx = wallet
              .triggerConstantContract(build.build(), new TransactionCapsule(txBuilder.build()),
                      trxExtBuilder,
                      retBuilder);
      trx = Util.setTransactionPermissionId(jsonObject, trx);
      trx = Util.setTransactionExtraData(jsonObject, trx, visible);
      trxExtBuilder.setTransaction(trx);
      retBuilder.setResult(true).setCode(response_code.SUCCESS);
    } catch (ContractValidateException e) {
      retBuilder.setResult(false).setCode(response_code.CONTRACT_VALIDATE_ERROR)
              .setMessage(ByteString.copyFromUtf8(e.getMessage()));
    } catch (Exception e) {
      String errString = null;
      if (e.getMessage() != null) {
        errString = e.getMessage().replaceAll("[\"]", "\'");
      }
      retBuilder.setResult(false).setCode(response_code.OTHER_ERROR)
              .setMessage(ByteString.copyFromUtf8(e.getClass() + " : " + errString));
    }
    trxExtBuilder.setResult(retBuilder);
    response.getWriter().println(Util.printTransactionExtention(trxExtBuilder.build(), visible));
  }
}