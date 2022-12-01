package com.performizeit.mjprof.api;

public class Attr {
  public String attrName;

  public Attr(String attrName) {
    this.attrName = attrName;
  }

  public String getAttrName() {
    return attrName;
  }

  @Override
  public String toString() {
    return attrName;
  }
}
