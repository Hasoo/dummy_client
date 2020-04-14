package io.github.hasoo.common;

import io.github.hasoo.common.dto.SenderQue;
import io.github.hasoo.netty.NettyClient;
import io.github.hasoo.util.HUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class MessageReceiver {
  private final MergeQueue mergeQueue = MergeQueue.getInstance();
  private NettyClient nettyClient;
  private boolean idle = true;
  private final int idleTimeout = 10000;
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
