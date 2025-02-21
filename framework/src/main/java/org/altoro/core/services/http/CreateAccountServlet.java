package org.altoro.core.services.http;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.altoro.core.Wallet;
import org.altoro.protos.Protocol.Transaction;
import org.altoro.protos.Protocol.Transaction.Contract.ContractType;
import org.altoro.protos.contract.AccountContract.AccountCreateContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
@Slf4j(topic = "API")
public class CreateAccountServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      PostParams params = PostParams.getPostParams(request);

      AccountCreateContract.Builder build = AccountCreateContract.newBuilder();
      JsonFormat.merge(params.getParams(), build, params.isVisible());
      Transaction tx = wallet
          .createTransactionCapsule(build.build(), ContractType.AccountCreateContract)
          .getInstance();

      JSONObject input = JSONObject.parseObject(params.getParams());
      tx = Util.setTransactionPermissionId(input, tx);
      response.getWriter().println(Util.printCreateTransaction(tx, params.isVisible()));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}