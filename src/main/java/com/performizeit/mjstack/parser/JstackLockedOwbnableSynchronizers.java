package com.performizeit.mjstack.parser;

/**
 * Created by life on 23/2/14.
 */
public class JstackLockedOwbnableSynchronizers implements Comparable<JstackLockedOwbnableSynchronizers> {

    String linesOfLOS;
    public JstackLockedOwbnableSynchronizers(String linesOfLOS) {
        this.linesOfLOS = linesOfLOS;
    }

    @Override
    public String toString() {
        return linesOfLOS.toString();
    }
    @Override
    public int compareTo(JstackLockedOwbnableSynchronizers o) {
        return linesOfLOS.compareTo(o.linesOfLOS);
    }
}
