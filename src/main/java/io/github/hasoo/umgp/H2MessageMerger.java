package io.github.hasoo.umgp;

import io.github.hasoo.common.CallbackExpiredEvent;
import io.github.hasoo.common.MessageMerger;
import io.github.hasoo.common.dto.SenderQue;
import io.github.hasoo.common.rabbitmq.MessagePublisher;
import io.github.hasoo.util.HUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Date;

@Slf4j
public class H2MessageMerger extends MessageMerger {
  private Connection connection = null;

  private static final String DB_DRIVER = "org.h2.Driver";
  private static final String DB_CONNECTION = "jdbc:h2:./db/merge";
  private static final String DB_USER = "";
  private static final String DB_PASSWORD = "";

  private static final String CREATE_QUERY =
      "create table merge(msg_key varchar(40) primary key, sender_que object, res_date timestamp)";
  private static final String INDEX_QUERY = "create index idx_merge on merge(res_date)";
  private static final String INSERT_QUERY =
      "insert into merge(msg_key, sender_que, res_date) values(?,?,?)";
  private static final String SELECT_QUERY = "select * from merge where msg_key=?";
  private static final String EXPIRED_QUERY = "select * from merge where res_date <= ?";
  private static final String DELETE_QUERY = "delete from merge where msg_key=?";

  public H2MessageMerger(MessagePublisher messagePublisher, int expiredTimeout) {
    super(messagePublisher, expiredTimeout);
    connect();
  }

  @Override
  public boolean save(SenderQue que) {
    try {
      if (false == insert(que)) { // not exist table
        createTable();
        insert(que);
      }

    } catch (SQLException e) {
      if (23505 == e.getErrorCode()) {
        log.warn("duplicated key:{}", que.getMsgKey());
      } else {
        log.error("state:{} code:{} message:{}", e.getSQLState(), e.getErrorCode(), e.getMessage());
      }
    }
    return false;
  }

  @Override
  public SenderQue find(String key) {
    try (PreparedStatement selectPreparedStatement = connection.prepareStatement(SELECT_QUERY)) {
      selectPreparedStatement.setString(1, key);
      ResultSet rs = selectPreparedStatement.executeQuery();
      if (rs.next()) {
        SenderQue que = (SenderQue) rs.getObject("sender_que");
        delete(que.getMsgKey());
        return que;
      }
    } catch (SQLException e) {
      if (42102 != e.getErrorCode()) {
        log.error(HUtil.getStackTrace(e));
      }
    }

    return null;
  }

  @Override
  public void findExpired(CallbackExpiredEvent event, int expiredTimeout) {
    Date now = new Date();
    try (PreparedStatement selectPreparedStatement = connection.prepareStatement(EXPIRED_QUERY)) {

      Timestamp expiredTimestamp = new Timestamp(now.getTime() - expiredTimeout);
      selectPreparedStatement.setTimestamp(1, expiredTimestamp);
      ResultSet rs = selectPreparedStatement.executeQuery();
      while (rs.next()) {
        SenderQue que = (SenderQue) rs.getObject("sender_que");
        event.expired(que);
        delete(que.getMsgKey());
      }
    } catch (SQLException e) {
      if (42102 != e.getErrorCode()) {
        log.error(HUtil.getStackTrace(e));
      }
    }
  }

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

  private boolean delete(String key) throws SQLException {
    try (PreparedStatement insertPreparedStatement = connection.prepareStatement(DELETE_QUERY)) {

      insertPreparedStatement.setString(1, key);
      insertPreparedStatement.executeUpdate();
      insertPreparedStatement.close();

      return true;
    } catch (SQLException e) {
      if (42102 != e.getErrorCode()) {
        throw e;
      }
    }

    return false;
  }

  private boolean insert(SenderQue que) throws SQLException {
    try (PreparedStatement insertPreparedStatement = connection.prepareStatement(INSERT_QUERY)) {

      insertPreparedStatement.setString(1, que.getMsgKey());
      insertPreparedStatement.setObject(2, que);
      insertPreparedStatement.setTimestamp(3, new Timestamp(que.getResDate().getTime()));
      insertPreparedStatement.executeUpdate();
      return true;
    } catch (SQLException e) {
      if (42102 != e.getErrorCode()) {
        throw e;
      }
    }

    return false;
  }

  private void createTable() {
    try (PreparedStatement createPreparedStatement = connection.prepareStatement(CREATE_QUERY)) {
      createPreparedStatement.executeUpdate();
    } catch (SQLException e) {
      log.error(HUtil.getStackTrace(e));
    }
    try (PreparedStatement createPreparedStatement = connection.prepareStatement(INDEX_QUERY)) {
      createPreparedStatement.executeUpdate();
    } catch (SQLException e) {
      log.error(HUtil.getStackTrace(e));
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
