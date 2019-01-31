package com.hasoo.dummyclient.umgp;

import java.util.HashMap;
import com.hasoo.dummyclient.common.MessageSender;
import com.hasoo.dummyclient.common.dto.SenderQue;
import com.hasoo.dummyclient.common.rabbitmq.MessageConsumer;
import com.hasoo.dummyclient.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

public class UmgpMessageSender extends MessageSender {
  private NettyClient nettyClient;

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
