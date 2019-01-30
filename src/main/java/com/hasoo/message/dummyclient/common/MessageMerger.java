package com.hasoo.message.dummyclient.common;

import java.util.LinkedList;
import java.util.Queue;
import com.hasoo.message.dummyclient.dto.SenderQue;
import com.hasoo.message.dummyclient.rabbitmq.MessagePublisher;
import com.hasoo.message.dummyclient.util.Util;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageMerger {
  private Queue<SenderQue> senderQues = new LinkedList<>();
  private MessagePublisher messagePublisher;
  private int expiredTimeout = 10;
  private boolean isRunning = true;

  @SuppressWarnings("unused")
  private MessageMerger() {}

  public MessageMerger(int expiredTimeout, MessagePublisher messagePublisher) {
    this.expiredTimeout = expiredTimeout;
    this.messagePublisher = messagePublisher;
  }

  public abstract boolean save(SenderQue que);

  public abstract SenderQue find(String key);

  public abstract void findExpired(CallbackExpiredEvent event, int expiredTimeout);

  public void stop() {
    isRunning = false;
  }

  public void work() {
    while (isRunning) {
      try {
        SenderQue que = senderQues.poll();
        if (null == que) {
          findExpired(new CallbackExpiredEvent() {
            @Override
            public boolean expired(SenderQue que) {
              messagePublisher.publish(que);
              return true;
            }
          }, expiredTimeout);

          continue;
        }

        process(que);
      } catch (Exception e) {
        log.debug(Util.getStackTrace(e));
      }
    }
  }

  private void process(SenderQue que) {
    SenderQue storedQue = find(que.getMsgKey());
    if (null == storedQue) {
      if (null != que.getCode()) { // if a key of sent message is empty, throw away report.
        return;
      }

      save(que); // save a sent message
      return;
    }

    messagePublisher.publish(merge(storedQue, que));
  }

  private SenderQue merge(SenderQue sentQue, SenderQue reportQue) {
    sentQue.setCode(reportQue.getCode());
    sentQue.setDesc(reportQue.getDesc());
    sentQue.setDoneDate(reportQue.getDoneDate());
    sentQue.setNet(reportQue.getNet());
    return sentQue;
  }
}
