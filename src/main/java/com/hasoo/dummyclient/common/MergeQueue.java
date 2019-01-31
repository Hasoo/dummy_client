package com.hasoo.dummyclient.common;

import java.util.LinkedList;
import java.util.Queue;
import com.hasoo.dummyclient.common.dto.SenderQue;

public class MergeQueue {
  private Queue<SenderQue> senderQues = new LinkedList<>();

  private MergeQueue() {}

  private static class SingletonHelper {
    private static final MergeQueue INSTANCE = new MergeQueue();
  }

  public static MergeQueue getInstance() {
    return SingletonHelper.INSTANCE;
  }

  public synchronized SenderQue poll() {
    return senderQues.poll();
  }

  public synchronized void push(SenderQue que) {
    senderQues.add(que);
  }
}
