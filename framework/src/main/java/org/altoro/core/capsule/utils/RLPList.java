package org.altoro.core.capsule.utils;

import org.altoro.common.utils.ByteArray;

import java.util.ArrayList;

/**
 */
public class RLPList extends ArrayList<RLPElement> implements RLPElement {

  private byte[] rlpData;

  public static void recursivePrint(RLPElement element) {

    if (element == null) {
      throw new RuntimeException("RLPElement object can't be null");
    }
    if (element instanceof RLPList) {

      RLPList rlpList = (RLPList) element;
      System.out.print("[");
      for (RLPElement singleElement : rlpList) {
        recursivePrint(singleElement);
      }
      System.out.print("]");
    } else {
      String hex = ByteArray.toHexString(element.getRLPData());
      System.out.print(hex + ", ");
    }
  }

  public byte[] getRLPData() {
    return rlpData;
  }

  public void setRLPData(byte[] rlpData) {
    this.rlpData = rlpData;
  }
}
