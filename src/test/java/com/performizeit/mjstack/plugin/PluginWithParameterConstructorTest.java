package com.performizeit.mjstack.plugin;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadInfo;
import com.performizeit.mjstack.model.StackTrace;

import java.util.ArrayList;
import java.util.HashMap;
/*
 * Only for TEST! need to be in another project
 */
@Plugin(name="test2",paramTypes = {String.class})
public class PluginWithParameterConstructorTest implements JStackMapper {
    private final String expr;

    public PluginWithParameterConstructorTest(String expr) {
        this.expr = expr;
    }

    public ThreadInfo map(ThreadInfo stck) {
        HashMap<String,Object> mtd = stck.cloneMetaData();
        StackTrace jss = (StackTrace) mtd.get("stack");
        String[] stackFrames = jss.getStackFrames();
        ArrayList<String> partial = new ArrayList<String>();
        boolean fromHere = false;
        for (int i=stackFrames.length-1;i>=0;i--) {

                if (stackFrames[i].contains(expr)  ) fromHere = true;
                if (fromHere)  partial.add(0,stackFrames[i]);
        }
        jss.setStackFrames(partial);
        return new ThreadInfo(mtd);
    }

	public ThreadInfo execute(ThreadInfo stck) {
		return map(stck);
	}

	public String getHelpLine() {
		return "TrimBelowhelp";
	}
}
