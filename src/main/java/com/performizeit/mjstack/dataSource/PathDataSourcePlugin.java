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

@Plugin(name="path", paramTypes={},description = "sdtin")
public class PathDataSourcePlugin extends StreamDataSourcePlugin{

	public ArrayList<ThreadDump> getThreadDumps(String fileName) {
		ArrayList<String> stackStrings = getStackStringsFromStream(fileName);
		return buildJstacks(stackStrings);
	}
	
//	public ArrayList<String> getStackStringsFromStream(String fileName) {
//		return getStackStrings(fileName);
//	}
//	public ArrayList<String> getStackStringsFromStream() {
//		return getStackStrings("");
//	}
	
//	@SuppressWarnings("resource")
//	public ArrayList<String> getStackStrings(String fileName){
//		BufferedReader r = null;
//		if(fileName.length()==0){
//			r = new BufferedReader(new InputStreamReader(System.in));
//		}else {
//		 try {
//			r = new BufferedReader(new FileReader(fileName));
//		} catch (FileNotFoundException e) {
//			System.out.println("File " + fileName + "not found");
//		}
//		}
//		ArrayList<String> stackDumps = new ArrayList<String>();
//		StringBuilder linesOfStack = new StringBuilder();
//		String line;
//		try {
//			while ((line = r.readLine()) != null) {
//				if (line.length() > 0 && Character.isDigit(line.charAt(0))) {   //starting a new stack dump
//					if (linesOfStack.length() > 0) {
//						stackDumps.add(linesOfStack.toString());
//					}
//					linesOfStack = new StringBuilder();
//
//				}
//                if (line.startsWith(ThreadDump.JNI_GLOBAL_REFS))
//                    linesOfStack.append("\""); // need this hack for later parsing
//                linesOfStack.append(line).append("\n");
//
//			}
//		} catch (IOException e) {
//			System.err.println("Error while parsing stdin" + e);
//		}
//		if (linesOfStack.length() > 0) {
//			stackDumps.add(linesOfStack.toString());
//		}
//		return stackDumps;
//	}
	





}
