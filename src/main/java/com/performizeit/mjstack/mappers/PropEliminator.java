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
import com.performizeit.mjstack.api.Attr;
import com.performizeit.mjstack.api.Mapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadInfo;

import java.util.HashMap;


@Plugin(name="eliminate", paramTypes={Attr.class},
        description = "Removes a certain attribute e.g. eliminate/stack/")
public class PropEliminator implements Mapper {
    private final String prop;

    public PropEliminator(Attr prop) {
        this.prop = prop.getAttrName();
    }
    @Override
    public ThreadInfo map(ThreadInfo stck) {

        HashMap<String,Object> mtd = stck.cloneMetaData();
        mtd.remove(prop);
        return      new ThreadInfo(mtd);

    }
}
