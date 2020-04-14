package io.github.hasoo.umgp;

import io.github.hasoo.common.MessageSender;
import io.github.hasoo.common.dto.SenderQue;
import io.github.hasoo.common.rabbitmq.MessageConsumer;
import io.github.hasoo.netty.NettyClient;

import java.util.HashMap;

public class UmgpMessageSender extends MessageSender {
  private final NettyClient nettyClient;

  public UmgpMessageSender(NettyClient nettyClient, MessageConsumer messageConsumer) {
    super(nettyClient, messageConsumer);
    this.nettyClient = nettyClient;
  }

  @Override
  public void loadConfiguration(HashMap<String, String> props) {
    System.out.println("setup");
  }

  @Override
  public void sendPing() {
    nettyClient.sendPing("1");
  }

  @Override
  public boolean sendMessage(String contentType, SenderQue que) {
    return nettyClient.sendSMS(que.getMsgKey(), que.getPhone(), que.getCallback(),
        que.getMessage());
  }
}
