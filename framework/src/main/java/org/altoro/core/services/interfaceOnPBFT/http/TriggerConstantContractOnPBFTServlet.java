package org.altoro.core.services.interfaceOnPBFT.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.altoro.core.services.http.TriggerConstantContractServlet;
import org.altoro.core.services.interfaceOnPBFT.WalletOnPBFT;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
@Slf4j(topic = "API")
public class TriggerConstantContractOnPBFTServlet extends TriggerConstantContractServlet {

  @Autowired
  private WalletOnPBFT walletOnPBFT;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    walletOnPBFT.futureGet(() -> super.doGet(request, response));
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    walletOnPBFT.futureGet(() -> {
      try {
        super.doPost(request, response);
      } catch (IOException e) {
        logger.error("TriggerConstantContractOnPBFTServlet Exception", e);
      }
    });
  }
}
