package io.github.hasoo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SenderQue implements Serializable {
  private static final long serialVersionUID = 1L;
  private String msgKey;
  private String userKey;
  private String groupname;
  private String username;
  private Double fee;
  private Date resDate;
  private Date sentDate;
  private String msgType;
  private String contentType;
  private String phone;
  private String callback;
  private String message;
  private String code;
  private String desc;
  private Date doneDate;
  private String net;

  private String routingType;
  private List<Sender> senders;
}
