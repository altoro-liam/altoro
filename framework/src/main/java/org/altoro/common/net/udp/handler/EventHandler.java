package org.altoro.common.net.udp.handler;

public interface EventHandler {

  void channelActivated();

  void handleEvent(UdpEvent event);

}
