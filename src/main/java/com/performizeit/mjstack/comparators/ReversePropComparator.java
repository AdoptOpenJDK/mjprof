package com.performizeit.mjstack.comparators;

import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.Comparator;

public class ReversePropComparator extends PropComparator {

    public ReversePropComparator(String prop) {
        super(prop);
    }

    @Override
    public int compare(JStackMetadataStack o1, JStackMetadataStack o2) {
        return -super.compare(o1,o2);

    }
}
