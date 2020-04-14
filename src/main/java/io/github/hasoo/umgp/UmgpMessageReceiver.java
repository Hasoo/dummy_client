package io.github.hasoo.umgp;

import io.github.hasoo.common.MessageReceiver;
import io.github.hasoo.common.RsltCodeType;
import io.github.hasoo.netty.NettyClient;

import java.util.HashMap;

public class UmgpMessageReceiver extends MessageReceiver {
  private final NettyClient nettyClient;

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
