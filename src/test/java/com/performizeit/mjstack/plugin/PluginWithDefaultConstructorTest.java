package com.performizeit.mjstack.plugin;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.JStackMetadataStack;
import com.performizeit.mjstack.parser.JStackStack;

import java.util.ArrayList;
import java.util.HashMap;
/*
 * Only for TEST! need to be in another project
 */
@Plugin
public class PluginWithDefaultConstructorTest implements JStackMapper {
    private final String expr="kk";


    public JStackMetadataStack map(JStackMetadataStack stck) {
        HashMap<String,Object> mtd = stck.cloneMetaData();
        JStackStack jss = (JStackStack) mtd.get("stack");
        String[] stackFrames = jss.getStackFrames();
        ArrayList<String> partial = new ArrayList<String>();
        boolean fromHere = false;
        for (int i=stackFrames.length-1;i>=0;i--) {

                if (stackFrames[i].contains(expr)  ) fromHere = true;
                if (fromHere)  partial.add(0,stackFrames[i]);
        }
        jss.setStackFrames(partial);
        return new JStackMetadataStack(mtd);
    }

	public JStackMetadataStack execute(JStackMetadataStack stck) {
		return map(stck);
	}
	
	public String getHelpLine() {
		return "Trim";
	}

}
