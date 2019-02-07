package com.hasoo.dummyclient.common;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import com.hasoo.dummyclient.common.dto.SenderQue;
import com.hasoo.dummyclient.netty.NettyClient;
import com.hasoo.dummyclient.util.HUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageReceiver {
  private MergeQueue mergeQueue = MergeQueue.getInstance();
  private NettyClient nettyClient;
  private boolean idle = true;
  private int idleTimeout = 10000;
  private boolean isRunning = true;

  @SuppressWarnings("unused")
  private MessageReceiver() {}

  public MessageReceiver(NettyClient nettyClient) {
    this.nettyClient = nettyClient;
    this.nettyClient.setMessageReceiver(this);
  }

  public abstract void loadConfiguration(HashMap<String, String> props);

  public abstract void sendPing();

  public abstract String convertNetRslt(String code);

  public void stop() {
    isRunning = false;
  }

  public void connect() {
    nettyClient.connect();
  }

  public boolean work() {
    try {
      Instant preTime = Instant.now();
      while (isRunning) {
        if (nettyClient.isReceivedReport()) {
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
      log.debug("exited");

      return false;
    } catch (Exception e) {
      log.error(HUtil.getStackTrace(e));
    }

    return true;
  }

  public boolean receiveReport(SenderQue que) {
    mergeQueue.push(que);
    return true;
  }
}
