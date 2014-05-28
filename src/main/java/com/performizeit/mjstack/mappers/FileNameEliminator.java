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
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.ProfileVisitor;
import com.performizeit.mjstack.model.SFNode;
import com.performizeit.mjstack.parser.ThreadInfo;

import java.util.HashMap;

@Plugin(name="fnelim", params = {},
        description = "Eliminates file name from stack frames")
public class FileNameEliminator implements Mapper {


    public FileNameEliminator() {

    }

    @Override
    public ThreadInfo map(ThreadInfo stck) {
        HashMap<String,Object> mtd = stck.cloneMetaData();
        Profile jss = (Profile) stck.getVal("stack");
        jss.visit(new ProfileVisitor() {
            @Override
            public void visit(SFNode sf,int level) {
                if (sf.getStackFrame()==null) return;
                sf.setStackFrame(eliminatePackage(sf.getStackFrame()));
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
             String pkgClsMthd ;
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
            String pkg;
            if (pkgClsMthd.contains(".")) {   // class name contains package
                className = pkgClsMthd.substring(pkgClsMthd.lastIndexOf(".") + 1);
                pkg = pkgClsMthd.substring(0, pkgClsMthd.lastIndexOf("."))+".";
            } else {
                className =   pkgClsMthd;
                pkg = "";
            }
            return at + pkg+ className +"."+ method ;

    }
}
