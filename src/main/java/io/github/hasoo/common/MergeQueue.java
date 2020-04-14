package io.github.hasoo.common;

import io.github.hasoo.common.dto.SenderQue;

import java.util.LinkedList;
import java.util.Queue;

public class MergeQueue {
    private final Queue<SenderQue> senderQues = new LinkedList<>();

    private MergeQueue() {
    }

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
