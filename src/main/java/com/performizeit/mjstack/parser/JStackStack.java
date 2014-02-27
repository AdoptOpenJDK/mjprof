package com.performizeit.mjstack.parser;

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

    @Override
    public int compareTo(JStackStack o) {
        return linesOfStack.compareTo(o.linesOfStack);
    }
}
