package com.performizeit.mjstack.dataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.performizeit.mjstack.api.DataSourcePlugin;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadDump;

@Plugin(name="sdtin", paramTypes={},description = "Anat")
public class JmxDataSourcePlugin implements DataSourcePlugin{

	@Override
	public ArrayList<ThreadDump> getStack() {
		return null;
		
	}


}
