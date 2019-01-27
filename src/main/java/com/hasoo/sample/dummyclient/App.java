package com.hasoo.sample.dummyclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.hasoo.sample.dummyclient.netty.UmgpClient;
import com.hasoo.sample.dummyclient.rabbitmq.MessageConsumer;
import com.hasoo.sample.dummyclient.service.MessageSender;
import com.hasoo.sample.dummyclient.service.MessageSenderTask;
import com.hasoo.sample.dummyclient.umgp.UmgpMessageSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  public static void main(String[] args) {
    ExecutorService executor = Executors.newCachedThreadPool();

    UmgpClient umgpClient_1 = UmgpClient.builder().ip("127.0.0.1").port(4000).build();

    /* @formatter:off */
    MessageConsumer messageConsumer_1 = MessageConsumer.builder()
        .ip("127.0.0.1")
        .port(5672)
        .username("test")
        .password("test")
        .queueName("LGT_1")
        .build();
    /* @formatter:on */

    MessageSender messageSender_1 = new UmgpMessageSender(umgpClient_1, messageConsumer_1);
    // messageSender_1.setProperty(props);
    messageSender_1.setup();

    executor.execute(new MessageSenderTask(messageSender_1));

    try {
      TimeUnit.SECONDS.sleep(60 * 10);
    } catch (InterruptedException e) {
    }

    try {
      log.debug("attempt to shutdown executor");

      // messageSender.stop();
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("tasks interrupted");
    } finally {
      if (!executor.isTerminated()) {
        log.debug("cancel non-finished tasks");
      }
      executor.shutdownNow();
      umgpClient_1.shutdown();
      log.debug("shutdown finished");
    }
  }
}
