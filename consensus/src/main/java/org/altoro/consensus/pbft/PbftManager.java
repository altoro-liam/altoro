package org.altoro.consensus.pbft;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.altoro.consensus.base.Param;
import org.altoro.consensus.base.Param.Miner;
import org.altoro.consensus.dpos.MaintenanceManager;
import org.altoro.consensus.pbft.message.PbftBaseMessage;
import org.altoro.consensus.pbft.message.PbftMessage;
import org.altoro.core.ChainBaseManager;
import org.altoro.core.capsule.BlockCapsule;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j(topic = "pbft")
@Component
public class PbftManager {

  @Autowired
  private org.altoro.consensus.pbft.PbftMessageHandle pbftMessageHandle;

  @Autowired
  private MaintenanceManager maintenanceManager;

  @Autowired
  private ChainBaseManager chainBaseManager;

  private ExecutorService executorService = Executors.newFixedThreadPool(10,
      r -> new Thread(r, "Pbft"));

  @PostConstruct
  public void init() {
    maintenanceManager.setPbftManager(this);
    pbftMessageHandle.setMaintenanceManager(maintenanceManager);
  }

  public void blockPrePrepare(BlockCapsule block, long epoch) {
    if (!chainBaseManager.getDynamicPropertiesStore().allowPBFT()) {
      return;
    }
    if (!pbftMessageHandle.isSyncing()) {
      if (Param.getInstance().isEnable()) {
        for (Miner miner : pbftMessageHandle.getSrMinerList()) {
          doAction(PbftMessage.prePrepareBlockMsg(block, epoch, miner));
        }
      } else {
        doAction(PbftMessage.fullNodePrePrepareBlockMsg(block, epoch));
      }
    }
  }

  public void srPrePrepare(BlockCapsule block, List<ByteString> currentWitness, long epoch) {
    if (!chainBaseManager.getDynamicPropertiesStore().allowPBFT()) {
      return;
    }
    if (!pbftMessageHandle.isSyncing()) {
      if (Param.getInstance().isEnable()) {
        for (Miner miner : pbftMessageHandle.getSrMinerList()) {
          doAction(PbftMessage.prePrepareSRLMsg(block, currentWitness, epoch, miner));
        }
      } else {
        doAction(PbftMessage.fullNodePrePrepareSRLMsg(block, currentWitness, epoch));
      }
    }
  }

  public void forwardMessage(PbftBaseMessage message) {
    pbftMessageHandle.forwardMessage(message);
  }

  public boolean doAction(PbftMessage msg) {
    executorService.submit(() -> {
      logger.info("receive pbft msg: {}", msg);
      switch (msg.getPbftMessage().getRawData().getMsgType()) {
        case PREPREPARE:
          pbftMessageHandle.onPrePrepare(msg);
          break;
        case PREPARE:
          // prepare
          pbftMessageHandle.onPrepare(msg);
          break;
        case COMMIT:
          // commit
          pbftMessageHandle.onCommit(msg);
          break;
        case REQUEST:
          pbftMessageHandle.onRequestData(msg);
          break;
        case VIEW_CHANGE:
          pbftMessageHandle.onChangeView(msg);
          break;
        default:
          break;
      }
    });
    return true;
  }

  public boolean verifyMsg(PbftBaseMessage msg) {
    long epoch = msg.getPbftMessage().getRawData().getEpoch();
    List<ByteString> witnessList;
    if (epoch > maintenanceManager.getBeforeMaintenanceTime()) {
      witnessList = maintenanceManager.getCurrentWitness();
    } else {
      witnessList = maintenanceManager.getBeforeWitness();
    }
    return witnessList.contains(ByteString.copyFrom(msg.getPublicKey()));
  }

}