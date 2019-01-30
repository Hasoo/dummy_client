package com.hasoo.message.dummyclient.common;

import com.hasoo.message.dummyclient.dto.SenderQue;

public interface CallbackExpiredEvent {
  public boolean expired(SenderQue que);
}
