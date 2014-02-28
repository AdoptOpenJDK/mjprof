package com.performizeit.mjstack.comparators;

import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.Comparator;

public class ReversePropComparator implements Comparator<JStackMetadataStack> {
    private final String prop;

    public ReversePropComparator(String prop) {
        this.prop = prop;
    }
    @Override
    public int compare(JStackMetadataStack o1, JStackMetadataStack o2) {
        return ((Comparable)o1.getVal(prop)) .compareTo(o2.getVal(prop));

    }
}
