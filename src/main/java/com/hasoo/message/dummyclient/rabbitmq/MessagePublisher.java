package com.hasoo.message.dummyclient.rabbitmq;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import com.hasoo.message.dummyclient.dto.SenderQue;
import com.hasoo.message.dummyclient.util.Util;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
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
  private Channel channel = null;

  private boolean alive = false;

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
      Connection connection = connectionFactory.newConnection();
      this.channel = connection.createChannel();
      alive = true;
      return true;
    } catch (ConnectException e) {
      log.error(e.getMessage());
    } catch (Exception e) {
      log.error(Util.getStackTrace(e));
    }
    return false;
  }

  public boolean publish(SenderQue que) {
    Map<String, Object> headers = new HashMap<String, Object>();

    BasicProperties props = new AMQP.BasicProperties.Builder().headers(headers)
        .contentEncoding("UTF-8").contentType("application/json").build();
    channel.basicPublish(exchange, routingKey, props, body);
    return true;
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
