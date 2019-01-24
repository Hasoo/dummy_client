package com.hasoo.sample.dummyclient;

import com.hasoo.sample.dummyclient.netty.UmgpClient;
import com.hasoo.sample.dummyclient.rabbitmq.CallbackReceiveEvent;
import com.hasoo.sample.dummyclient.rabbitmq.MessageConsumer;
import com.hasoo.sample.dummyclient.util.Util;
import lombok.extern.slf4j.Slf4j;

/**
 * Hello world!
 *
 */
@Slf4j
public class App {
  public static void main(String[] args) {
    // UmgpClient umgpSenderClient = new UmgpClient("127.0.0.1", 4000);
    // UmgpClient umgpReceiverClient = new UmgpClient("127.0.0.1", 4000);
    MessageConsumer messageConsumer =
        MessageConsumer.builder().ip("127.0.0.1").port(5672).username("test").password("test")
            .exchangeName("amq.direct").routingKey("batch").queueName("test").build();
    try {
      // umgpSenderClient.connect();
      // umgpReceiverClient.connect();
      while (true != messageConsumer.connect()) {
        Thread.sleep(1000);
      }
      messageConsumer.consume(new CallbackReceiveEvent() {

        @Override
        public boolean receive(String contentType, String contentEncoding, String message) {
          log.debug("content_type:{} content_encoding:{} messae:{}", contentType, contentEncoding,
              message);
          return true;
        }
      });
      Thread.sleep(100000);
    } catch (Exception e) {
      log.error(Util.getStackTrace(e));
    } finally {
      // umgpSenderClient.shutdown();
      // umgpReceiverClient.shutdown();
    }
  }
}
