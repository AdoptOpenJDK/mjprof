package com.performizeit.mjstack.terminals;

import com.performizeit.mjstack.parser.JStackDump;

/**
 * Created by life on 28/2/14.
 */
public interface TerminalStep {
    public void addStackDump(JStackDump jsd);
}
