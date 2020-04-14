package io.github.hasoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hasoo.common.dto.SenderQue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsumerTest {
  ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testJackson() {
    String json =
        "{\"msgKey\":\"149e818a553b410d84413898f7ca90b2\",\"userKey\":\"1\",\"groupname\":\"TEST\",\"username\":\"test\",\"resDate\":1548390779105,\"msgType\":\"SMS\",\"contentType\":\"SMS\",\"phone\":\"01029663620\",\"callback\":\"01022222222\",\"message\":\"testmessage\",\"code\":null,\"desc\":null,\"doneDate\":null,\"net\":null,\"routingType\":\"order\",\"senders\":[{\"name\":\"SKT_1\",\"attr\":\"SKT\",\"level\":1},{\"name\":\"KT_1\",\"attr\":\"KT\",\"level\":3}]}";
    try {
      // Object jsonObject = mapper.readValue(json, Object.class);
      // System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject));
      SenderQue senderQue = mapper.readValue(json, SenderQue.class);
      assertEquals("149e818a553b410d84413898f7ca90b2", senderQue.getMsgKey());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
