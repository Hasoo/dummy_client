package com.hasoo.sample.dummyclient.service;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import com.hasoo.sample.dummyclient.rabbitmq.CallbackReceiveEvent;
import com.hasoo.sample.dummyclient.rabbitmq.MessageConsumer;

public abstract class MessageSender {
  private MessageConsumer messageConsumer;
  private HashMap<String, String> props;
  private boolean idle = true;
  private int idleTimeout = 10000;

  public MessageSender(MessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  public void setProperty(HashMap<String, String> props) {
    this.props = props;
  }

  public abstract void setup();

  public abstract void sendPing();

  public abstract boolean send(String contentType, String contentEncoding, String message);

  public void work() throws InterruptedException, IOException {
    setup();

    messageConsumer.connect();
    messageConsumer.consume(new CallbackReceiveEvent() {

      @Override
      public boolean receive(String contentType, String contentEncoding, String message) {
        return send(contentType, contentEncoding, message);
      }
    });

    Instant preTime = Instant.now();
    while (true) {

      if (idle) {
        Instant curTime = Instant.now();
        long gap = ChronoUnit.MILLIS.between(preTime, curTime);
        if (idleTimeout <= gap) {
          sendPing();
          preTime = curTime;
        }
      }

      Thread.sleep(100);
    }
  }
}
