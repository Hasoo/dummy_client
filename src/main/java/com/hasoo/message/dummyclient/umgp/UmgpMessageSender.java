package com.hasoo.message.dummyclient.umgp;

import java.util.HashMap;
import com.hasoo.message.dummyclient.common.MessageSender;
import com.hasoo.message.dummyclient.dto.SenderQue;
import com.hasoo.message.dummyclient.netty.UmgpClient;
import com.hasoo.message.dummyclient.rabbitmq.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

public class UmgpMessageSender extends MessageSender {
  private UmgpClient umgpClient;

  public UmgpMessageSender(UmgpClient umgpClient, MessageConsumer messageConsumer) {
    super(umgpClient, messageConsumer);
    this.umgpClient = umgpClient;
  }

  @Override
  public void loadConfiguration(HashMap<String, String> props) {
    System.out.println("setup");
  }

  @Override
  public void sendPing() {
    umgpClient.sendPing("1");
  }

  @Override
  public boolean sendMessage(String contentType, SenderQue que) {
    return umgpClient.sendSMS(que.getMsgKey(), que.getPhone(), que.getCallback(), que.getMessage());
  }
}
