package com.performizeit.mjstack.mergers;

import com.performizeit.mjstack.api.Plugin;

@Plugin(name="ctree", paramTypes={},description="combine all stack traces in color (UNIX Terminal) ")
public class StackTreeColor extends StackTreeMerger {
    public StackTreeColor() {
        st.color = true;
    }
}
