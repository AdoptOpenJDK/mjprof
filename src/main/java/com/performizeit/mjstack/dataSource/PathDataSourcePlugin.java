package com.performizeit.mjstack.dataSource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadDump;

@Plugin(name="path", paramTypes={String.class},description = "Read thread dump from file ")
public class PathDataSourcePlugin extends StreamDataSourcePluginBase {
    String fileName;

    public PathDataSourcePlugin(String fileName) {
        this.fileName = fileName;
    }
	public ArrayList<ThreadDump> getThreadDumps() {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		ArrayList<String> stackStrings = getStackStringsFromReader(r);
		return buildJstacks(stackStrings);
	}
}
