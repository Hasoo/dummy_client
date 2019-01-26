package com.hasoo.sample.dummyclient.service;

import java.util.concurrent.TimeUnit;
import com.hasoo.sample.dummyclient.netty.UmgpClient;

public class MessageSenderTask implements Runnable {
  private UmgpClient umgpClient;
  private MessageSender messageSender;

  @SuppressWarnings("unused")
  private MessageSenderTask() {}

  public MessageSenderTask(UmgpClient umgpClient, MessageSender messageSender) {
    this.umgpClient = umgpClient;
    this.messageSender = messageSender;
  }

  @Override
  public void run() {
    umgpClient.connect();

    while (true) {
      if (false == messageSender.work()) {
        break;
      }
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
      }
    }
  }

}
