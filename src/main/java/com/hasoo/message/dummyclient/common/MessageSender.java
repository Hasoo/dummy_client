package com.hasoo.message.dummyclient.common;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasoo.message.dummyclient.dto.SenderQue;
import com.hasoo.message.dummyclient.netty.UmgpClient;
import com.hasoo.message.dummyclient.rabbitmq.CallbackReceiveEvent;
import com.hasoo.message.dummyclient.rabbitmq.MessageConsumer;
import com.hasoo.message.dummyclient.util.Util;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageSender {
  private ObjectMapper mapper = new ObjectMapper();
  private UmgpClient umgpClient;
  private MessageConsumer messageConsumer;
  private boolean idle = true;
  private int idleTimeout = 10000;
  private boolean isRunning = true;

  @SuppressWarnings("unused")
  private MessageSender() {}

  public MessageSender(UmgpClient umgpClient, MessageConsumer messageConsumer) {
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

        if (true != messageConsumer.isAlive()) {
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

      return false;
    } catch (Exception e) {
      log.error(Util.getStackTrace(e));
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
          log.error(Util.getStackTrace(e));
          return true; // NACK for deleting
        }
        idle = false;
        return sendMessage(contentType, que);
      }
    });

    return true;
  }
}
