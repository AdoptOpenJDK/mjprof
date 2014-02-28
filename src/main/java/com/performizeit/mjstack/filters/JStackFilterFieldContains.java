package com.performizeit.mjstack.filters;

import com.performizeit.mjstack.parser.JStackMetadataStack;

/**
 * Created by life on 22/2/14.
 */
public class JStackFilterFieldContains implements JStackFilter {
    private final String fieldName;
    private final String valContained;

    public JStackFilterFieldContains(String fieldName, String valContained) {
        this.fieldName = fieldName;
        this.valContained = valContained;
    }

    @Override
    public boolean filter(JStackMetadataStack stck) {
        Object o = stck.getVal(fieldName);
        if (o == null) return false;
        return o.toString().contains(valContained);

    }
}
