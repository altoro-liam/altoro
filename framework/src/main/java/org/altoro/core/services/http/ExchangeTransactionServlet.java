package org.altoro.core.services.http;

import com.alibaba.fastjson.JSONObject;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.altoro.core.Wallet;
import org.altoro.protos.Protocol.Transaction;
import org.altoro.protos.Protocol.Transaction.Contract.ContractType;
import org.altoro.protos.contract.ExchangeContract.ExchangeTransactionContract;


@Component
@Slf4j(topic = "API")
public class ExchangeTransactionServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String contract = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(contract);
      boolean visible = Util.getVisiblePost(contract);
      ExchangeTransactionContract.Builder build = ExchangeTransactionContract.newBuilder();
      JsonFormat.merge(contract, build, visible);
      Transaction tx = wallet
          .createTransactionCapsule(build.build(), ContractType.ExchangeTransactionContract)
          .getInstance();
      JSONObject jsonObject = JSONObject.parseObject(contract);
      tx = Util.setTransactionPermissionId(jsonObject, tx);
      response.getWriter().println(Util.printCreateTransaction(tx, visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}