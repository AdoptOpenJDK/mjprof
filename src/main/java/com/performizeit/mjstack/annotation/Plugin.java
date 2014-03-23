package com.performizeit.annotation;

import com.performizeit.mjstack.parser.JStackMetadataStack;
//TODO: change the interface (or the mapper and filter base classes) to include both filter and mapper
public interface Plugin {
	
	public JStackMetadataStack excute(JStackMetadataStack stck);
	
	public String getHelpLine();
	

}
