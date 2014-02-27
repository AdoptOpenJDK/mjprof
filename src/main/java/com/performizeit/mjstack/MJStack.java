package com.performizeit.mjstack;

import com.performizeit.mjstack.comparators.PropComparator;
import com.performizeit.mjstack.filters.JStackFilterField;
import com.performizeit.mjstack.mappers.PropEliminator;
import com.performizeit.mjstack.parser.JStackDump;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MJStack {
    public static void main(String[] args) throws IOException {


        ArrayList<MJStep> steps  = parseCommandLine(args);
        System.out.println("steps"+steps.size());
        ArrayList<String> stackStrings = getStackStringsFromStdIn();

        ArrayList<JStackDump> jStackDumps =      buildJstacks (stackStrings);

     //   stckDump = stckDump.filterDump(new JStackFilterField(args[0], args[1])).mapDump(new PropEliminator(args[2]));

        //System.out.println(stackDumps.size());
        for (MJStep mjstep  : steps) {
            ArrayList<JStackDump> jStackDumpsOrig = jStackDumps;
            jStackDumps = new ArrayList<JStackDump>(jStackDumpsOrig.size());
            for (JStackDump jsd : jStackDumpsOrig) {
                System.out.println("aaaaaa");

                if (mjstep.getStepName().equals("contains")) {
                    System.out.println( mjstep.getStepArg(0)+mjstep.getStepArg(1));
                    jStackDumps.add(jsd.filterDump(new JStackFilterField(mjstep.getStepArg(0),mjstep.getStepArg(1))));
                } else  if (mjstep.getStepName().equals("sort")) {
                    System.out.println( mjstep.getStepArg(0));
                    jStackDumps.add(jsd.sortDump(new PropComparator(mjstep.getStepArg(0))));
                } else  if (mjstep.getStepName().equals("eliminate")) {
                    jStackDumps.add(jsd.mapDump(new PropEliminator(mjstep.getStepArg(0))));
                }

            }
        }
        for (int i = 0; i < jStackDumps.size(); i++) {
            //System.out.println(jStackDumps.get(i).toString().equals(stackDumps.get(i)));

            System.out.println(jStackDumps.get(i));
        }
    }

    private static void printSynopsisAndExit() {
        System.out.println("synopsis");
        System.exit(1);
    }

    private static ArrayList<MJStep> parseCommandLine(String[] args) {
        if (args.length!=1) {
            printSynopsisAndExit();
        }
        ArrayList<MJStep> mjsteps = new ArrayList<MJStep>();
        for (String s: args[0].split("\\.") ) {
            Pattern p = Pattern.compile("(.*)/(.*)/");
            Matcher m = p.matcher(s);
            if (m.find()) {
                MJStep mjStep = new MJStep(m.group(1));
                System.out.println((m.group(1)));
                for (String q: m.group(2).split(",")) {
                    System.out.println(q);
                    mjStep.addStepArg(q);
                }
                mjsteps.add(mjStep);
            }
            System.out.println(s);

        }


        return mjsteps;
    }

    public static   ArrayList<String> getStackStringsFromStdIn() {
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
        }catch(IOException e) {
            System.err.println("Error while parsing stdin"+e);
        }
        if (linesOfStack.length() > 0) {
            stackDumps.add(linesOfStack.toString());
        }
        return stackDumps;

    }

    public  static ArrayList<JStackDump> buildJstacks ( ArrayList<String> stackStrings) {
        ArrayList<JStackDump> jStackDumps = new ArrayList<JStackDump>(stackStrings.size());
        for (String stackDump : stackStrings) {
            JStackDump stckDump = new JStackDump(stackDump);
            jStackDumps.add(stckDump);
        }
        return jStackDumps;
    }
}
