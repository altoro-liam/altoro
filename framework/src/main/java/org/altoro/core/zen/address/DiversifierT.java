package org.altoro.core.zen.address;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.altoro.core.Constant;
import org.altoro.core.exception.ZksnarkException;
import org.altoro.keystore.Wallet;
import org.tron.common.zksnark.JLibrustzcash;

@AllArgsConstructor
public class DiversifierT {

  @Setter
  @Getter
  private byte[] data = new byte[Constant.ZC_DIVERSIFIER_SIZE];

  public DiversifierT() {
  }

  public static DiversifierT random() throws ZksnarkException {
    byte[] d;
    while (true) {
      d = Wallet.generateRandomBytes(Constant.ZC_DIVERSIFIER_SIZE);
      if (JLibrustzcash.librustzcashCheckDiversifier(d)) {
        break;
      }
    }
    return new DiversifierT(d);
  }
}
