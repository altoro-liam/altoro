package org.altoro.core.db;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.altoro.core.db.Manager;
import org.altoro.core.capsule.TransactionCapsule;
import org.altoro.core.config.args.Args;
import org.altoro.core.db.TransactionTrace.TimeResultType;
import org.altoro.core.metrics.MetricsKey;
import org.altoro.core.metrics.MetricsUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "DB")
public class PendingManager implements AutoCloseable {

  @Getter
  private List<TransactionCapsule> tmpTransactions = new ArrayList<>();
  private Manager dbManager;
  private long timeout = Args.getInstance().getPendingTransactionTimeout();

  public PendingManager(Manager db) {
    this.dbManager = db;
    db.getPendingTransactions().forEach(transactionCapsule -> {
      if (System.currentTimeMillis() - transactionCapsule.getTime() < timeout) {
        tmpTransactions.add(transactionCapsule);
      }
    });

    if (db.getPendingTransactions().size() > tmpTransactions.size()) {
      MetricsUtil.meterMark(MetricsKey.BLOCKCHAIN_MISSED_TRANSACTION,
              db.getPendingTransactions().size() - tmpTransactions.size());
    }

    db.getPendingTransactions().clear();
    db.getSession().reset();
    db.getShieldedTransInPendingCounts().set(0);
  }

  @Override
  public void close() {

    for (TransactionCapsule tx : tmpTransactions) {
      txIteration(tx);
    }
    tmpTransactions.clear();

    for (TransactionCapsule tx : dbManager.getPoppedTransactions()) {
      txIteration(tx);
    }
    dbManager.getPoppedTransactions().clear();
  }

  private void txIteration(TransactionCapsule tx) {
    try {
      if (tx.getTrxTrace() != null
              && tx.getTrxTrace().getTimeResultType().equals(TimeResultType.NORMAL)) {
        dbManager.getRePushTransactions().put(tx);
      }
    } catch (InterruptedException e) {
      logger.error(e.getMessage());
      Thread.currentThread().interrupt();
    }
  }
}
