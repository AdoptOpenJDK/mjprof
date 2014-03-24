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

package com.performizeit.mjstack.monads;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MJStep {
    String stepName;
    List<String> stepArgs;
    String stepVal;

    public MJStep(String stepString) {

        stepArgs = new ArrayList<String>();


        Pattern p = Pattern.compile("(.*)/(.*)/");
        Matcher m = p.matcher(stepString);
        if (m.find()) {
            stepName = m.group(1);
            MJStep mjStep = new MJStep(m.group(1));
            // System.out.println((m.group(1)));
            String params = m.group(2);
            if (params.trim().length() > 0) {
                params = params.replaceAll(",,", "__DOUBLE_COMMA__xxxxxxx");
                for (String q : params.split(",")) {
                    //    System.out.println(q);
                    addStepArg(q.replaceAll("__DOUBLE_COMMA__xxxxxxx", ","));
                }
            }

        } else {
            stepName = stepString;
        }

    }

    public String getStepName() {
        return stepName;
    }

    public List<String> getStepArgs() {
        return stepArgs;
    }

    public String getStepArg(int i) {
        return stepArgs.get(i);
    }

    public void addStepArg(String arg) {
        stepArgs.add(arg);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stepName).append("/");
        for (String stepV : stepArgs) {
            sb.append(stepV).append(",");
        }
        sb.append("/");
        return sb.toString();
    }
}
