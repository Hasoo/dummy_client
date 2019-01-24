package com.hasoo.sample.dummyclient.netty;

import java.util.concurrent.TimeUnit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

public class ReconnectionListener implements ChannelFutureListener {
  private UmgpClient umgpClient;

  @SuppressWarnings("unused")
  private ReconnectionListener() {}

  ReconnectionListener(UmgpClient umgpClient) {
    this.umgpClient = umgpClient;
  }

  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    if (true != future.isSuccess()) {
      EventLoop eventLoop = future.channel().eventLoop();
      eventLoop.schedule(new Runnable() {

        @Override
        public void run() {
          future.channel().close();
          umgpClient.setup(eventLoop);
        }
      }, 1L, TimeUnit.SECONDS);
    }
  }
}
