package com.performizeit.mjstack.parser;

/**
 * Created by life on 23/2/14.
 */
public class HexaLong implements Comparable<HexaLong>{
    long value;
    public HexaLong(long val) {
        value = val;

    }

    public HexaLong(String s) throws NumberFormatException {
        if (s.startsWith("0x")) {
            s=s.substring(2);
        }
        value = Long.parseLong(s, 16) ;

    }

    @Override
    public String toString() {
        return String.format("%x",value);
    }

    @Override
    public int compareTo(HexaLong o) {
        return new Long(value).compareTo(o.value);
    }

    public long getValue() {
        return value;
    }
}
