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
import com.performizeit.mjstack.dataSource.StdinDataSourcePlugin;
import com.performizeit.mjstack.monads.MJStep;
import com.performizeit.mjstack.monads.StepInfo;
import com.performizeit.mjstack.monads.StepsRepository;
import com.performizeit.mjstack.parser.ThreadDump;
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

		ArrayList<ThreadDump> jStackDumps = new  ArrayList<ThreadDump>();

		//calling the relevant datasource plugin
		for (MJStep mjstep : steps) {
			//TODO: Save the objects so we won't create it twice
			Object obj = getObjectFromStep(mjstep);
			ArrayList<ThreadDump> dumps;
			if(PluginUtils.isImplementsDataSource((obj.getClass()))){
				dumps = ((DataSourcePlugin) obj).getThreadDumps();
				if(dumps!=null){
					jStackDumps.addAll(dumps);
				}
			}
		}
		//Default
		if(jStackDumps.isEmpty()){
			StdinDataSourcePlugin std=new StdinDataSourcePlugin();
			jStackDumps=std.getThreadDumps();
		}

		for (MJStep mjstep : steps) {
			ArrayList<ThreadDump> jStackDumpsOrig = jStackDumps;
			jStackDumps = new ArrayList<ThreadDump>(jStackDumpsOrig.size());

			Object obj = getObjectFromStep(mjstep);

			for (ThreadDump jsd : jStackDumpsOrig) {
				if(PluginUtils.isImplementsMapper(obj.getClass())){
					jStackDumps.add(jsd.mapDump((JStackMapper) obj));
				}else if(PluginUtils.isImplementsDumpMapper(obj.getClass())){
                    jStackDumps.add(jsd.mapDump((DumpMapper) obj));
                } else if(PluginUtils.isImplementsDumpReducer(obj.getClass())){

                        DumpReducer dr = (DumpReducer) obj;
                        dr.reduce(jsd);

                } else if(PluginUtils.isImplementsFilter(obj.getClass())){
					jStackDumps.add(jsd.filterDump((JStackFilter) obj));
				}else if(PluginUtils.isImplementsTerminal(obj.getClass())){
					jStackDumps.add(jsd.terminateDump((JStackTerminal) obj));
				}else if(PluginUtils.isImplementsComparators(obj.getClass())){
					jsd.sortDump((JStackComparator)obj);
				}
			}
            if(PluginUtils.isImplementsDumpReducer(obj.getClass())) {
                DumpReducer dr = (DumpReducer) obj;
                jStackDumps.add(dr.getResult());
            }

		}


		for (int i = 0; i < jStackDumps.size(); i++) {
			System.out.println(jStackDumps.get(i));
		}
	}


	private static Object getObjectFromStep(MJStep mjstep) {
		StepInfo step = StepsRepository.getStep(mjstep.getStepName());
		Object[] paramArgs = buildArgsArray(step.getParamTypes(),mjstep.getStepArgs());
		Object obj=PluginUtils.initObj(step.getClazz(), step.getParamTypes(), paramArgs);
		return obj;
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


	public static String join(String[] strs,String delim) {
		StringBuilder b = new StringBuilder();
		for (int i =0;i<strs.length;i++ ) {
			b.append(strs[i]);
			if (i<strs.length-1) b.append(delim); 
		}
		return b.toString();
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
