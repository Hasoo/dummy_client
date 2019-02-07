package com.hasoo.dummyclient.umgp;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import com.hasoo.dummyclient.common.CallbackExpiredEvent;
import com.hasoo.dummyclient.common.MessageMerger;
import com.hasoo.dummyclient.common.dto.SenderQue;
import com.hasoo.dummyclient.common.rabbitmq.MessagePublisher;
import com.hasoo.dummyclient.util.HUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UmgpMessageMerger extends MessageMerger {
  private DB db;
  private Map<String, byte[]> mergeMap;

  public UmgpMessageMerger(MessagePublisher messagePublisher, int expiredTimeout) {
    super(messagePublisher, expiredTimeout);
    db = DBMaker.fileDB(HUtil.getFilePath("./db", "merge.db").toFile()).fileMmapEnable()
        .checksumHeaderBypass().make();
    mergeMap = db.hashMap("merge").keySerializer(Serializer.STRING)
        .valueSerializer(Serializer.BYTE_ARRAY).createOrOpen();
  }

  @Override
  public boolean save(SenderQue que) {
    try {
      mergeMap.put(que.getMsgKey(), HUtil.serializeObj(que));
      return true;
    } catch (IOException e) {
      log.error(HUtil.getStackTrace(e));
    }
    return false;
  }

  @Override
  public SenderQue find(String key) {
    try {
      byte[] ob = mergeMap.get(key);
      if (null == ob) {
        return null;
      }
      SenderQue que = (SenderQue) HUtil.deserializedObj(ob);
      mergeMap.remove(key);
      return que;
    } catch (ClassNotFoundException e) {
      log.error(HUtil.getStackTrace(e));
    } catch (IOException e) {
      log.error(HUtil.getStackTrace(e));
    }
    return null;
  }

  @Override
  public void findExpired(CallbackExpiredEvent event, int expiredTimeout) {
    SenderQue que = null;
    Date now = new Date();
    for (Map.Entry<String, byte[]> elem : mergeMap.entrySet()) {
      try {
        que = (SenderQue) HUtil.deserializedObj(elem.getValue());
        final long diff = now.getTime() - que.getResDate().getTime();
        if (diff >= expiredTimeout) {
          log.debug("expired diff:{} expiredTimeout:{} msgKey:{}", diff, expiredTimeout,
              que.getMsgKey());
          event.expired(que);
          mergeMap.remove(elem.getKey());
        }
      } catch (ClassNotFoundException e) {
        log.error(HUtil.getStackTrace(e));
      } catch (IOException e) {
        log.error(HUtil.getStackTrace(e));
      }
    }
  }

  @Override
  public void close() {
    db.close();
    log.debug("MapDB closed");
  }

}
