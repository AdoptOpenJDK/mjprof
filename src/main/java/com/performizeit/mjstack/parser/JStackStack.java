package com.performizeit.mjstack.parser;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by life on 23/2/14.
 */
public class JStackStack implements Comparable<JStackStack>{
    String linesOfStack;
    public JStackStack(String linesOfStack) {
                        this.linesOfStack = linesOfStack;
    }

    @Override
    public String toString() {
        return linesOfStack.toString();
    }

    public String[] getStackFrames () {
        return linesOfStack.split("\n");
    }

    @Override
    public int compareTo(JStackStack o) {
        return linesOfStack.compareTo(o.linesOfStack);
    }

    public void setStackFrames(String[] stackFrames) {
        linesOfStack =  join(Arrays.asList(stackFrames), "\n");
    }
    public void setStackFrames(ArrayList<String> stackFrames) {
        linesOfStack =  join(stackFrames,"\n");
    }

    public static String join(List<?> list, String delim) {
        int len = list.size();
        if (len == 0)
            return "";
        StringBuilder sb = new StringBuilder(list.get(0).toString());
        for (int i = 1; i < len; i++) {
            sb.append(delim);
            sb.append(list.get(i).toString());
        }
        return sb.toString();
    }
}
