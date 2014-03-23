package com.performizeit.mjstack.mappers;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.HashMap;

/**
 * Created by life on 23/2/14.
 */
@Plugin
public class PropEliminator implements JStackMapper {
    private final String prop;

    public PropEliminator(String prop) {
        this.prop = prop;
    }
    @Override
    public JStackMetadataStack map(JStackMetadataStack stck) {

        HashMap<String,Object> mtd = stck.cloneMetaData();
        mtd.remove(prop);
        return      new JStackMetadataStack(mtd);

    }
    
   public String getHelpLine(){
    	return "prop";
    }
}
