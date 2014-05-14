package com.performizeit.mjstack.api;

/**
 * Created by life on 13/5/14.
 */
public class Attr  {
    public String attrName;

    public Attr(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrName() {
        return attrName;
    }

    @Override
    public String toString() {
        return attrName.toString();
    }
}
