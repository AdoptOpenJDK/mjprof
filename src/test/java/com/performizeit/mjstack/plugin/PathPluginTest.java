package com.performizeit.mjstack.plugin;

import java.util.HashMap;

import org.junit.Test;

import com.performizeit.mjstack.dataSource.PathDataSourcePlugin;

public class PathPluginTest {
	

	@Test
	public void testPlugin() throws Exception {
		
	}
	
	@Test
	public void getAllDataSourcePlugin() throws Exception {
		PathDataSourcePlugin plugin= new PathDataSourcePlugin();
		System.out.println(plugin.getStackStringsFromFile("c:/test.txt"));
	}

}
