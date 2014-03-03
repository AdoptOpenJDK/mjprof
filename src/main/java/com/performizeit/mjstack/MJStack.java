package com.performizeit.mjstack;

import com.performizeit.mjstack.comparators.PropComparator;
import com.performizeit.mjstack.comparators.ReversePropComparator;
import com.performizeit.mjstack.filters.JStackFilterFieldContains;
import com.performizeit.mjstack.filters.JStackFilterFieldNotContains;
import com.performizeit.mjstack.mappers.*;
import com.performizeit.mjstack.monads.MJStep;
import com.performizeit.mjstack.monads.StepProps;
import com.performizeit.mjstack.terminals.CountThreads;
import com.performizeit.mjstack.terminals.GroupByProp;
import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.terminals.ListProps;
import com.performizeit.mjstack.terminals.TerminalStep;
import static com.performizeit.mjstack.monads.StepProps.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MJStack {
    public static void main(String[] args) throws IOException {
        ArrayList<MJStep> steps = parseCommandLine(args);
        if (steps == null) {
            printSynopsisAndExit();
        }
        ArrayList<String> stackStrings = getStackStringsFromStdIn();

        ArrayList<JStackDump> jStackDumps = buildJstacks(stackStrings);


        for (MJStep mjstep : steps) {
            ArrayList<JStackDump> jStackDumpsOrig = jStackDumps;
            jStackDumps = new ArrayList<JStackDump>(jStackDumpsOrig.size());
            TerminalStep gbp = null;
            if (mjstep.getStepName().equals(GROUP)) {
                gbp = new GroupByProp(mjstep.getStepArg(0));
            } else if (mjstep.getStepName().equals(LIST)) {
                gbp = new ListProps();
            }    else if (mjstep.getStepName().equals(COUNT)) {
                gbp = new CountThreads();
            }

            for (JStackDump jsd : jStackDumpsOrig) {

                if (mjstep.getStepName().equals(CONTAINS)) {
                    jStackDumps.add(jsd.filterDump(new JStackFilterFieldContains(mjstep.getStepArg(0), mjstep.getStepArg(1))));
                }
                if (mjstep.getStepName().equals(NOT_CONTAINS)) {
                    jStackDumps.add(jsd.filterDump(new JStackFilterFieldNotContains(mjstep.getStepArg(0), mjstep.getStepArg(1))));
                } else if (mjstep.getStepName().equals(SORT)) {
                    jStackDumps.add(jsd.sortDump(new PropComparator(mjstep.getStepArg(0))));
                } else if (mjstep.getStepName().equals(SORT_DESC)) {
                        jStackDumps.add(jsd.sortDump(new ReversePropComparator(mjstep.getStepArg(0))));

                } else if (mjstep.getStepName().equals(ELIMINATE)) {
                    jStackDumps.add(jsd.mapDump(new PropEliminator(mjstep.getStepArg(0))));
                }else if (mjstep.getStepName().equals(KEEP_TOP)) {
                    jStackDumps.add(jsd.mapDump(new TrimBottom(Integer.parseInt(mjstep.getStepArg(0)))));
                } else if (mjstep.getStepName().equals(KEEP_BOT)) {
                    jStackDumps.add(jsd.mapDump(new TrimTop(Integer.parseInt(mjstep.getStepArg(0)))));
                }
                else if (mjstep.getStepName().equals(STACK_ELIM)) {
                    jStackDumps.add(jsd.mapDump(new StackFrameContains(mjstep.getStepArg(0),true)));
                } else if (mjstep.getStepName().equals(STACK_KEEP)) {
                    jStackDumps.add(jsd.mapDump(new StackFrameContains(mjstep.getStepArg(0),false)));
                }
                else if (mjstep.getStepName().equals(TRIM_BELOW)) {
                    jStackDumps.add(jsd.mapDump(new TrimBelow(mjstep.getStepArg(0))));
                }

                else if (mjstep.getStepName().equals(GROUP) || mjstep.getStepName().equals(LIST) || mjstep.getStepName().equals(COUNT)) {
                    gbp.addStackDump(jsd);
                }
            }
            if (mjstep.getStepName().equals(GROUP) || mjstep.getStepName().equals(LIST) || mjstep.getStepName().equals(COUNT)) {
                System.out.print(gbp.toString());
                return;
            }

        }
        for (int i = 0; i < jStackDumps.size(); i++) {
            System.out.println(jStackDumps.get(i));
        }
    }

    private static void printSynopsisAndExit() {
        System.out.println("synopsis");
        System.out.println("Building Blocks\n" +
                " contains/attr,string/  - returns only threads which contains the string (regexp not supported)\n" +
                " ncontains/attr,string/ - returns only threads which do no contains the string(regexp not supported)\n" +
                " eliminate/attr/        - Removes a certain attribute e.g. eliminate/stack/\n" +
                " sort/attr/             - Sorts based on attribute\n" +
                " sortd/attr/            - Sorts based on attribute (descending order)\n" +
                " keeptop/int/           - Returns at most n top stack frames of the stack\n" +
                " keepbot/int/           - Returns at most n bottom stack frames of the stack\n" +
                " stackelim/string/      - Eliminates stack frames from all stacks which do not contain string\n" +
                " stackkeep/string/      - Keeps only stack frames from all stacks which contain string\n" +
                " trimbelow/string/      - trim all stack frames below the first occurance of string\n" +
                " count                  - counts number of threads\n" +
                " list                   - lists the possible stack trace attributes\n" +
                " group/attr/            - group by an attribute\n" +
                " help                   - Prints this message");
        System.exit(1);
    }
    static int findNextPeriod(String str)  {
        boolean insideArgList= false;
        for (int i=0;i<str.length();i++) {
            if (str.charAt(i) =='/') insideArgList = !insideArgList;
            if (str.charAt(i) =='.' && !insideArgList)  return i;

        }
        return -1;
    }
    static ArrayList<String> splitCommandLine(String arg) {
        String argPart = arg;
        ArrayList<String> argParts = new ArrayList<String>();
        for (int idx =  findNextPeriod(argPart);idx != -1;idx = findNextPeriod(argPart)) {
            argParts.add(argPart.substring(0,idx));
            argPart = argPart.substring(idx+1);
        }
        argParts.add(argPart);
        return argParts;
    }
    static ArrayList<MJStep> parseCommandLine(String[] args) {
        if (args.length < 1) {
            return null;
        }
        ArrayList<String> argParts = splitCommandLine(args[0]);
        ArrayList<MJStep> mjsteps = new ArrayList<MJStep>();
        for (String s : argParts) {
            MJStep step = new MJStep(s);
            if (!StepProps.stepValid(step))    {
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

    public static ArrayList<JStackDump> buildJstacks(ArrayList<String> stackStrings) {
        ArrayList<JStackDump> jStackDumps = new ArrayList<JStackDump>(stackStrings.size());
        for (String stackDump : stackStrings) {
            JStackDump stckDump = new JStackDump(stackDump);
            jStackDumps.add(stckDump);
        }
        return jStackDumps;
    }
}
