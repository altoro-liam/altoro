package org.altoro.core.services.http;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.altoro.core.Wallet;
import org.altoro.protos.contract.ShieldContract.IncrementalMerkleVoucherInfo;
import org.altoro.protos.contract.ShieldContract.OutputPointInfo;


@Component
@Slf4j(topic = "API")
public class GetMerkleTreeVoucherInfoServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(input);
      boolean visible = Util.getVisiblePost(input);
      OutputPointInfo.Builder build = OutputPointInfo.newBuilder();
      JsonFormat.merge(input, build);
      IncrementalMerkleVoucherInfo reply = wallet.getMerkleTreeVoucherInfo(build.build());
      if (reply != null) {
        response.getWriter().println(JsonFormat.printToString(reply, visible));
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
