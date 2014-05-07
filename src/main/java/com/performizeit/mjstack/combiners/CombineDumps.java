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

package com.performizeit.mjstack.combiners;

import com.performizeit.mjstack.api.DumpCombiner;
import com.performizeit.mjstack.api.DumpMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.ThreadInfo;

import java.util.ArrayList;
import java.util.HashMap;

import static com.performizeit.mjstack.parser.JStackProps.*;

/**
 * Created by life on 28/2/14.
 */
@Plugin(name="merge", paramTypes={String.class},description="combine all dumps to a single one")
public class CombineDumps implements DumpCombiner {
    public CombineDumps() {
    }
    public JStackDump  map(JStackDump jsd1,JStackDump jsd2 ) {
        HashMap<Long,ThreadInfo> threadMap = new HashMap<Long, ThreadInfo>();
        for (ThreadInfo mss : jsd1.getStacks()  ) {
          long a = (Long)mss.getVal(TID+"Long");
        }

        return jsd1;
    }


}
