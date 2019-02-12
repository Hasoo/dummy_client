package com.hasoo.dummyclient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.hasoo.dummyclient.common.dto.SenderQue;
import com.hasoo.dummyclient.util.HUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class H2Test {
  @BeforeEach
  public void setUp() {
    // DeleteDbFiles.execute("./db", "test", true);
  }

  @AfterEach
  public void setDown() {}

  @Test
  public void testSave() {}

  @Test
  public void testFind() {}

  @Test
  public void testExpired() {}
}
