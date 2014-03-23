package com.performizeit.mjstack.api;

import com.performizeit.mjstack.parser.JStackMetadataStack;

/**
 * Created by life on 22/2/14.
 */
public interface JStackFilter {
    boolean filter(JStackMetadataStack stck);
}
