package com.performizeit.mjstack.dataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadDump;

@Plugin(name="stdin", paramTypes={},description = "Read thread dumps from standard input")
public class StdinDataSourcePlugin extends StreamDataSourcePluginBase {
    public StdinDataSourcePlugin() {
    }
	@Override
	public ArrayList<ThreadDump> getThreadDumps() {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<String> stackStrings = getStackStringsFromReader(r);
		return buildJstacks(stackStrings);
	}
}
