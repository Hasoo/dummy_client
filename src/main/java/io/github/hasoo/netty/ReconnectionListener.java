package io.github.hasoo.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

public class ReconnectionListener implements ChannelFutureListener {
  private NettyClient umgpClient;

  @SuppressWarnings("unused")
  private ReconnectionListener() {
  }

  ReconnectionListener(NettyClient umgpClient) {
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
