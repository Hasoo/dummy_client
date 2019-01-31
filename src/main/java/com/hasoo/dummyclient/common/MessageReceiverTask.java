package com.hasoo.dummyclient.common;

import java.util.concurrent.TimeUnit;

public class MessageReceiverTask implements Runnable {
  private MessageReceiver messageReceiver;

  @SuppressWarnings("unused")
  private MessageReceiverTask() {}

  public MessageReceiverTask(MessageReceiver messageReceiver) {
    this.messageReceiver = messageReceiver;
  }

  @Override
  public void run() {
    messageReceiver.connect();

    while (true) {
      if (false == messageReceiver.work()) {
        break;
      }
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
      }
    }
  }

}
