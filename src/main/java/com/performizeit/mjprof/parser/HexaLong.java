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


public class HexaLong implements Comparable<HexaLong> {
  long value;

  public HexaLong(long val) {
    value = val;
  }

  public HexaLong(String s) throws NumberFormatException {
    if (s.startsWith("0x")) {
      s = s.substring(2);
    }
    value = Long.parseLong(s, 16);

  }

  @Override
  public String toString() {
    return String.format("%x", value);
  }

  @Override
  public int compareTo(HexaLong o) {
    return Long.compare(value, o.value);
  }

  public long getValue() {
    return value;
  }
}
