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

package com.performizeit.mjprof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.performizeit.mjprof.api.*;
import com.performizeit.mjprof.monads.MJStep;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.monads.StepInfo;
import com.performizeit.mjprof.monads.StepsRepository;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.mjprof.plugin.PluginUtils;
import com.performizeit.plumbing.Generator;
import com.performizeit.plumbing.GeneratorHandler;
import com.performizeit.plumbing.Pipe;
import com.performizeit.plumbing.PipeHandler;


public class MJProf {
    public static void main(String[] args) throws IOException {
        //  System.setProperty("java.awt.headless","true"); // when using lanterna we suffer when there we do not disable GUI
        if (args.length < 1) {
            printSynopsisAndExit();
        }

        ArrayList<MJStep> steps = parseCommandLine(join(args, " ").trim());
        if (steps == null) {
            printSynopsisAndExit();
        }
        boolean foundExplicitDataSource = false;
        for (MJStep mjstep : steps) {
            Object obj = getObjectFromStep(mjstep);
            if (PluginUtils.isDataSource((obj))) {
                foundExplicitDataSource = true;
            }
        }
        if (!foundExplicitDataSource) {
            steps.add(0,new MJStep("stdin"));

        }
        MJStep lastStep = steps.get(steps.size()-1);
        Object lststp = getObjectFromStep(lastStep);
        if (!PluginUtils.isOutputer(lststp)) {
            steps.add(new MJStep("stdout"));
        }




        constructPlumbing(steps);
        for (Pipe pipe : pipes) {
             pipe.start();
        }
        for (Generator g : generators) {
            g.start();
        }


    }

    static ArrayList <Pipe> pipes = new ArrayList<Pipe>();
    static ArrayList <Generator<ThreadInfo>> generators = new ArrayList<Generator<ThreadInfo>>();
    private static void constructPlumbing(ArrayList<MJStep> steps) {
        int i=0;

        for (MJStep step:steps) {    // create handlers
            Pipe p;
            if (PluginUtils.isDataSource(getObjectFromStep(step))) {
                MJStep noopStep = new MJStep("noop");
                PipeHandler handler = (PipeHandler)getObjectFromStep(noopStep);
                p = new Pipe("Pipe Thread "+step.getStepName() +i,handler);
                GeneratorHandler genHandler = (GeneratorHandler)getObjectFromStep(step);
                Generator<ThreadInfo> g = new Generator<ThreadInfo>("Generator Thread "+step.getStepName() +i,genHandler,p);
                generators.add(g);


            }   else {

                PipeHandler<ThreadInfo,ThreadInfo> handler = (PipeHandler<ThreadInfo,ThreadInfo>)getObjectFromStep(step);
                p = new Pipe<ThreadInfo,ThreadInfo> ("Pipe Thread "+step.getStepName() +i,handler);
            }
            pipes.add(p);
            if (i>0) {     //connect to previous pipe
              Pipe  prevPipe = pipes.get(i-1);
                prevPipe.setOutgoingPipe(p);
            }
            i++;


        }


    }

    private static Object getObjectFromStep(MJStep mjstep) {
        StepInfo step = StepsRepository.getStep(mjstep.getStepName());
        Object[] paramArgs = buildArgsArray(step.getParams(), mjstep.getStepArgs());
        Object obj = PluginUtils.initObj(step.getClazz(), step.getParamTypes(), paramArgs);
        return obj;
    }

    private static void printSynopsisAndExit() {
        System.err.println(getSynopsisString());
        System.exit(1);
    }

    public static String getSynopsisString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Synopsis\nA list of the following monads concatenated with . \n");
        List<String> keys = new ArrayList<String>(StepsRepository.getRepository().keySet());
        Collections.sort(keys);
        sb.append("\nData sources:\n");
        getSynopsisContent(sb, keys, DataSource.class);
        sb.append("\nFilters:\n");
        getSynopsisContent(sb, keys, Filter.class);
        sb.append("\nMappers:\n");
        getSynopsisContent(sb, keys,Mapper.class);
        getSynopsisContent(sb, keys,DumpReducer.class);
        getSynopsisContent(sb, keys,ThreadInfoComparator.class);
        sb.append("\nTerminals:\n");
        getSynopsisContent(sb, keys,Terminal.class);

        sb.append("\n  help                                  -Prints this message");
        return sb.toString();
    }

    private static void getSynopsisContent(StringBuilder sb, List<String> keys,Class pluginType) {
        int lineLength = 0;
        StepInfo stepInfo;
        for (String stepName : keys) {
            lineLength = 0;
            stepInfo = StepsRepository.getStep(stepName);
            if (!stepInfo.getPluginType().equals(pluginType))  continue;
            lineLength += appendAndCount(sb, "  "+stepName);
            if (stepInfo.getArgNum() > 0) {
                lineLength += appendAndCount(sb, "/");
                Param[] params = stepInfo.getParams();
                for (int i = 0; i < stepInfo.getArgNum(); i++) {
                    String paramName = params[i].value();
                    if (paramName == null || paramName.length() ==0)   // the default paramter name is ""
                        paramName = params[i].type().getSimpleName().toLowerCase();
                    if (params[i].optional()) {
                        paramName = "["+paramName+"]";
                    }
                    lineLength += appendAndCount(sb,paramName );
                    if (i < stepInfo.getArgNum() - 1) {
                        lineLength += appendAndCount(sb, ",");
                    }
                }
                lineLength += appendAndCount(sb, "/");
            }
            for (int j = 0; j < (60 - lineLength); j++) {
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
    static int findNextSeperator(String str) {
        boolean insideArgList = false;
        for (int i = 0; i < str.length(); i++) {

            if (str.charAt(i) == '/') {
                if (i<str.length()-1 && insideArgList && str.charAt(i+1) !='.'  && str.charAt(i+1) !=' ' && str.charAt(i+1) !='/') {
                    //do nothing
                } else {
                    insideArgList = !insideArgList;
                }
            }
            if ((str.charAt(i) == '.' || str.charAt(i) == ' ') && !insideArgList) return i;
        }
        return -1;
    }

    static ArrayList<String> splitCommandLine(String arg) {
        String argPart = arg;
        ArrayList<String> argParts = new ArrayList<String>();
        for (int idx = findNextSeperator(argPart); idx != -1; idx = findNextSeperator(argPart)) {
            argParts.add(argPart.substring(0, idx));
            argPart = argPart.substring(idx + 1);
        }
        argParts.add(argPart);
        return argParts;
    }

    static ArrayList<MJStep> parseCommandLine(String concatArgs) {

        ArrayList<String> argParts = splitCommandLine(concatArgs);
        ArrayList<MJStep> mjsteps = new ArrayList<MJStep>();
        for (String s : argParts) {
            if (s.equalsIgnoreCase("help")) {
                printSynopsisAndExit();
            }
            MJStep step = new MJStep(s);
            if (!StepsRepository.stepValid(step)) {
                System.err.println("Step " + step + " is invalid\n");
                return null;
            }
            mjsteps.add(step);
        }
        return mjsteps;
    }


    public static String join(String[] strs, String delim) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            b.append(strs[i]);
            if (i < strs.length - 1) b.append(delim);
        }
        return b.toString();
    }

    public static Object[] buildArgsArray(Param[] params, List<String> paramsVals) {
        Object[] paramsTrans = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            try {
                String val;
                if (paramsVals.size() <= i ) {
                    if (params[i].optional()) {
                        val = params[i].defaultValue();

                    } else {
                        throw new RuntimeException("Non optional parameter" + params[i].value() + "  is missing ");
                        // TODO improve error handling
                    }
                } else {
                    val = paramsVals.get(i);
                }


                if (params[i].type().equals(Integer.class) || params[i].type().equals(int.class)) {
                    paramsTrans[i] = Integer.parseInt(val);
                } else if (params[i].type().equals(Long.class) || params[i].type().equals(long.class)) {
                    paramsTrans[i] = Long.parseLong(val);
                } else if (params[i].type().equals(Boolean.class) || params[i].type().equals(boolean.class)) {
                    paramsTrans[i] = Boolean.parseBoolean(val);
                } else if (params[i].type().equals(Attr.class) ) {
                    paramsTrans[i] = new Attr(val);
                } else {
                    paramsTrans[i] = val;
                }
            } catch (NumberFormatException e) {
                System.err.println("Please re-enter - wrong parameter format.");
            }
        }
        return paramsTrans;
    }
}
