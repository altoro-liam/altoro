package org.altoro.core.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.altoro.core.capsule.MarketAccountOrderCapsule;
import org.altoro.core.db.TronStoreWithRevoking;
import org.altoro.core.exception.ItemNotFoundException;

@Component
public class MarketAccountStore extends TronStoreWithRevoking<MarketAccountOrderCapsule> {

  @Autowired
  protected MarketAccountStore(@Value("market_account") String dbName) {
    super(dbName);
  }

  @Override
  public MarketAccountOrderCapsule get(byte[] key) throws ItemNotFoundException {
    byte[] value = revokingDB.get(key);
    return new MarketAccountOrderCapsule(value);
  }

}