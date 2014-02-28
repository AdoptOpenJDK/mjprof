package com.performizeit.mjstack.mappers;

import com.performizeit.mjstack.parser.JStackMetadataStack;
import com.performizeit.mjstack.parser.JStackStack;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by life on 28/2/14.
 */
public class TrimBottom implements  JStackMapper {
    private final int count;

    public TrimBottom(int count) {
        this.count = count;
    }

    @Override
    public JStackMetadataStack map(JStackMetadataStack stck) {
        HashMap<String,Object> mtd = stck.cloneMetaData();
        JStackStack jss = (JStackStack) mtd.get("stack");
        String[] stackFrames = jss.getStackFrames();
        String[] partial = {};
        if (count < stackFrames.length)   {
            partial = Arrays.copyOfRange(stackFrames, count, stackFrames.length-1);
        }
        jss.setStackFrames(partial);
        return      new JStackMetadataStack(mtd);
    }
}
