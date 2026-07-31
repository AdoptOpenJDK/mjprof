package com.performizeit.mjprof.core.api;

import com.performizeit.mjprof.core.parser.ThreadInfoProps;

public class Attr {
  public static final Attr NAME = new Attr(ThreadInfoProps.NAME);
  public static final Attr TID = new Attr(ThreadInfoProps.TID);
  public static final Attr NID = new Attr(ThreadInfoProps.NID);
  public static final Attr DAEMON = new Attr(ThreadInfoProps.DAEMON);
  public static final Attr PRIO = new Attr(ThreadInfoProps.PRIO);
  public static final Attr STATUS = new Attr(ThreadInfoProps.STATUS);
  public static final Attr STATE = new Attr(ThreadInfoProps.STATE);
  public static final Attr CPU_NS = new Attr(ThreadInfoProps.CPUNS);
  public static final Attr WALL_MS = new Attr(ThreadInfoProps.WALL);
  public static final Attr CPU_PCT = new Attr(ThreadInfoProps.CPU_PREC);
  public static final Attr STACK = new Attr(ThreadInfoProps.STACK);
  public static final Attr LOS = new Attr(ThreadInfoProps.LOS);
  public static final Attr COUNT = new Attr(ThreadInfoProps.COUNT);

  public final String attrName;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Attr)) return false;
    return java.util.Objects.equals(attrName, ((Attr) o).attrName);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hashCode(attrName);
  }
}
