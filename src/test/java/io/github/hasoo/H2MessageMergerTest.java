package io.github.hasoo;

import io.github.hasoo.common.CallbackExpiredEvent;
import io.github.hasoo.common.MessageMerger;
import io.github.hasoo.common.dto.SenderQue;
import io.github.hasoo.common.rabbitmq.MessagePublisher;
import io.github.hasoo.umgp.H2MessageMerger;
import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class H2MessageMergerTest {
  @BeforeEach
  public void setUp() {
    DeleteDbFiles.execute("./db", "test", true);
  }

  @AfterEach
  public void setDown() {}

  // @Test
  public void testMessageMerger() {
    int expiredTimeout = 1_000; // millisecond

    MessageMerger messageMerger =
        new H2MessageMerger(MessagePublisher.builder().build(), expiredTimeout);

    String msgKey1 = "1'", msgKey2 = "2";
    Date resDate = new Date(new Date().getTime() - expiredTimeout);
    messageMerger.save(SenderQue.builder().msgKey(msgKey1).resDate(resDate).build());
    messageMerger.save(SenderQue.builder().msgKey(msgKey2).resDate(new Date()).build());

    messageMerger.findExpired(new CallbackExpiredEvent() {

      @Override
      public boolean expired(SenderQue que) {
        Assertions.assertEquals(msgKey1, que.getMsgKey());
        return false;
      }
    }, 1000);

    Assertions.assertEquals(null, messageMerger.find(msgKey1));
    Assertions.assertEquals(msgKey2, messageMerger.find(msgKey2).getMsgKey());
  }

}
