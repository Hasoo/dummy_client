package com.hasoo.dummyclient.umgp;

import java.util.HashMap;
import com.hasoo.dummyclient.common.MessageReceiver;
import com.hasoo.dummyclient.common.RsltCodeType;
import com.hasoo.dummyclient.netty.NettyClient;

public class UmgpMessageReceiver extends MessageReceiver {
  private NettyClient nettyClient;

  public UmgpMessageReceiver(NettyClient nettyClient) {
    super(nettyClient);
    this.nettyClient = nettyClient;
  }

  @Override
  public void loadConfiguration(HashMap<String, String> props) {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendPing() {
    nettyClient.sendPing("1");
  }

  @Override
  public String convertNetRslt(String code) {
    if (code.equals("100Success")) {
      return RsltCodeType.SUCC.getCode();
    } else if (code.equals("4100/Invalid")) {
      return RsltCodeType.INVA.getCode();
    } else if (code.equals("4000/Failure")) {
      return RsltCodeType.TIME.getCode();
    }

    return RsltCodeType.FAIL.getCode();
  }

}
