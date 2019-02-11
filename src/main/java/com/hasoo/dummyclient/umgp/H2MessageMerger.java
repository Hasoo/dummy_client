package com.hasoo.dummyclient.umgp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.hasoo.dummyclient.common.CallbackExpiredEvent;
import com.hasoo.dummyclient.common.MessageMerger;
import com.hasoo.dummyclient.common.dto.SenderQue;
import com.hasoo.dummyclient.common.rabbitmq.MessagePublisher;
import com.hasoo.dummyclient.util.HUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class H2MessageMerger extends MessageMerger {
  private Connection connection = null;

  private static final String DB_DRIVER = "org.h2.Driver";
  private static final String DB_CONNECTION = "jdbc:h2:./db/merge";
  private static final String DB_USER = "";
  private static final String DB_PASSWORD = "";

  private static final String CREATE_QUERY =
      "create table merge(msg_key varchar(40) primary key, sender_que object)";
  private static final String INSERT_QUERY = "insert into merge(msg_key, sender_que) values(?,?)";
  private static final String SELECT_QUERY = "select * from merge";

  public H2MessageMerger(MessagePublisher messagePublisher, int expiredTimeout) {
    super(messagePublisher, expiredTimeout);
  }

  @Override
  public boolean save(SenderQue que) {
    connect();

    PreparedStatement createPreparedStatement = null;
    PreparedStatement insertPreparedStatement = null;
    PreparedStatement selectPreparedStatement = null;

    try {
      connection.setAutoCommit(false);

      createPreparedStatement = connection.prepareStatement(CREATE_QUERY);
      createPreparedStatement.executeUpdate();
      createPreparedStatement.close();

      insertPreparedStatement = connection.prepareStatement(INSERT_QUERY);
      insertPreparedStatement.setString(1, que.getMsgKey());
      insertPreparedStatement.setObject(2, que);
      insertPreparedStatement.executeUpdate();
      insertPreparedStatement.close();

      selectPreparedStatement = connection.prepareStatement(SELECT_QUERY);
      ResultSet rs = selectPreparedStatement.executeQuery();
      while (rs.next()) {
        log.info("msg_key:{} sender_que:{}", rs.getString("msg_key"),
            rs.getObject("sender_que").toString());
      }
      selectPreparedStatement.close();

      connection.commit();
      return true;
    } catch (SQLException e) {
      log.error(HUtil.getStackTrace(e));
    } catch (Exception e) {
      log.error(HUtil.getStackTrace(e));
    }
    return false;
  }

  @Override
  public SenderQue find(String key) {
    return null;
  }

  @Override
  public void findExpired(CallbackExpiredEvent event, int expiredTimeout) {}

  @Override
  public void close() {
    if (null != connection) {
      try {
        connection.close();
        connection = null;
      } catch (SQLException e) {
        log.error(HUtil.getStackTrace(e));
      }
    }
  }

  private void connect() {
    try {
      Class.forName(DB_DRIVER);
    } catch (ClassNotFoundException e) {
      System.out.println(e.getMessage());
    }
    try {
      if (null == connection) {
        connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
      }
    } catch (SQLException e) {
      log.error(HUtil.getStackTrace(e));
    }
  }

}
