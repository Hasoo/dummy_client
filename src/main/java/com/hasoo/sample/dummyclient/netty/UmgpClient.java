package com.hasoo.sample.dummyclient.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UmgpClient {
  private EventLoopGroup clientGroup = new NioEventLoopGroup();
  private Channel clientChannel;
  private String ip;
  private int port;

  public UmgpClient() {}

  public UmgpClient(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  public void run() {
    try {
      setup(new Bootstrap(), clientGroup);
      Thread.sleep(20000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      clientGroup.shutdownGracefully();
    }
  }

  public Bootstrap setup(Bootstrap bootstrap, EventLoopGroup eventLoopGroup) {

    UmgpClientHandler umgpClientHandler = new UmgpClientHandler(this);

    bootstrap.group(eventLoopGroup);
    bootstrap.channel(NioSocketChannel.class);
    bootstrap.remoteAddress(new InetSocketAddress(this.ip, this.port));
    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(SocketChannel ch) throws Exception {
        /* @formatter:off */
        ch.pipeline()
            .addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
            .addLast(new LineEncoder(LineSeparator.WINDOWS))
            .addLast(new LineBasedFrameDecoder(4096))
            .addLast(new UmgpTimeoutHandler())
            .addLast(umgpClientHandler)
            ;
        /* @formatter:on */
      }
    });
    bootstrap.option(ChannelOption.TCP_NODELAY, true);
    this.clientChannel = bootstrap.connect().addListener(new ReconnectionListener(this)).channel();
    return bootstrap;
  }

}
