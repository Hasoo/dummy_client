package io.github.hasoo.common;

import io.github.hasoo.common.dto.SenderQue;

public interface CallbackExpiredEvent {
    boolean expired(SenderQue que);
}
