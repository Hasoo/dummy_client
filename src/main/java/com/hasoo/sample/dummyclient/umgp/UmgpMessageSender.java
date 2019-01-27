package com.hasoo.sample.dummyclient.umgp;

import com.hasoo.sample.dummyclient.dto.SenderQue;
import com.hasoo.sample.dummyclient.netty.UmgpClient;
import com.hasoo.sample.dummyclient.rabbitmq.MessageConsumer;
import com.hasoo.sample.dummyclient.service.MessageSender;

public class UmgpMessageSender extends MessageSender {
  private UmgpClient umgpClient;

  public UmgpMessageSender(UmgpClient umgpClient, MessageConsumer messageConsumer) {
    super(umgpClient, messageConsumer);
    this.umgpClient = umgpClient;
  }

  @Override
  public void setup() {}

  @Override
  public void sendPing() {
    StringBuilder packet = new StringBuilder();
    packet.append(Umgp.headerPart(Umgp.PING));
    packet.append(Umgp.dataPart(Umgp.KEY, "1"));
    packet.append(Umgp.end());
    this.umgpClient.send(packet.toString());
  }

  @Override
  public boolean sendMessage(String contentType, String contentEncoding, SenderQue que) {
    return this.umgpClient.send("test");
  }

}
