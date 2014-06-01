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

public interface ThreadInfoProps {
    public static final String NAME = "name";     // the name of the thread
    public static final String PRIO = "prio";
    public static final String CPU = "cpu";
    public static final String TID = "tid";
    public static final String NID = "nid";
    public static final String STACK = "stack";
    public static final String STATUS = "status";
    public static final String STATE = "state";
    public static final String LOS = "los";
    public static final String DAEMON = "daemon";
    public static final String COUNT = "count";
}