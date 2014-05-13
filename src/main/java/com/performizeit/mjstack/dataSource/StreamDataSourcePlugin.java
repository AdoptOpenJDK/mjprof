package com.performizeit.mjstack.dataSource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.performizeit.mjstack.api.DataSourcePlugin;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadDump;

@Plugin(name="sdtin", paramTypes={},description = "sdtin")
public abstract class StreamDataSourcePlugin implements DataSourcePlugin{


	public abstract ArrayList<ThreadDump> getThreadDumps(String fileName) ;
	
	protected ArrayList<String> getStackStringsFromStream(){
		return getStackStringsFromStream("");
	}

	public ArrayList<String> getStackStringsFromStream(String fileName){
		return getStackStringsFromFile(fileName);
	}
	public ArrayList<String> getStackStringsFromFile(String fileName)  {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<String> stackDumps = new ArrayList<String>();
		StringBuilder linesOfStack = new StringBuilder();
		String line;
		try {
			while ((line = r.readLine()) != null) {
				if (line.length() > 0 && Character.isDigit(line.charAt(0))) {   //starting a new stack dump
					if (linesOfStack.length() > 0) {
						stackDumps.add(linesOfStack.toString());
					}
					linesOfStack = new StringBuilder();

				}
				if (line.startsWith(ThreadDump.JNI_GLOBAL_REFS))
					linesOfStack.append("\""); // need this hack for later parsing
				linesOfStack.append(line).append("\n");

			}
		} catch (IOException e) {
			System.err.println("Error while parsing stdin" + e);
		}
		if (linesOfStack.length() > 0) {
			stackDumps.add(linesOfStack.toString());
		}
		return stackDumps;
	}

	protected  ArrayList<ThreadDump> buildJstacks(ArrayList<String> stackStrings) {
		ArrayList<ThreadDump> jStackDumps = new ArrayList<ThreadDump>(stackStrings.size());
		for (String stackDump : stackStrings) {
			ThreadDump stckDump = new ThreadDump(stackDump);
			jStackDumps.add(stckDump);
		}
		return jStackDumps;
	}


}
