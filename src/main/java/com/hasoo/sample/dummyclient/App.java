package com.hasoo.sample.dummyclient;

import com.hasoo.sample.dummyclient.netty.UmgpClient;
import com.hasoo.sample.dummyclient.util.Util;
import lombok.extern.slf4j.Slf4j;

/**
 * Hello world!
 *
 */
@Slf4j
public class App {
  public static void main(String[] args) {
    try {
      UmgpClient umgpClient = new UmgpClient("127.0.0.1", 4000);
      umgpClient.run();
    } catch (Exception e) {
      log.error(Util.getStackTrace(e));
    }
  }
}
