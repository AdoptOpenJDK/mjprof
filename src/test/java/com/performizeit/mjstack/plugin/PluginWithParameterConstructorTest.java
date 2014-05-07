package com.performizeit.mjstack.plugin;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.ProfileNodeFilter;
import com.performizeit.mjstack.model.SFNode;
import com.performizeit.mjstack.parser.ThreadInfo;
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
        Profile jss = (Profile) mtd.get("stack");

        jss.filter(new ProfileNodeFilter() {

            @Override
            public boolean accept(SFNode node, int level, Object context) {
                if (node.getStackFrame() == null) return true;
                return node.getStackFrame().contains("lock");
            }
        },null);
        return stck;
    }

	public ThreadInfo execute(ThreadInfo stck) {
		return map(stck);
	}

	public String getHelpLine() {
		return "TrimBelowhelp";
	}
}
