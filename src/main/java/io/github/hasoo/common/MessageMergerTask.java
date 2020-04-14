package io.github.hasoo.common;

import java.util.concurrent.TimeUnit;

public class MessageMergerTask implements Runnable {
  private MessageMerger messageMerger;

  @SuppressWarnings("unused")
  private MessageMergerTask() {}

  public MessageMergerTask(MessageMerger messageMerger) {
    this.messageMerger = messageMerger;
  }

  @Override
  public void run() {
    while (true) {
      if (false == messageMerger.work()) {
        break;
      }
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
      }
    }
  }

}
