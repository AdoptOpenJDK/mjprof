/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjstack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.performizeit.mjstack.api.*;
import com.performizeit.mjstack.monads.MJStep;
import com.performizeit.mjstack.monads.StepInfo;
import com.performizeit.mjstack.monads.StepsRepository;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadDumpBase;
import com.performizeit.mjstack.plugin.PluginUtils;


public class MJStack {
	public static void main(String[] args) throws IOException {
      //  System.setProperty("java.awt.headless","true"); // when using lanterna we suffer when there we do not disable GUI
		if (args.length <1) {
			printSynopsisAndExit();
		}

		ArrayList<MJStep> steps = parseCommandLine(join(args, " ").trim());
		if (steps == null) {
			printSynopsisAndExit();
		}
		ArrayList<String> stackStrings = getStackStringsFromStdIn();

		ArrayList<ThreadDumpBase> jStackDumps = buildJstacks(stackStrings);

		for (MJStep mjstep : steps) {
			ArrayList<ThreadDumpBase> jStackDumpsOrig = jStackDumps;
			jStackDumps = new ArrayList<ThreadDumpBase>(jStackDumpsOrig.size());
			StepInfo step = StepsRepository.getStep(mjstep.getStepName());
			Object[] paramArgs = buildArgsArray(step.getParamTypes(),mjstep.getStepArgs());
			for (ThreadDumpBase jsd : jStackDumpsOrig) {
				Object obj=PluginUtils.initObj(step.getClazz(), step.getParamTypes(), paramArgs);
				if(PluginUtils.isImplementsMapper(obj.getClass())){
					jStackDumps.add(jsd.mapDump((JStackMapper) obj));
				}else if(PluginUtils.isImplementsDumpMapper(obj.getClass())){
                    jStackDumps.add(jsd.mapDump((DumpMapper) obj));
                }else if(PluginUtils.isImplementsFilter(obj.getClass())){
					jStackDumps.add(jsd.filterDump((JStackFilter) obj));
				}else if(PluginUtils.isImplementsTerminal(obj.getClass())){
					jStackDumps.add(jsd.terminateDump((JStackTerminal) obj));
				}else if(PluginUtils.isImplementsComparators(obj.getClass())){
					jsd.sortDump((JStackComparator)obj);
				}
			}
		}


		for (int i = 0; i < jStackDumps.size(); i++) {
			System.out.println(jStackDumps.get(i));
		}
	}


	private static void printSynopsisAndExit(){
		System.out.println(getSynopsisString());
		System.exit(1);
	}

	public static String getSynopsisString(){
		StringBuilder sb= new StringBuilder();
		StringBuilder command;
		sb.append("synopsis\n");
		sb.append("Building Blocks*\n");
		List<String> keys = new ArrayList<String>(StepsRepository.getRepository().keySet());
		Collections.sort(keys);
		getSynopsisContent(sb, keys);
		sb.append("help                                    -Prints this message");
		return sb.toString();
	}
	private static void getSynopsisContent(StringBuilder sb, List<String> keys) {
		int lineLength=0;
		StepInfo stepInfo;
		for (String stepName : keys) {
			lineLength=0;
			stepInfo = StepsRepository.getStep(stepName);
			lineLength += appendAndCount(sb,stepName);
			if(stepInfo.getArgNum()>0){
				lineLength += appendAndCount(sb,"/");
				for(int i=0;i<stepInfo.getArgNum();i++){
					lineLength += appendAndCount(sb,stepInfo.getParamTypes()[i].getSimpleName());
					if(i==stepInfo.getArgNum()-2){
						lineLength += appendAndCount(sb,",");
					}
				}
				lineLength += appendAndCount(sb,"/");
			}
			for(int j=0;j<(40-lineLength);j++){
				sb.append(" ");
			}
			sb.append("-");
			sb.append(stepInfo.getDescription());
			sb.append("\n");
		}
	}


	private static int appendAndCount(StringBuilder command, String str) {
		command.append(str);
		return str.length();
	}

	// a separator between steps can be either a period of a space if  part of argument list (inside // it is ignored)
	static int findNextSeperator(String str)  {
		boolean insideArgList= false;
		for (int i=0;i<str.length();i++) {
			if (str.charAt(i) =='/') insideArgList = !insideArgList;
			if ((str.charAt(i) =='.' || str.charAt(i) ==' ') && !insideArgList)  return i;

		}
		return -1;
	}
	static ArrayList<String> splitCommandLine(String arg) {
		String argPart = arg;
		ArrayList<String> argParts = new ArrayList<String>();
		for (int idx =  findNextSeperator(argPart);idx != -1;idx = findNextSeperator(argPart)) {
			argParts.add(argPart.substring(0,idx));
			argPart = argPart.substring(idx+1);
		}
		argParts.add(argPart);
		return argParts;
	}
	static ArrayList<MJStep> parseCommandLine(String concatArgs) {

		ArrayList<String> argParts = splitCommandLine(concatArgs);
		ArrayList<MJStep> mjsteps = new ArrayList<MJStep>();
		for (String s : argParts) {
			if(s.equalsIgnoreCase("help")){
				printSynopsisAndExit();
			}
			MJStep step = new MJStep(s);
			if(!StepsRepository.stepValid(step)){
				System.out.println("Step " + step + " is invalid\n");
				return null;
			}
			mjsteps.add(step);
		}
		return mjsteps;
	}

	public static ArrayList<String> getStackStringsFromStdIn() {
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
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

	public static String join(String[] strs,String delim) {
		StringBuilder b = new StringBuilder();
		for (int i =0;i<strs.length;i++ ) {
			b.append(strs[i]);
			if (i<strs.length-1) b.append(delim); 
		}
		return b.toString();
	}

	public static ArrayList<ThreadDumpBase> buildJstacks(ArrayList<String> stackStrings) {
		ArrayList<ThreadDumpBase> jStackDumps = new ArrayList<ThreadDumpBase>(stackStrings.size());
		for (String stackDump : stackStrings) {
			ThreadDump stckDump = new ThreadDump(stackDump);
			jStackDumps.add(stckDump);
		}
		return jStackDumps;
	}
	
	public static Object[] buildArgsArray(Class[] paramTypes, List<String> params) {
		Object[] paramsTrans = new Object[paramTypes.length];

        for (int i=0;i<paramTypes.length;i++) {
        	try{
            if (paramTypes[i].equals(Integer.class) || paramTypes[i].equals(int.class)) {
                paramsTrans[i] = Integer.parseInt(params.get(i));
            } else
            if (paramTypes[i].equals(Long.class) || paramTypes[i].equals(long.class)) {
                paramsTrans[i] = Long.parseLong(params.get(i));
            }else {
                paramsTrans[i] =   params.get(i);
            }
        	}catch (NumberFormatException e){
        		System.out.println("Please re-enter - wrong parameter format.");
        	}
        }
		return paramsTrans;
	}
}
