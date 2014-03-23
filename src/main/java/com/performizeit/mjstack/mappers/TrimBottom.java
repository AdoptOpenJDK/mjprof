package com.performizeit.mjstack.mappers;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.parser.JStackMetadataStack;
import com.performizeit.mjstack.parser.JStackStack;

import java.util.Arrays;
import java.util.HashMap;


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
        if (count < stackFrames.length)   {
            String[] partial = Arrays.copyOfRange(stackFrames, 0, count);
            jss.setStackFrames(partial);
        }

        return      new JStackMetadataStack(mtd);
    }
}
