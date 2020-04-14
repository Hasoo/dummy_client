package io.github.hasoo;

import io.github.hasoo.common.*;
import io.github.hasoo.common.rabbitmq.MessageConsumer;
import io.github.hasoo.common.rabbitmq.MessagePublisher;
import io.github.hasoo.netty.NettyClient;
import io.github.hasoo.umgp.H2MessageMerger;
import io.github.hasoo.umgp.UmgpMessageReceiver;
import io.github.hasoo.umgp.UmgpMessageSender;
import io.github.hasoo.util.HUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class App {
  public static void main(String[] args) throws IOException {
    final String propFilename = "./cfg/application.properties";
    Properties prop = HUtil.getProperties(propFilename);

    String umgpIp = prop.getProperty("umgp.server.ip");
    String umgpPort = prop.getProperty("umgp.server.port");
    String umgpUsername = prop.getProperty("umgp.server.username");
    String umgpPassword = prop.getProperty("umgp.server.password");
    String umgpTimeout = prop.getProperty("umgp.server.timeout");

    log.info("umgp ip:{} port:{} username:{} password:{} timeout:{}", umgpIp, umgpPort,
        umgpUsername, umgpPassword, umgpTimeout);

    String rabbitmqIp = prop.getProperty("rabbitmq.ip");
    String rabbitmqPort = prop.getProperty("rabbitmq.port");
    String rabbitmqUsername = prop.getProperty("rabbitmq.username");
    String rabbitmqPassword = prop.getProperty("rabbitmq.password");
    String rabbitmqExchange = prop.getProperty("rabbitmq.exchange_name");
    String rabbitmqRouting = prop.getProperty("rabbitmq.routing_key");
    String rabbitmqQueue = prop.getProperty("rabbitmq.queue_name");

    log.info(
        "rabbitmq ip:{} port:{} username:{} password:{} exchange_name:{} routing_key:{} queue_name:{}",
        rabbitmqIp, rabbitmqPort, rabbitmqUsername, rabbitmqPassword, rabbitmqExchange,
        rabbitmqRouting, rabbitmqQueue);

    ExecutorService executor = Executors.newCachedThreadPool();

    /* @formatter:off */
    NettyClient umgpSenderClient_1 = NettyClient.builder()
        .ip(umgpIp)
        .port(Integer.parseInt(umgpPort))
        .username(umgpUsername)
        .password(umgpPassword)
        .reportline("N")
        .build();
    /* @formatter:on */

    /* @formatter:off */
    NettyClient umgpReceiverClient_1 = NettyClient.builder()
        .ip(umgpIp)
        .port(Integer.parseInt(umgpPort))
        .username(umgpUsername)
        .password(umgpPassword)
        .reportline("Y")
        .build();
    /* @formatter:on */

    /* @formatter:off */
    MessageConsumer messageConsumer_1 = MessageConsumer.builder()
        .ip(rabbitmqIp)
        .port(Integer.parseInt(rabbitmqPort))
        .username(rabbitmqUsername)
        .password(rabbitmqPassword)
        .queueName(rabbitmqQueue)
        .build();
    /* @formatter:on */

    /* @formatter:off */
    MessagePublisher messagePublisher_1 = MessagePublisher.builder()
        .ip(rabbitmqIp)
        .port(Integer.parseInt(rabbitmqPort))
        .username(rabbitmqUsername)
        .password(rabbitmqPassword)
        .exchange(rabbitmqExchange)
        .routingKey(rabbitmqRouting)
        .build();
    /* @formatter:on */

    MessageSender messageSender_1 = new UmgpMessageSender(umgpSenderClient_1, messageConsumer_1);
    MessageReceiver messageReceiver_1 = new UmgpMessageReceiver(umgpReceiverClient_1);
    MessageMerger messageMerger_1 =
        new H2MessageMerger(messagePublisher_1, 1000 * 60 * 60 * Integer.parseInt(umgpTimeout));
    // messageSender_1.setProperty(props);

    executor.execute(new MessageSenderTask(messageSender_1));
    executor.execute(new MessageReceiverTask(messageReceiver_1));
    executor.execute(new MessageMergerTask(messageMerger_1));

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          log.debug("shutdown hooked");
          log.debug("attempt to shutdown executor");
          messageSender_1.stop();
          messageReceiver_1.stop();
          messageMerger_1.stop();
          executor.shutdown();
          executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          log.error("tasks interrupted");
        } finally {
          if (!executor.isTerminated()) {
            log.debug("cancel non-finished tasks");
          }
          executor.shutdownNow();
          umgpSenderClient_1.shutdown();
          umgpReceiverClient_1.shutdown();
          log.debug("shutdown finished");
        }
      }
    });

    while (true) {
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
      }
    }
  }
}
