package com.hasoo.sample.dummyclient.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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

public class UmgpClient {
  private EventLoopGroup clientGroup = new NioEventLoopGroup();
  private Channel clientChannel = null;
  private String ip;
  private int port;

  private UmgpClient(UmgpClientBuilder builder) {
    this.ip = builder.ip;
    this.port = builder.port;
  }

  public void connect() {
    setup(clientGroup);
  }

  public void shutdown() {
    clientGroup.shutdownGracefully();
  }

  public Bootstrap setup(EventLoopGroup eventLoopGroup) {

    UmgpClientHandler umgpClientHandler = new UmgpClientHandler(this);

    Bootstrap bootstrap = new Bootstrap();
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

  public boolean send(String msg) {
    if (clientChannel != null && clientChannel.isActive()) {
      clientChannel.writeAndFlush(msg);
    } else {
      return false;
    }
    return true;
  }

  public static UmgpClientBuilder builder() {
    return new UmgpClientBuilder();
  }

  public static class UmgpClientBuilder {
    private String ip;
    private int port;

    public UmgpClientBuilder ip(String ip) {
      this.ip = ip;
      return this;
    }

    public UmgpClientBuilder port(int port) {
      this.port = port;
      return this;
    }

    public UmgpClient build() {
      return new UmgpClient(this);
    }
  }
}
