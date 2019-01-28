package com.hasoo.message.dummyclient.common;

import java.util.concurrent.TimeUnit;

public class MessageSenderTask implements Runnable {
  private MessageSender messageSender;

  @SuppressWarnings("unused")
  private MessageSenderTask() {}

  public MessageSenderTask(MessageSender messageSender) {
    this.messageSender = messageSender;
  }

  @Override
  public void run() {
    messageSender.connect();

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
