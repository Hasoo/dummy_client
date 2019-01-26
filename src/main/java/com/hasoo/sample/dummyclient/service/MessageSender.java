package com.hasoo.sample.dummyclient.service;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasoo.sample.dummyclient.dto.SenderQue;
import com.hasoo.sample.dummyclient.rabbitmq.CallbackReceiveEvent;
import com.hasoo.sample.dummyclient.rabbitmq.MessageConsumer;
import com.hasoo.sample.dummyclient.util.Util;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageSender {
  private ObjectMapper mapper = new ObjectMapper();
  private MessageConsumer messageConsumer;
  private HashMap<String, String> props;
  private boolean idle = true;
  private int idleTimeout = 10000;
  private boolean isRunning = true;

  @SuppressWarnings("unused")
  private MessageSender() {}

  public MessageSender(MessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  public void setProperty(HashMap<String, String> props) {
    this.props = props;
  }

  public void stop() {
    isRunning = false;
  }

  public abstract void setup();

  public abstract void sendPing();

  public abstract boolean send(String contentType, String contentEncoding, SenderQue que);

  public boolean work() {
    try {
      setup();

      if (true != startConsumer()) {
        return true;
      }

      Instant preTime = Instant.now();
      while (isRunning) {
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
        Thread.sleep(100);
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
      public boolean receive(String contentType, String contentEncoding, String message) {
        SenderQue que = null;
        try {
          que = mapper.readValue(message, SenderQue.class);
        } catch (IOException e) {
          log.error(Util.getStackTrace(e));
        }
        idle = false;
        return send(contentType, contentEncoding, que);
      }
    });

    return true;
  }
}
