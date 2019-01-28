package com.hasoo.message.dummyclient.common;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import com.hasoo.message.dummyclient.netty.UmgpClient;
import com.hasoo.message.dummyclient.util.Util;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageReceiver {
  private UmgpClient umgpClient;
  private boolean idle = true;
  private int idleTimeout = 10000;
  private boolean isRunning = true;

  @SuppressWarnings("unused")
  private MessageReceiver() {}

  public MessageReceiver(UmgpClient umgpClient) {
    this.umgpClient = umgpClient;
  }

  public abstract void loadConfiguration(HashMap<String, String> props);

  public abstract void sendPing();

  public void stop() {
    isRunning = false;
  }

  public void connect() {
    umgpClient.connect();
  }

  public boolean work() {
    try {
      Instant preTime = Instant.now();
      while (isRunning) {
        if (umgpClient.isReceivedReport()) {
          idle = false;
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
}
