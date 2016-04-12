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

package com.performizeit.mjprof.parser;

import java.util.HashMap;
import java.util.Set;


public class Props {
  HashMap<String, Object> props;

  public Props(HashMap<String, Object> mtd) {
    props = mtd;
  }

  public Props() {
    props = new HashMap<>();
  }

  public HashMap<String, Object> cloneMetaData() {
    return (HashMap<String, Object>) props.clone();
  }

  public Object getVal(String key) {
    return props.get(key);
  }

  public Object setVal(String key, Object val) {
    return props.put(key, val);
  }

  public Set<String> getProps() {
    return props.keySet();
  }
}
