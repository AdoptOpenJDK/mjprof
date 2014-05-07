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

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.ProfileVisitor;
import com.performizeit.mjstack.parser.ThreadInfo;
import  static com.performizeit.mjstack.parser.JStackProps.*;

@Plugin(name="pkgelim",paramTypes = {},
        description = "Eliminates package name from stack frames")
public class PackageEliminator implements  JStackMapper {
    @Override
    public ThreadInfo map(ThreadInfo stck) {
        Profile jss = (Profile) stck.getVal(STACK);
        jss.visit(new ProfileVisitor() {
            @Override
            public String visit(String sf,int level) {
                if (sf == null) return null;
                return eliminatePackage(sf);
            }
        });
        return stck;
    }

    static String eliminatePackage(String stackFrame) {
        if (stackFrame.trim().length() == 0) return stackFrame;
        int fnStart = stackFrame.indexOf("(");
        int atStart = stackFrame.indexOf("at ");
        if ( atStart < 0) return stackFrame;
        String fileName;
        String pkgClsMthd;
        if (fnStart<0 ) {
            fileName ="";
            pkgClsMthd = stackFrame.substring(atStart + 3);
        } else {
            fileName = stackFrame.substring(fnStart);
            pkgClsMthd = stackFrame.substring(atStart + 3, fnStart);
        }
        String at = stackFrame.substring(0, atStart + 3);

            String method = pkgClsMthd.substring(pkgClsMthd.lastIndexOf(".") + 1);
            pkgClsMthd = pkgClsMthd.substring(0, pkgClsMthd.lastIndexOf("."));
            String className;
            if (pkgClsMthd.contains(".")) {   // class name contains package
                className = pkgClsMthd.substring(pkgClsMthd.lastIndexOf(".") + 1);
            } else {
                className =   pkgClsMthd;
            }
            return at + className +"."+ method +  fileName;

    }
}
