package com.hasoo.message.dummyclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sender {
  private String name;
  private String attr;
  private int level;
}
