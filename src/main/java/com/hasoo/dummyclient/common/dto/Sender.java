package com.hasoo.dummyclient.common.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sender implements Serializable {
  private static final long serialVersionUID = 1L;
  private String name;
  private String attr;
  private int level;
}
