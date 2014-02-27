package com.performizeit.mjstack.comparators;

import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.Comparator;

public class PropComparator implements Comparator<JStackMetadataStack> {
    private final String prop;

    public PropComparator(String prop) {
        this.prop = prop;
    }
    @Override
    public int compare(JStackMetadataStack o1, JStackMetadataStack o2) {
        return ((Comparable)o1.getVal(prop)) .compareTo(o2.getVal(prop));

    }
}
