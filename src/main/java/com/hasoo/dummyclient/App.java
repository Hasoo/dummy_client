package com.hasoo.dummyclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.hasoo.dummyclient.common.MessageMerger;
import com.hasoo.dummyclient.common.MessageMergerTask;
import com.hasoo.dummyclient.common.MessageReceiver;
import com.hasoo.dummyclient.common.MessageReceiverTask;
import com.hasoo.dummyclient.common.MessageSender;
import com.hasoo.dummyclient.common.MessageSenderTask;
import com.hasoo.dummyclient.common.rabbitmq.MessageConsumer;
import com.hasoo.dummyclient.common.rabbitmq.MessagePublisher;
import com.hasoo.dummyclient.netty.NettyClient;
import com.hasoo.dummyclient.umgp.UmgpMessageMerger;
import com.hasoo.dummyclient.umgp.UmgpMessageReceiver;
import com.hasoo.dummyclient.umgp.UmgpMessageSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  public static void main(String[] args) {
    ExecutorService executor = Executors.newCachedThreadPool();

    NettyClient umgpSenderClient_1 = NettyClient.builder().ip("127.0.0.1").port(4000)
        .username("test").password("test").reportline("N").build();

    NettyClient umgpReceiverClient_1 = NettyClient.builder().ip("127.0.0.1").port(4000)
        .username("test").password("test").reportline("Y").build();

    /* @formatter:off */
    MessageConsumer messageConsumer_1 = MessageConsumer.builder()
        .ip("127.0.0.1")
        .port(5672)
        .username("test")
        .password("test")
        .queueName("LGT_1")
        .build();
    /* @formatter:on */

    /* @formatter:off */
    MessagePublisher messagePublisher_1 = MessagePublisher.builder()
        .ip("127.0.0.1")
        .port(5672)
        .username("test")
        .password("test")
        .exchange("amq.direct")
        .routingKey("router")
        .build();
    /* @formatter:on */

    MessageSender messageSender_1 = new UmgpMessageSender(umgpSenderClient_1, messageConsumer_1);
    MessageReceiver messageReceiver_1 = new UmgpMessageReceiver(umgpReceiverClient_1);
    MessageMerger messageMerger_1 = new UmgpMessageMerger(messagePublisher_1, 1000 * 60 * 60 * 80);
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
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
      }
    }
  }
}
