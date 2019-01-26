package com.hasoo.sample.dummyclient.dto;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SenderQue {
  private String msgKey;
  private String userKey;
  private String groupname;
  private String username;
  private Date resDate;
  private String msgType;
  private String contentType;
  private String phone;
  private String callback;
  private String message;

  private String routingType;
  private List<Sender> senders;
}
