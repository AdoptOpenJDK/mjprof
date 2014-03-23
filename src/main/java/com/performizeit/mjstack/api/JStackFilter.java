package com.performizeit.mjstack.api;

import com.performizeit.mjstack.parser.JStackMetadataStack;

/**
 * Created by life on 22/2/14.
 */
public interface JStackFilter extends BasePlugin{
    boolean filter(JStackMetadataStack stck);

	String getHelpLine();
}
