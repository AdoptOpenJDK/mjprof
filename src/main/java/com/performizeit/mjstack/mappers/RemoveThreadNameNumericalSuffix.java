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

package com.performizeit.mjstack.mappers;

import com.performizeit.mjstack.api.Mapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadInfo;
import  static com.performizeit.mjstack.parser.ThreadInfoProps.*;

@Plugin(name="namesuffix",paramTypes = {},
        description = "Trim the last number from thread names")
public class RemoveThreadNameNumericalSuffix implements Mapper {
    public RemoveThreadNameNumericalSuffix() {
    }

    @Override
    public ThreadInfo map(ThreadInfo stck) {
        String tName = (String)stck.getVal(NAME);
        int idx = tName.length()-1;
        for (;;idx--) {
            Character c = tName.charAt(idx);
            if (!Character.isDigit(c)) break;
        }
        tName = tName.substring(0,idx+1);
        stck.setVal(NAME,tName);
        return stck;
    }
}
