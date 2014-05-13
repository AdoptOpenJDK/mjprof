/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjstack.mergers;

import com.performizeit.mjstack.api.Terminal;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfoProps;
import com.performizeit.mjstack.parser.ThreadInfo;


@Plugin(name="tree", paramTypes={},description="combine all stack traces ")
public class StackTreeMerger implements Terminal {
    Profile st = new Profile();
    public void addStackDump(ThreadDump jsd) {
        for (ThreadInfo mss : jsd.getStacks()  ) {
            st.addMulti((Profile) mss.getVal(ThreadInfoProps.STACK));      // System.out.println();
        }
    }

    @Override
    public String toString() {
        return st.toString();
    }
}
