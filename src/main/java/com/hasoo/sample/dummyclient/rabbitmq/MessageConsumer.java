package com.hasoo.sample.dummyclient.rabbitmq;

import java.io.IOException;
import java.net.ConnectException;
import com.hasoo.sample.dummyclient.util.Util;
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
  private String exchangeName;
  private String routingKey;
  private String queueName;

  private ConnectionFactory connectionFactory = new ConnectionFactory();
  private Channel channel = null;

  private MessageConsumer(MessageConsumerBuilder builder) {
    this.ip = builder.ip;
    this.port = builder.port;
    this.username = builder.username;
    this.password = builder.password;
    this.exchangeName = builder.exchangeName;
    this.routingKey = builder.routingKey;
    this.queueName = builder.queueName;

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
      return true;
    } catch (ConnectException e) {
      log.error(e.getMessage());
    } catch (Exception e) {
      log.error(Util.getStackTrace(e));
    }
    return false;
  }

  public void consume(CallbackReceiveEvent event) throws IOException {
    // boolean autoAck = false;
    boolean autoAck = true;
    channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
          AMQP.BasicProperties properties, byte[] body) throws IOException {
        boolean isSuccess = event.receive(properties.getContentType(),
            properties.getContentEncoding(), new String(body, properties.getContentEncoding()));
        if (isSuccess) {
          channel.basicAck(envelope.getDeliveryTag(), false);
        }
      }
    });
  }

  public static MessageConsumerBuilder builder() {
    return new MessageConsumerBuilder();
  }

  public static class MessageConsumerBuilder {
    private String ip;
    private int port;
    private String username;
    private String password;
    private String exchangeName;
    private String routingKey;
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

    public MessageConsumerBuilder exchangeName(String exchangeName) {
      this.exchangeName = exchangeName;
      return this;
    }

    public MessageConsumerBuilder routingKey(String routingKey) {
      this.routingKey = routingKey;
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
