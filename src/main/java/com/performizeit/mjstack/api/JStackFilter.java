package com.performizeit.mjstack.api;

import com.performizeit.mjstack.parser.JStackMetadataStack;

/**
 * Created by life on 22/2/14.
 */
public interface JStackFilter extends BasePlugin{
	public boolean filter(JStackMetadataStack stck);
}
