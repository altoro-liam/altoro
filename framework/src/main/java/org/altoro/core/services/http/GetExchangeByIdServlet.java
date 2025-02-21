package org.altoro.core.services.http;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.altoro.common.utils.ByteArray;
import org.altoro.core.Wallet;


@Component
@Slf4j(topic = "API")
public class GetExchangeByIdServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(input);
      boolean visible = Util.getVisiblePost(input);
      JSONObject jsonObject = JSONObject.parseObject(input);
      long id = Util.getJsonLongValue(jsonObject, "id", true);
      response.getWriter()
          .println(JsonFormat
              .printToString(wallet.getExchangeById(ByteString.copyFrom(ByteArray.fromLong(id))),
                  visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      String input = request.getParameter("id");
      response.getWriter()
          .println(JsonFormat.printToString(wallet
                  .getExchangeById(ByteString.copyFrom(ByteArray.fromLong(Long.parseLong(input)))),
              visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}