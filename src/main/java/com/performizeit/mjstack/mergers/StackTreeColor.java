package com.performizeit.mjstack.mergers;

import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.JStackMetadataStack;
import com.performizeit.mjstack.parser.JStackProps;
import com.performizeit.mjstack.parser.JStackStack;

import java.util.HashMap;
import java.util.HashSet;

@Plugin(name="ctree", paramTypes={},description="combine all stack traces in color (UNIX Terminal) ")
public class StackTreeColor extends StackTree {

    public StackTreeColor() {
        super.color = true;
    }


}
