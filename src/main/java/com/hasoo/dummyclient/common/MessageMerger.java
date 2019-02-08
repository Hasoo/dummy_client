package com.hasoo.dummyclient.common;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasoo.dummyclient.common.dto.SenderQue;
import com.hasoo.dummyclient.common.rabbitmq.MessagePublisher;
import com.hasoo.dummyclient.util.HUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageMerger {
  private ObjectMapper mapper = new ObjectMapper();
  private MergeQueue mergeQueue = MergeQueue.getInstance();
  private MessagePublisher messagePublisher;
  private int expiredTimeout = 10;
  private boolean isRunning = true;

  @SuppressWarnings("unused")
  private MessageMerger() {}

  public MessageMerger(MessagePublisher messagePublisher, int expiredTimeout) {
    this.messagePublisher = messagePublisher;
    this.expiredTimeout = expiredTimeout;
  }

  public abstract boolean save(SenderQue que);

  public abstract SenderQue find(String key);

  public abstract void findExpired(CallbackExpiredEvent event, int expiredTimeout);

  public abstract void close();

  public void stop() {
    isRunning = false;
    close();
  }

  public boolean work() {
    try {
      if (true != messagePublisher.connect()) {
        return true;
      }

      while (isRunning) {
        SenderQue que = mergeQueue.poll();
        if (null == que) {
          findExpired(new CallbackExpiredEvent() {
            @Override
            public boolean expired(SenderQue que) {
              try {
                messagePublisher.publish(mapper.writeValueAsString(byTimeout(que)));
                return true;
              } catch (JsonProcessingException e) {
                log.error(HUtil.getStackTrace(e));
              }
              return false;
            }
          }, expiredTimeout);

          try {
            TimeUnit.MILLISECONDS.sleep(10);
          } catch (InterruptedException e) {
          }

          continue;
        }

        process(que);
      }
      log.debug("exited");

      return false; // exit
    } catch (Exception e) {
      log.debug(HUtil.getStackTrace(e));
    }

    return true;
  }

  private void process(SenderQue que) {
    SenderQue storedQue = find(que.getMsgKey());
    if (null == storedQue) {
      if (null == que.getUsername()) { // throw away que because it's report.
        log.info("failed to merge {}", que.toString());
        return;
      }

      save(que); // save a sent message
      return;
    }

    try {
      SenderQue mergedQue = merge(storedQue, que);
      log.debug("mergedQue:{}", mergedQue.toString());
      messagePublisher.publish(mapper.writeValueAsString(mergedQue));
    } catch (JsonProcessingException e) {
      log.error(HUtil.getStackTrace(e));
    }
  }

  private SenderQue merge(SenderQue sentQue, SenderQue reportQue) {
    sentQue.setCode(reportQue.getCode());
    sentQue.setDesc(reportQue.getDesc());
    sentQue.setDoneDate(reportQue.getDoneDate());
    sentQue.setNet(reportQue.getNet());
    return sentQue;
  }

  private SenderQue byTimeout(SenderQue que) {
    que.setCode("7000");
    que.setDesc("Timeout");
    que.setNet("ETC");
    que.setDoneDate(new Date());
    return que;
  }
}
