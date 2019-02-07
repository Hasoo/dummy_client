package com.hasoo.dummyclient.common.rabbitmq;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Optional;
import com.hasoo.dummyclient.util.HUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageConsumer {
  private String ip;
  private int port;
  private String username;
  private String password;
  private String queueName;

  private ConnectionFactory connectionFactory = new ConnectionFactory();
  private Connection connection = null;
  private Channel channel = null;

  private boolean retry = false;

  private MessageConsumer(MessageConsumerBuilder builder) {
    this.ip = builder.ip;
    this.port = builder.port;
    this.username = builder.username;
    this.password = builder.password;
    this.queueName = builder.queueName;

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

  public boolean isRetry() {
    return retry;
  }

  public void consume(CallbackReceiveEvent event) throws IOException {
    boolean autoAck = false;
    channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
          AMQP.BasicProperties properties, byte[] body) throws IOException {

        boolean isSuccess = event.receive(properties.getContentType(),
            new String(body, Optional.ofNullable(properties.getContentEncoding()).orElse("UTF-8")));
        if (isSuccess) {
          channel.basicAck(envelope.getDeliveryTag(), false);
        } else {
          retry = true;
          disconnect();
        }
      }
    });
  }

  private void disconnect() {
    try {
      if (null != this.connection) {
        this.connection.close();
        this.connection = null;
        this.channel = null;
      }
    } catch (IOException e) {
      log.error(HUtil.getStackTrace(e));
    }
  }

  public static MessageConsumerBuilder builder() {
    return new MessageConsumerBuilder();
  }

  public static class MessageConsumerBuilder {
    private String ip;
    private int port;
    private String username;
    private String password;
    private String queueName;

    public MessageConsumerBuilder ip(String ip) {
      this.ip = ip;
      return this;
    }

    public MessageConsumerBuilder port(int port) {
      this.port = port;
      return this;
    }

    public MessageConsumerBuilder username(String username) {
      this.username = username;
      return this;
    }

    public MessageConsumerBuilder password(String password) {
      this.password = password;
      return this;
    }

    public MessageConsumerBuilder queueName(String queueName) {
      this.queueName = queueName;
      return this;
    }

    public MessageConsumer build() {
      return new MessageConsumer(this);
    }
  }
}
