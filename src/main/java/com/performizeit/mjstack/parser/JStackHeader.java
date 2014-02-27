package com.performizeit.mjstack.parser;

/**
 * Created by life on 23/2/14.
 */
public class JStackHeader {
    String header;
    public JStackHeader(String header) {
                  this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
