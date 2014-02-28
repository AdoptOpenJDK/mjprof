package com.performizeit.mjstack;

import com.performizeit.mjstack.comparators.PropComparator;
import com.performizeit.mjstack.comparators.ReversePropComparator;
import com.performizeit.mjstack.filters.JStackFilterFieldContains;
import com.performizeit.mjstack.filters.JStackFilterFieldNotContains;
import com.performizeit.mjstack.mappers.StackFrameContains;
import com.performizeit.mjstack.mappers.TrimBottom;
import com.performizeit.mjstack.mappers.TrimTop;
import com.performizeit.mjstack.monads.MJStep;
import com.performizeit.mjstack.terminals.CountThreads;
import com.performizeit.mjstack.terminals.GroupByProp;
import com.performizeit.mjstack.mappers.PropEliminator;
import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.terminals.ListProps;
import com.performizeit.mjstack.terminals.TerminalStep;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MJStack {
    public static void main(String[] args) throws IOException {


        ArrayList<MJStep> steps = parseCommandLine(args);
        ArrayList<String> stackStrings = getStackStringsFromStdIn();

        ArrayList<JStackDump> jStackDumps = buildJstacks(stackStrings);

        //   stckDump = stckDump.filterDump(new JStackFilterFieldContains(args[0], args[1])).mapDump(new PropEliminator(args[2]));

        //System.out.println(stackDumps.size());
        for (MJStep mjstep : steps) {
            ArrayList<JStackDump> jStackDumpsOrig = jStackDumps;
            jStackDumps = new ArrayList<JStackDump>(jStackDumpsOrig.size());
            TerminalStep gbp = null;
            if (mjstep.getStepName().equals("group")) {
                gbp = new GroupByProp(mjstep.getStepArg(0));
            } else if (mjstep.getStepName().equals("list")) {
                gbp = new ListProps();
            }    else if (mjstep.getStepName().equals("count")) {
                gbp = new CountThreads();
            }

            for (JStackDump jsd : jStackDumpsOrig) {

                if (mjstep.getStepName().equals("contains")) {
                    jStackDumps.add(jsd.filterDump(new JStackFilterFieldContains(mjstep.getStepArg(0), mjstep.getStepArg(1))));
                }
                if (mjstep.getStepName().equals("ncontains")) {
                    jStackDumps.add(jsd.filterDump(new JStackFilterFieldNotContains(mjstep.getStepArg(0), mjstep.getStepArg(1))));
                } else if (mjstep.getStepName().equals("sort")) {
                    jStackDumps.add(jsd.sortDump(new PropComparator(mjstep.getStepArg(0))));
                } else if (mjstep.getStepName().equals("sortd")) {
                        jStackDumps.add(jsd.sortDump(new ReversePropComparator(mjstep.getStepArg(0))));

                } else if (mjstep.getStepName().equals("eliminate")) {
                    jStackDumps.add(jsd.mapDump(new PropEliminator(mjstep.getStepArg(0))));
                }else if (mjstep.getStepName().equals("keeptop")) {
                    jStackDumps.add(jsd.mapDump(new TrimBottom(Integer.parseInt(mjstep.getStepArg(0)))));
                } else if (mjstep.getStepName().equals("keepbottom")) {
                    jStackDumps.add(jsd.mapDump(new TrimTop(Integer.parseInt(mjstep.getStepArg(0)))));
                }
                else if (mjstep.getStepName().equals("stackelim")) {
                    jStackDumps.add(jsd.mapDump(new StackFrameContains(mjstep.getStepArg(0),true)));
                } else if (mjstep.getStepName().equals("stackkeep")) {
                    jStackDumps.add(jsd.mapDump(new StackFrameContains(mjstep.getStepArg(0),false)));
                }

                else if (mjstep.getStepName().equals("group") || mjstep.getStepName().equals("list") || mjstep.getStepName().equals("count")) {
                    gbp.addStackDump(jsd);
                }
            }
            if (mjstep.getStepName().equals("group") || mjstep.getStepName().equals("list") || mjstep.getStepName().equals("count")) {
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
        System.exit(1);
    }

    private static ArrayList<MJStep> parseCommandLine(String[] args) {
        if (args.length != 1) {
            printSynopsisAndExit();
        }
        ArrayList<MJStep> mjsteps = new ArrayList<MJStep>();
        for (String s : args[0].split("\\.")) {
            mjsteps.add(new MJStep(s));
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
