package org.altoro.core.services.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.altoro.api.GrpcAPI;
import org.altoro.api.GrpcAPI.IvkDecryptTRC20Parameters;
import org.altoro.common.utils.ByteArray;
import org.altoro.core.Wallet;

@Component
@Slf4j(topic = "API")
public class ScanShieldedTRC20NotesByIvkServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  public static String convertOutput(GrpcAPI.DecryptNotesTRC20 notes, boolean visible) {
    String resultString = JsonFormat.printToString(notes, visible);
    if (notes.getNoteTxsCount() == 0) {
      return resultString;
    } else {
      JSONObject jsonNotes = JSONObject.parseObject(resultString);
      JSONArray array = jsonNotes.getJSONArray("noteTxs");
      for (int index = 0; index < array.size(); index++) {
        JSONObject item = array.getJSONObject(index);
        item.put("index", notes.getNoteTxs(index).getIndex()); //避免把0自动忽略
      }
      return jsonNotes.toJSONString();
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(input);

      boolean visible = Util.getVisiblePost(input);
      IvkDecryptTRC20Parameters.Builder ivkDecryptTRC20Parameters = IvkDecryptTRC20Parameters
          .newBuilder();
      JsonFormat.merge(input, ivkDecryptTRC20Parameters, visible);

      GrpcAPI.DecryptNotesTRC20 notes = wallet
          .scanShieldedTRC20NotesByIvk(ivkDecryptTRC20Parameters.getStartBlockIndex(),
              ivkDecryptTRC20Parameters.getEndBlockIndex(),
              ivkDecryptTRC20Parameters.getShieldedTRC20ContractAddress().toByteArray(),
              ivkDecryptTRC20Parameters.getIvk().toByteArray(),
              ivkDecryptTRC20Parameters.getAk().toByteArray(),
              ivkDecryptTRC20Parameters.getNk().toByteArray(),
              ivkDecryptTRC20Parameters.getEventsList());
      response.getWriter().println(convertOutput(notes, visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      long startNum = Long.parseLong(request.getParameter("start_block_index"));
      long endNum = Long.parseLong(request.getParameter("end_block_index"));
      String ivk = request.getParameter("ivk");

      String contractAddress = request.getParameter("shielded_TRC20_contract_address");
      if (visible) {
        contractAddress = Util.getHexAddress(contractAddress);
      }

      String ak = request.getParameter("ak");
      String nk = request.getParameter("nk");

      GrpcAPI.DecryptNotesTRC20 notes = wallet
          .scanShieldedTRC20NotesByIvk(startNum, endNum,
              ByteArray.fromHexString(contractAddress), ByteArray.fromHexString(ivk),
              ByteArray.fromHexString(ak), ByteArray.fromHexString(nk), null);
      response.getWriter().println(convertOutput(notes, visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
