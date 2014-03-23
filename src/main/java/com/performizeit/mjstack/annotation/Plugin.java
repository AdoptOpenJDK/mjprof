package com.performizeit.mjstack.annotation;

import java.lang.annotation.*;

/*
 * Annotation for marking the class as Plugin : @Plugin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {
	
	public String getHelpLine();
	

}
