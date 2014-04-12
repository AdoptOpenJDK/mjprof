/*
       This file is part of mjstack.

        mjstack is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjstack is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjstack.mappers;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadInfo;
import com.performizeit.mjstack.parser.StackTrace;

import java.util.ArrayList;
import java.util.HashMap;

@Plugin(name="trimbelow",paramTypes = {String.class},
        description = "Trim all stack frames below the first occurance of string")
public class TrimBelow implements  JStackMapper {
    private final String expr;

    public TrimBelow(String expr) {
        this.expr = expr;

    }

    @Override
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

        return      new ThreadInfo(mtd);
    }
}
