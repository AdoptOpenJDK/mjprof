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

package com.performizeit.mjstack.filters;

import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.parser.JStackMetadataStack;

/**
 * Created by life on 22/2/14.
 */
public class JStackFilterFieldContains implements JStackFilter {
    private final String fieldName;
    private final String valContained;

    public JStackFilterFieldContains(String fieldName, String valContained) {
        this.fieldName = fieldName;
        this.valContained = valContained;
    }

    @Override
    public boolean filter(JStackMetadataStack stck) {
        Object o = stck.getVal(fieldName);
        if (o == null) return false;
        return o.toString().contains(valContained);

    }
//TODO
	@Override
	public String getHelpLine() {
		// TODO Auto-generated method stub
		return null;
	}
}
