package com.hasoo.dummyclient.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import com.hasoo.dummyclient.common.MessageReceiver;
import com.hasoo.dummyclient.common.dto.SenderQue;
import com.hasoo.dummyclient.umgp.Umgp;
import com.hasoo.dummyclient.util.HUtil;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClient {
  private EventLoopGroup clientGroup = new NioEventLoopGroup();
  private Channel clientChannel = null;
  private MessageReceiver messageReceiver = null;
  private Umgp umgp = new Umgp();

  private String ip;
  private int port;
  private String username;
  private String password;
  private String reportline;

  private Umgp.HType headerType;
  private boolean authenticated = false;
  private boolean receivedReport = false;

  private NettyClient(UmgpClientBuilder builder) {
    this.ip = builder.ip;
    this.port = builder.port;
    this.username = builder.username;
    this.password = builder.password;
    this.reportline = builder.reportline;
  }

  public void setMessageReceiver(MessageReceiver messageReceiver) {
    this.messageReceiver = messageReceiver;
  }

  public void connect() {
    setup(clientGroup);
  }

  public void shutdown() {
    clientGroup.shutdownGracefully();
  }

  public Bootstrap setup(EventLoopGroup eventLoopGroup) {

    ClientHandler umgpClientHandler = new ClientHandler(this);

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
            .addLast(new TimeoutHandler())
            .addLast(umgpClientHandler)
            ;
        /* @formatter:on */
      }
    });
    bootstrap.option(ChannelOption.TCP_NODELAY, true);

    this.clientChannel = bootstrap.connect().addListener(new ReconnectionListener(this)).channel();
    return bootstrap;
  }

  public void receive(Channel channel, String buf) {
    try {
      if (true != umgp.isCompletedBegin()) {
        this.headerType = umgp.parseHeaderPart(buf);
      } else {
        umgp.parseDataPart(buf);
      }

      if (umgp.isCompletedEnd()) {
        if (Umgp.HType.ACK == this.headerType) {
          if (true != authenticated) {
            log.debug("<- {} code:{} data:{}", Umgp.ACK, umgp.getCode(),
                umgp.getData().toString().replace('\n', '\\'));
            if (true != umgp.getCode().equals("100")) {
              channel.close();
              authenticated = false;
            } else {
              authenticated = true;
            }
          } else {
            log.debug("<- {} key:{} code:{} data:{}", Umgp.ACK, umgp.getKey(), umgp.getCode(),
                umgp.getData().toString().replace('\n', '\\'));
          }
        } else if (Umgp.HType.REPORT == headerType) {
          log.debug("<- {} key:{} code:{} data:{} date:{} net:{}", Umgp.REPORT, umgp.getKey(),
              umgp.getCode(), umgp.getData().toString(), umgp.getDate(), umgp.getNet());
          /* @formatter:off */
          String netRslt = umgp.getCode()+umgp.getData().toString();
          String rsltCode = messageReceiver.convertNetRslt(netRslt);
          messageReceiver.receiveReport(
              SenderQue.builder()
                .msgKey(umgp.getKey())
                .code(rsltCode)
                .desc(netRslt)
                .doneDate(HUtil.getDate12(umgp.getDate()))
                .net(umgp.getNet())
                .build());
          /* @formatter:off */
          receivedReport = true;
          sendAck(umgp.getKey(), "100", "Success");
        } else if (Umgp.HType.PONG == headerType) {
          log.debug("<- {} key:{}", Umgp.PONG, umgp.getKey());
        } else {
          throw new RuntimeException(String.format("invalid packet -> %s", headerType));
        }

        umgp.reset();
      }
    } catch (RuntimeException ex) {
      log.error(ex.getMessage());
      channel.close();
    }
  }

  public void sendConnect() {
    log.debug("-> {} username:{} password:{} reportline:{} version:{}", Umgp.CONNECT, username,
        password, reportline, "UMGP/1.0");
    StringBuilder packet = new StringBuilder();
    packet.append(Umgp.headerPart(Umgp.CONNECT));
    packet.append(Umgp.dataPart(Umgp.ID, username));
    packet.append(Umgp.dataPart(Umgp.PASSWORD, password));
    packet.append(Umgp.dataPart(Umgp.REPORTLINE, reportline));
    packet.append(Umgp.dataPart(Umgp.VERSION, "UMGP/1.0"));
    packet.append(Umgp.end());

    send(packet.toString());
  }

  public boolean sendSMS(String key, String phone, String callback, String message) {
    log.debug("-> {} key:{} phone:{} callback:{} message{}", Umgp.SEND, key, phone, callback,
        message.replace('\n', '\\'));
    StringBuilder packet = new StringBuilder();
    packet.append(Umgp.headerPart(Umgp.SEND));
    packet.append(Umgp.dataPart(Umgp.KEY, key));
    packet.append(Umgp.dataPart(Umgp.RECEIVERNUM, phone));
    packet.append(Umgp.dataPart(Umgp.CALLBACK, callback));

    String[] lines = message.split("\\n");
    if (1 < lines.length) {
      for (String line : lines) {
        packet.append(Umgp.dataPart(Umgp.DATA, line));
      }
    } else {
      packet.append(Umgp.dataPart(Umgp.DATA, message));
    }

    packet.append(Umgp.end());

    return send(packet.toString());
  }

  public void sendAck(String key, String code, String data) {
    log.debug("-> {} key:{} code:{} data:{}", Umgp.ACK, key, code, data);
    StringBuilder packet = new StringBuilder();
    packet.append(Umgp.headerPart(Umgp.ACK));
    packet.append(Umgp.dataPart(Umgp.KEY, key));
    packet.append(Umgp.dataPart(Umgp.CODE, code));
    packet.append(Umgp.dataPart(Umgp.DATA, data));
    packet.append(Umgp.end());

    send(packet.toString());
  }

  public void sendPing(String key) {
    log.debug("-> {} key:{}", Umgp.PING, key);
    StringBuilder packet = new StringBuilder();
    packet.append(Umgp.headerPart(Umgp.PING));
    packet.append(Umgp.dataPart(Umgp.KEY, key));
    packet.append(Umgp.end());

    send(packet.toString());
  }

  public boolean isReceivedReport() {
    if (receivedReport) {
      receivedReport = false;
      return true;
    }
    return false;
  }

  private boolean send(String msg) {
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
    private String username;
    private String password;
    private String reportline;

    public UmgpClientBuilder ip(String ip) {
      this.ip = ip;
      return this;
    }

    public UmgpClientBuilder port(int port) {
      this.port = port;
      return this;
    }

    public UmgpClientBuilder username(String username) {
      this.username = username;
      return this;
    }

    public UmgpClientBuilder password(String password) {
      this.password = password;
      return this;
    }

    public UmgpClientBuilder reportline(String reportline) {
      this.reportline = reportline;
      return this;
    }

    public NettyClient build() {
      return new NettyClient(this);
    }
  }
}
