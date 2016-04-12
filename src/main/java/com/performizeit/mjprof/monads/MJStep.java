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

package com.performizeit.mjprof.monads;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MJStep {
  String stepName;
  List<String> stepArgs;

  public MJStep(String stepString) {

    stepArgs = new ArrayList<String>();

    if (!stepString.contains("/")) {
      stepName = stepString;
    } else {
      int firstSlash = stepString.indexOf('/');
      int lastSlash = stepString.lastIndexOf('/');
      stepName = stepString.substring(0, firstSlash);
      if (firstSlash < lastSlash) {
        String params = stepString.substring(firstSlash + 1, lastSlash);
        if (params.trim().length() > 0) {
          params = params.replaceAll(",,", "__DOUBLE_COMMA__xxxxxxx");
          for (String q : params.split(",")) {
            addStepArg(q.replaceAll("__DOUBLE_COMMA__xxxxxxx", ","));
          }
        }
      }

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
