package io.github.hasoo.common.rabbitmq;

public interface CallbackReceiveEvent {
    boolean receive(String contentType, String message);
}
