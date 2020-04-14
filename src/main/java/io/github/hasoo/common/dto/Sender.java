package io.github.hasoo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sender implements Serializable {
  private static final long serialVersionUID = 1L;
  private String name;
  private String attr;
  private int level;
}
