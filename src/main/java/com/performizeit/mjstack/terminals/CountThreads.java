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

package com.performizeit.mjstack.terminals;

import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.JStackHeader;
import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by life on 28/2/14.
 */
public class CountThreads implements TerminalStep{
    int count =0;
    ArrayList<JStackHeader> stackDumpsHeaders = new ArrayList<JStackHeader>();


    public void addStackDump(JStackDump jsd) {
        for (JStackMetadataStack mss : jsd.getStacks()  ) {
                count++;
        }
    }

    @Override
    public String toString() {
        return "Total number of threads is "+ count + "\n";
    }
}
