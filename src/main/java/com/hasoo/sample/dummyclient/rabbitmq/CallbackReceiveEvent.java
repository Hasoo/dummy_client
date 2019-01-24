package com.hasoo.sample.dummyclient.rabbitmq;

public interface CallbackReceiveEvent {
  public boolean receive(String contentType, String contentEncoding, String message);
}
