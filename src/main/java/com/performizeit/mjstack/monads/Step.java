package com.performizeit.mjstack.monads;

import com.performizeit.mjstack.parser.JStackDump;

/**
 * Created by life on 28/2/14.
 */
public interface Step {
    public void addStackDump(JStackDump jsd);
}
