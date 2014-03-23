package com.performizeit.mjstack.api;

import com.performizeit.mjstack.parser.JStackMetadataStack;

/**
 * Created by life on 23/2/14.
 */
public interface JStackMapper extends BasePlugin{
	JStackMetadataStack map(JStackMetadataStack stck);
}
