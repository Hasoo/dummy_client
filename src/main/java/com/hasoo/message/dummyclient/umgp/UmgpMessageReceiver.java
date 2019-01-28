package com.hasoo.message.dummyclient.umgp;

import java.util.HashMap;
import com.hasoo.message.dummyclient.common.MessageReceiver;
import com.hasoo.message.dummyclient.netty.UmgpClient;

public class UmgpMessageReceiver extends MessageReceiver {
  private UmgpClient umgpClient;

  public UmgpMessageReceiver(UmgpClient umgpClient) {
    super(umgpClient);
    this.umgpClient = umgpClient;
  }

  @Override
  public void loadConfiguration(HashMap<String, String> props) {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendPing() {
    umgpClient.sendPing("1");
  }

}
