package org.altoro.core.services.http.solidity;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.altoro.api.GrpcAPI.BytesMessage;
import org.altoro.common.utils.ByteArray;
import org.altoro.core.Wallet;
import org.altoro.core.services.http.JsonFormat;
import org.altoro.core.services.http.RateLimiterServlet;
import org.altoro.core.services.http.Util;
import org.altoro.protos.Protocol.TransactionInfo;


@Component
@Slf4j(topic = "API")
public class GetTransactionInfoByIdSolidityServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      String input = request.getParameter("value");
      TransactionInfo transInfo = wallet.getTransactionInfoById(ByteString.copyFrom(
          ByteArray.fromHexString(input)));
      if (transInfo == null) {
        response.getWriter().println("{}");
      } else {
        response.getWriter().println(JsonFormat.printToString(transInfo, visible));
      }
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
      try {
        response.getWriter().println(e.getMessage());
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(input);
      boolean visible = Util.getVisiblePost(input);
      BytesMessage.Builder build = BytesMessage.newBuilder();
      JsonFormat.merge(input, build, visible);
      TransactionInfo transInfo = wallet.getTransactionInfoById(build.build().getValue());
      if (transInfo == null) {
        response.getWriter().println("{}");
      } else {
        response.getWriter().println(JsonFormat.printToString(transInfo, visible));
      }
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
      try {
        response.getWriter().println(e.getMessage());
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
    }
  }
}
