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
import org.altoro.api.GrpcAPI.IvkDecryptParameters;
import org.altoro.common.utils.ByteArray;
import org.altoro.core.Wallet;

@Component
@Slf4j(topic = "API")
public class ScanNoteByIvkServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  public static String convertOutput(GrpcAPI.DecryptNotes notes, boolean visible) {
    String resultString = JsonFormat.printToString(notes, visible);
    if (notes.getNoteTxsCount() == 0) {
      return resultString;
    } else {
      JSONObject jsonNotes = JSONObject.parseObject(resultString);
      JSONArray array = jsonNotes.getJSONArray("noteTxs");
      for (int index = 0; index < array.size(); index++) {
        JSONObject item = array.getJSONObject(index);
        item.put("index", notes.getNoteTxs(index).getIndex());
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
      IvkDecryptParameters.Builder ivkDecryptParameters = IvkDecryptParameters.newBuilder();
      JsonFormat.merge(input, ivkDecryptParameters);

      GrpcAPI.DecryptNotes notes = wallet
          .scanNoteByIvk(ivkDecryptParameters.getStartBlockIndex(),
              ivkDecryptParameters.getEndBlockIndex(),
              ivkDecryptParameters.getIvk().toByteArray());
      response.getWriter().println(convertOutput(notes, visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      long startNum = Long.parseLong(request.getParameter("start_block_index"));
      long endNum = Long.parseLong(request.getParameter("end_block_index"));
      String ivk = request.getParameter("ivk");
      boolean visible = Util.getVisible(request);

      GrpcAPI.DecryptNotes notes = wallet
          .scanNoteByIvk(startNum, endNum, ByteArray.fromHexString(ivk));
      response.getWriter().println(convertOutput(notes, visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
