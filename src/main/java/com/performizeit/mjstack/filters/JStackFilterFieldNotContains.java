package com.performizeit.mjstack.filters;

import com.performizeit.mjstack.parser.JStackMetadataStack;

public class JStackFilterFieldNotContains extends JStackFilterFieldContains {
    public JStackFilterFieldNotContains(String fieldName, String valNotContained) {
       super(fieldName,valNotContained);
    }

    @Override
    public boolean filter(JStackMetadataStack stck) {
        return !super.filter(stck);
    }
}
