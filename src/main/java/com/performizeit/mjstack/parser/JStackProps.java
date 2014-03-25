package com.performizeit.mjstack.parser;

public interface JStackProps {
  /*  NAME,
    PRIO,
    TID,
    NID,
    STACK,
    STATUS,
    STATE,
    LOS,
    DAEMON ;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public String s() {
        return toString();
    }         */
       public static final String NAME = "name";     // the name of the thread
       public static final String PRIO = "prio";
       public static final String TID = "tid";
       public static final String NID = "nid";
       public static final String STACK = "stack";
       public static final String STATUS = "status";
       public static final String STATE = "state";
       public static final String LOS = "los";
       public static final String DAEMON = "daemon";

} ;