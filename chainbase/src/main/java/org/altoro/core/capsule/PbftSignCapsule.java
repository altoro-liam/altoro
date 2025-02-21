package org.altoro.core.capsule;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.altoro.protos.Protocol.PBFTCommitResult;

import java.util.List;

@Slf4j(topic = "pbft")
public class PbftSignCapsule implements ProtoCapsule<PBFTCommitResult> {

  @Getter
  private PBFTCommitResult pbftCommitResult;

  public PbftSignCapsule(byte[] data) {
    try {
      pbftCommitResult = PBFTCommitResult.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.error("", e);
    }
  }

  public PbftSignCapsule(ByteString data, List<ByteString> signList) {
    PBFTCommitResult.Builder builder = PBFTCommitResult.newBuilder();
    builder.setData(data).addAllSignature(signList);
    pbftCommitResult = builder.build();
  }

  @Override
  public byte[] getData() {
    return pbftCommitResult.toByteArray();
  }

  @Override
  public PBFTCommitResult getInstance() {
    return pbftCommitResult;
  }
}