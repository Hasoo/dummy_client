package com.hasoo.message.dummyclient.rabbitmq;

public interface CallbackReceiveEvent {
  public boolean receive(String contentType, String message);
}
