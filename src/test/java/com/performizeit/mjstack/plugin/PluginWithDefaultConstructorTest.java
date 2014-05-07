package com.performizeit.mjstack.plugin;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadInfo;
/*
 * Only for TEST! need to be in another project
 */
@Plugin(name="test1",paramTypes = {String.class})
public class PluginWithDefaultConstructorTest implements JStackMapper {
    private final String expr="kk";


    public ThreadInfo map(ThreadInfo stck) {
        return  stck;
    }

	public ThreadInfo execute(ThreadInfo stck) {
		return map(stck);
	}
	
	public String getHelpLine() {
		return "Trim";
	}

}
