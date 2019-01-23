package com.hasoo.sample.dummyclient.netty;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import com.hasoo.sample.dummyclient.util.Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UmgpClientHandler extends ChannelInboundHandlerAdapter {

  private UmgpClient umgpClient;

  @SuppressWarnings("unused")
  private UmgpClientHandler() {}

  public UmgpClientHandler(UmgpClient umgpClient) {
    this.umgpClient = umgpClient;
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    log.info("connected->{}", ctx.toString());
    super.channelRegistered(ctx);
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    log.debug(ctx.toString());
    super.channelUnregistered(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf byteBuf = (ByteBuf) msg;
    if (byteBuf.isReadable()) {
      String line = byteBuf.toString(Charset.defaultCharset());
      line = line.trim();
      log.debug(Util.dump(line));
      // umgpWorker.receive(ctx.channel(), line);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    log.info("connection closed {}", ctx.toString());
    // log.info("connection closed {}", umgpWorker.who(ctx.channel()));
    EventLoop eventLoop = ctx.channel().eventLoop();
    eventLoop.schedule(new Runnable() {

      @Override
      public void run() {
        umgpClient.setup(new Bootstrap(), eventLoop);
      }
    }, 1L, TimeUnit.SECONDS);

    super.channelInactive(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error(cause.getMessage());
  }
}
