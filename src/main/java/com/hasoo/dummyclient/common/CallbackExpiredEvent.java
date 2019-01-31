package com.hasoo.dummyclient.common;

import com.hasoo.dummyclient.common.dto.SenderQue;

public interface CallbackExpiredEvent {
  public boolean expired(SenderQue que);
}
