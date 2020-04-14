package io.github.hasoo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {

  private NettyClient umgpClient;

  @SuppressWarnings("unused")
  private ClientHandler() {
  }

  public ClientHandler(NettyClient umgpClient) {
    this.umgpClient = umgpClient;
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    log.debug(ctx.toString());
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
    try {
      if (byteBuf.isReadable()) {
        String line = byteBuf.toString(Charset.defaultCharset());
        line = line.trim();
        // log.debug(Util.dump(line));
        umgpClient.receive(ctx.channel(), line);
      }
    } finally {
      byteBuf.release();
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("connection established {}", ctx.toString());
    umgpClient.sendConnect();
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    log.info("connection closed {}", ctx.toString());
    // log.info("connection closed {}", umgpWorker.who(ctx.channel()));
    EventLoop eventLoop = ctx.channel().eventLoop();
    eventLoop.schedule(new Runnable() {

      @Override
      public void run() {
        umgpClient.setup(eventLoop);
      }
    }, 1L, TimeUnit.SECONDS);

    super.channelInactive(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error(cause.getMessage());
  }
}
