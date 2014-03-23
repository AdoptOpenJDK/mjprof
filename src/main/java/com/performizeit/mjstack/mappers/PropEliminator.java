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
import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.HashMap;

/**
 * Created by life on 23/2/14.
 */
@Plugin(name="elimprop", paramTypes={String.class},
        description = "Removes a certain attribute e.g. eliminate/stack/")
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
}
