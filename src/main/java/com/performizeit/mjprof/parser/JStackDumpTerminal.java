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

import com.performizeit.mjprof.api.DumpMapper;
import com.performizeit.mjprof.api.Filter;
import com.performizeit.mjprof.api.Mapper;
import com.performizeit.mjprof.api.Terminal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Comparator;


public class JStackDumpTerminal extends ThreadDump {
    protected  String data;
    public ThreadDump filterDump(Filter filter) {
        throw new NotImplementedException();
    }
    public ThreadDump mapDump(Mapper mapper) {
        throw new NotImplementedException();
    }

    @Override
    public ThreadDump mapDump(DumpMapper mapper) {
        throw new NotImplementedException();
    }

    public ThreadDump sortDump(Comparator<ThreadInfo> comp) {
        throw new NotImplementedException();
    }

    @Override
    public ThreadDump terminateDump(Terminal terminal) {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return header+"\n\n"+data;
    }
}
