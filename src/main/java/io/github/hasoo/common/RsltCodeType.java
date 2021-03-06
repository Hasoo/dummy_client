package io.github.hasoo.common;

public enum RsltCodeType {
  /* @formatter:off */
    SUCC("1000")
  , INVA("2000")
  , TIME("3000")
  , FAIL("4000")
  ;
  /* @formatter:on */

    private final String code;

  RsltCodeType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
