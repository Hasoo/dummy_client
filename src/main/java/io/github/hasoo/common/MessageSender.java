package io.github.hasoo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hasoo.common.dto.SenderQue;
import io.github.hasoo.common.rabbitmq.CallbackReceiveEvent;
import io.github.hasoo.common.rabbitmq.MessageConsumer;
import io.github.hasoo.netty.NettyClient;
import io.github.hasoo.util.HUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class MessageSender {
  private final ObjectMapper mapper = new ObjectMapper();
  private final MergeQueue mergeQueue = MergeQueue.getInstance();
  private NettyClient umgpClient;
  private MessageConsumer messageConsumer;
  private boolean idle = true;
  private final int idleTimeout = 10000;
  private boolean isRunning = true;

  @SuppressWarnings("unused")
  private MessageSender() {
  }

  public MessageSender(NettyClient umgpClient, MessageConsumer messageConsumer) {
    this.umgpClient = umgpClient;
    this.messageConsumer = messageConsumer;
  }

  public void stop() {
    isRunning = false;
  }

  public abstract void loadConfiguration(HashMap<String, String> props);

  public abstract void sendPing();

  public abstract boolean sendMessage(String contentType, SenderQue que);

  public void connect() {
    umgpClient.connect();
  }

  public boolean work() {
    try {
      if (true != startConsumer()) {
        return true;
      }

      Instant preTime = Instant.now();
      while (isRunning) {

        if (messageConsumer.isRetry()) {
          return true;
        }

        if (idle) {
          Instant curTime = Instant.now();
          long gap = ChronoUnit.MILLIS.between(preTime, curTime);
          if (idleTimeout <= gap) {
            sendPing();
            preTime = curTime;
          }
        } else {
          idle = true;
          preTime = Instant.now();
        }
        try {
          TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
        }
      }
      log.debug("exited");

      return false; // exit
    } catch (Exception e) {
      log.error(HUtil.getStackTrace(e));
    }

    return true;
  }

  private boolean startConsumer() throws IOException {
    if (true != messageConsumer.connect()) {
      return false;
    }

    messageConsumer.consume(new CallbackReceiveEvent() {
      @Override
      public boolean receive(String contentType, String message) {
        SenderQue que = null;
        try {
          que = mapper.readValue(message, SenderQue.class);
        } catch (IOException e) {
          log.error(HUtil.getStackTrace(e));
          return true; // NACK for deleting
        }
        idle = false;
        if (sendMessage(contentType, que)) {
          que.setSentDate(new Date());
          mergeQueue.push(que);
          return true;
        }
        return false;
      }
    });

    return true;
  }
}
