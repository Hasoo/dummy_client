package com.hasoo.dummyclient.common.rabbitmq;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import com.hasoo.dummyclient.util.HUtil;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessagePublisher {
  private String ip;
  private int port;
  private String username;
  private String password;
  private String exchange;
  private String routingKey;

  private ConnectionFactory connectionFactory = new ConnectionFactory();
  private Connection connection = null;
  private Channel channel = null;

  private boolean retry = false;

  private MessagePublisher(MessagePublisherBuilder builder) {
    this.ip = builder.ip;
    this.port = builder.port;
    this.username = builder.username;
    this.password = builder.password;
    this.exchange = builder.exchange;
    this.routingKey = builder.routingKey;

    connectionFactory.setHost(ip);
    connectionFactory.setPort(port);
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
    connectionFactory.setAutomaticRecoveryEnabled(true);
  }

  public boolean connect() {
    try {
      disconnect();
      this.connection = connectionFactory.newConnection();
      this.channel = connection.createChannel();
      this.retry = false;
      return true;
    } catch (ConnectException e) {
      log.error(e.getMessage());
    } catch (Exception e) {
      log.error(HUtil.getStackTrace(e));
    }
    return false;
  }

  public boolean publish(String json) {
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("__TypeId__", "com.iheart.message.ircs.queue.SenderQue");

    BasicProperties props = new BasicProperties.Builder().headers(headers).contentEncoding("UTF-8")
        .contentType("application/json").build();
    try {
      channel.basicPublish(exchange, routingKey, props, json.getBytes());
    } catch (IOException e) {
      log.error(HUtil.getStackTrace(e));
    }
    return true;
  }

  public boolean isRetry() {
    return retry;
  }

  private void disconnect() {
    try {
      if (null != this.connection) {
        this.connection.close();
        this.channel = null;
        this.connection = null;
      }
    } catch (IOException e) {
      log.error(HUtil.getStackTrace(e));
    }
  }

  public static MessagePublisherBuilder builder() {
    return new MessagePublisherBuilder();
  }

  public static class MessagePublisherBuilder {
    private String ip;
    private int port;
    private String username;
    private String password;
    private String exchange;
    private String routingKey;

    public MessagePublisherBuilder ip(String ip) {
      this.ip = ip;
      return this;
    }

    public MessagePublisherBuilder port(int port) {
      this.port = port;
      return this;
    }

    public MessagePublisherBuilder username(String username) {
      this.username = username;
      return this;
    }

    public MessagePublisherBuilder password(String password) {
      this.password = password;
      return this;
    }

    public MessagePublisherBuilder exchange(String exchange) {
      this.exchange = exchange;
      return this;
    }

    public MessagePublisherBuilder routingKey(String routingKey) {
      this.routingKey = routingKey;
      return this;
    }

    public MessagePublisher build() {
      return new MessagePublisher(this);
    }
  }
}
