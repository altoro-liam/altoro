package org.altoro.core.services.ratelimiter.adapter;


import org.altoro.core.services.ratelimiter.RuntimeData;
import org.altoro.core.services.ratelimiter.strategy.IPQpsStrategy;

public class IPQPSRateLimiterAdapter implements IRateLimiter {

  private IPQpsStrategy strategy;

  public IPQPSRateLimiterAdapter(String paramString) {
    strategy = new IPQpsStrategy(paramString);
  }

  @Override
  public boolean acquire(RuntimeData data) {
    return strategy.acquire(data.getRemoteAddr());
  }

}