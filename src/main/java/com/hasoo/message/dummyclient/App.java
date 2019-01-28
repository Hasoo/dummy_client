package com.hasoo.message.dummyclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.hasoo.message.dummyclient.common.MessageReceiver;
import com.hasoo.message.dummyclient.common.MessageReceiverTask;
import com.hasoo.message.dummyclient.common.MessageSender;
import com.hasoo.message.dummyclient.common.MessageSenderTask;
import com.hasoo.message.dummyclient.netty.UmgpClient;
import com.hasoo.message.dummyclient.rabbitmq.MessageConsumer;
import com.hasoo.message.dummyclient.umgp.UmgpMessageReceiver;
import com.hasoo.message.dummyclient.umgp.UmgpMessageSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  private static boolean isShutdown = false;

  public static void main(String[] args) {

    // Runtime.getRuntime().addShutdownHook(new Thread() {
    // @Override
    // public void run() {
    // log.debug("shutdown hooked");
    // App.isShutdown = true;
    // }
    // });

    ExecutorService executor = Executors.newCachedThreadPool();

    UmgpClient umgpSenderClient_1 = UmgpClient.builder().ip("127.0.0.1").port(4000).username("test")
        .password("test").reportline("N").build();

    UmgpClient umgpReceiverClient_1 = UmgpClient.builder().ip("127.0.0.1").port(4000)
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

    MessageSender messageSender_1 = new UmgpMessageSender(umgpSenderClient_1, messageConsumer_1);
    MessageReceiver messageReceiver_1 = new UmgpMessageReceiver(umgpReceiverClient_1);
    // messageSender_1.setProperty(props);

    executor.execute(new MessageSenderTask(messageSender_1));
    executor.execute(new MessageReceiverTask(messageReceiver_1));

    // while (true != App.isShutdown) {
    // try {
    // TimeUnit.SECONDS.sleep(1);
    // } catch (InterruptedException e) {
    // }
    // }
    //
    // try {
    // log.debug("attempt to shutdown executor");
    // messageSender_1.stop();
    // executor.shutdown();
    // executor.awaitTermination(5, TimeUnit.SECONDS);
    // } catch (InterruptedException e) {
    // log.error("tasks interrupted");
    // } finally {
    // if (!executor.isTerminated()) {
    // log.debug("cancel non-finished tasks");
    // }
    // executor.shutdownNow();
    // umgpClient_1.shutdown();
    // log.debug("shutdown finished");
    // }
  }
}
