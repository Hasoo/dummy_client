package com.hasoo.dummyclient.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeoutHandler extends ChannelDuplexHandler {

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cause instanceof ReadTimeoutException) {
      log.info("timeout");
    } else {
      super.exceptionCaught(ctx, cause);
    }
  }
}
