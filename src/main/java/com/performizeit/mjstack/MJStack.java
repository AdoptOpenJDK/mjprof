/*
       This file is part of mjstack.

        mjstack is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjstack is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.performizeit.mjstack;

import static com.performizeit.mjstack.monads.StepProps.CONTAINS;
import static com.performizeit.mjstack.monads.StepProps.COUNT;
import static com.performizeit.mjstack.monads.StepProps.ELIMINATE;
import static com.performizeit.mjstack.monads.StepProps.GROUP;
import static com.performizeit.mjstack.monads.StepProps.KEEP_BOT;
import static com.performizeit.mjstack.monads.StepProps.KEEP_TOP;
import static com.performizeit.mjstack.monads.StepProps.LIST;
import static com.performizeit.mjstack.monads.StepProps.NOT_CONTAINS;
import static com.performizeit.mjstack.monads.StepProps.NO_OP;
import static com.performizeit.mjstack.monads.StepProps.SORT;
import static com.performizeit.mjstack.monads.StepProps.SORT_DESC;
import static com.performizeit.mjstack.monads.StepProps.STACK_ELIM;
import static com.performizeit.mjstack.monads.StepProps.STACK_KEEP;
import static com.performizeit.mjstack.monads.StepProps.TRIM_BELOW;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.comparators.PropComparator;
import com.performizeit.mjstack.comparators.ReversePropComparator;
import com.performizeit.mjstack.filters.JStackFilterFieldContains;
import com.performizeit.mjstack.filters.JStackFilterFieldNotContains;
import com.performizeit.mjstack.mappers.PropEliminator;
import com.performizeit.mjstack.mappers.StackFrameContains;
import com.performizeit.mjstack.mappers.TrimBelow;
import com.performizeit.mjstack.mappers.TrimBottom;
import com.performizeit.mjstack.mappers.TrimTop;
import com.performizeit.mjstack.monads.MJStep;
import com.performizeit.mjstack.monads.StepInfo;
import com.performizeit.mjstack.monads.StepsRepository;
import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.terminals.CountThreads;
import com.performizeit.mjstack.terminals.GroupByProp;
import com.performizeit.mjstack.terminals.ListProps;


public class MJStack {
	public static void main(String[] args) throws IOException {
		if (args.length <1) {
			printSynopsisAndExit();
		}

		ArrayList<MJStep> steps = parseCommandLine(join(args, " ").trim());
		if (steps == null) {
			printSynopsisAndExit();
		}
		ArrayList<String> stackStrings = getStackStringsFromStdIn();

		ArrayList<JStackDump> jStackDumps = buildJstacks(stackStrings);


		for (MJStep mjstep : steps) {
			ArrayList<JStackDump> jStackDumpsOrig = jStackDumps;
			jStackDumps = new ArrayList<JStackDump>(jStackDumpsOrig.size());
			JStackTerminal gbp = null;
			StepInfo step = StepsRepository.getStep(mjstep.getStepName());

			//			try {
			//				for (JStackDump jsd : jStackDumpsOrig) {
			//					Object obj=PluginUtils.initObj(step.getClazz(), step.getParamTypes(), mjstep.getStepArgs());
			//					if(PluginUtils.isImplementsMapper(obj.getClass())){
			//						jStackDumps.add(jsd.mapDump((JStackMapper) obj));
			//					}else if(PluginUtils.isImplementsFilter(obj.getClass())){
			//						jStackDumps.add(jsd.filterDump((JStackFilter)obj));
			//					}else if(PluginUtils.isImplementsTerminal(obj.getClass())){
			//						gbp=(JStackTerminal) obj;
			//					}//TODO comprator
			//				}
			//			} catch (NoSuchMethodException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			} catch (InstantiationException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			} catch (IllegalAccessException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			} catch (InvocationTargetException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}

			if (mjstep.getStepName().equals(GROUP.getToken())) {
				gbp = new GroupByProp(mjstep.getStepArg(0));
			} else if (mjstep.getStepName().equals(LIST.getToken())) {
				gbp = new ListProps();
			}    else if (mjstep.getStepName().equals(COUNT.getToken())) {
				gbp = new CountThreads();
			}

			for (JStackDump jsd : jStackDumpsOrig) {

				if (mjstep.getStepName().equals(CONTAINS.getToken())) {
					jStackDumps.add(jsd.filterDump(new JStackFilterFieldContains(mjstep.getStepArg(0), mjstep.getStepArg(1))));
				}
				if (mjstep.getStepName().equals(NOT_CONTAINS.getToken())) {
					jStackDumps.add(jsd.filterDump(new JStackFilterFieldNotContains(mjstep.getStepArg(0), mjstep.getStepArg(1))));
				} else if (mjstep.getStepName().equals(SORT.getToken())) {
					jStackDumps.add(jsd.sortDump(new PropComparator(mjstep.getStepArg(0))));
				} else if (mjstep.getStepName().equals(SORT_DESC.getToken())) {
					jStackDumps.add(jsd.sortDump(new ReversePropComparator(mjstep.getStepArg(0))));

				} else if (mjstep.getStepName().equals(ELIMINATE.getToken())) {
					jStackDumps.add(jsd.mapDump(new PropEliminator(mjstep.getStepArg(0))));
				}else if (mjstep.getStepName().equals(KEEP_TOP.getToken())) {
					jStackDumps.add(jsd.mapDump(new TrimBottom(Integer.parseInt(mjstep.getStepArg(0)))));
				} else if (mjstep.getStepName().equals(KEEP_BOT.getToken())) {
					jStackDumps.add(jsd.mapDump(new TrimTop(Integer.parseInt(mjstep.getStepArg(0)))));
				}
				else if (mjstep.getStepName().equals(STACK_ELIM.getToken())) {
					jStackDumps.add(jsd.mapDump(new StackFrameContains(mjstep.getStepArg(0),true)));
				} else if (mjstep.getStepName().equals(STACK_KEEP.getToken())) {
					jStackDumps.add(jsd.mapDump(new StackFrameContains(mjstep.getStepArg(0),false)));
				}
				else if (mjstep.getStepName().equals(TRIM_BELOW.getToken())) {
					jStackDumps.add(jsd.mapDump(new TrimBelow(mjstep.getStepArg(0))));
				}
				else if (mjstep.getStepName().equals(NO_OP.getToken())) {
					jStackDumps.add(jsd); // do nothing
				}

				else if (mjstep.getStepName().equals(GROUP.getToken()) || mjstep.getStepName().equals(LIST.getToken()) || mjstep.getStepName().equals(COUNT.getToken())) {
					gbp.addStackDump(jsd);
				}
			}
			if (mjstep.getStepName().equals(GROUP.getToken()) || mjstep.getStepName().equals(LIST.getToken()) || mjstep.getStepName().equals(COUNT.getToken())) {
				System.out.print(gbp.toString());
				return;
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
		StringBuilder command;
		int lineLength=0;
		for (String stepName : keys) {
			lineLength=0;
			StepInfo stepInfo = StepsRepository.getStep(stepName);
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

	public static ArrayList<JStackDump> buildJstacks(ArrayList<String> stackStrings) {
		ArrayList<JStackDump> jStackDumps = new ArrayList<JStackDump>(stackStrings.size());
		for (String stackDump : stackStrings) {
			JStackDump stckDump = new JStackDump(stackDump);
			jStackDumps.add(stckDump);
		}
		return jStackDumps;
	}
}
