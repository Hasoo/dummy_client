package com.hasoo.dummyclient.common.rabbitmq;

public interface CallbackReceiveEvent {
  public boolean receive(String contentType, String message);
}
