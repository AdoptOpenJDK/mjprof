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

package com.performizeit.mjstack.parser;

import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.StackTrace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import  static com.performizeit.mjstack.parser.JStackProps.*;


public class ThreadInfo extends Props{





    public ThreadInfo(String stackTrace) {
        BufferedReader reader = new BufferedReader(new StringReader(stackTrace));
        try {
            String metaLine = reader.readLine();
            if (metaLine != null) {
                parseMetaLine(metaLine);

                String threadState = reader.readLine();
                if (threadState != null) {
                    parseThreadState(threadState);
                }
                String linesOfStack = "";
                String s;
                while ((s = reader.readLine()) != null) {
                    if (s.trim().length() == 0) break;
                    linesOfStack += s + "\n";
                }
                props.put(STACK, new Profile(linesOfStack));
                while ((s = reader.readLine()) != null) {
                   if (s.contains("Locked ownable synchronizers"))     break;
                }
                String linesOfLOS = "";
                while ((s = reader.readLine()) != null) {
                    if (s.trim().length() == 0) break;
                    linesOfLOS += s + "\n";
                }
                if (linesOfLOS.trim().length() > 0)
                    props.put(LOS, new JstackLockedOwbnableSynchronizers(linesOfLOS));
           }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ThreadInfo(HashMap<String, Object> mtd) {
        super(mtd);
    }



    private void parseThreadState(String threadState) {
        Pattern p = Pattern.compile("^[\\s]*java.lang.Thread.State: (.*)$");
        Matcher m = p.matcher(threadState);
        if (m.find()) {
            props.put(STATE, m.group(1));
        }
    }




    protected String metadataProperty(String metaLine,String propertyName){
        Pattern p = Pattern.compile(".* "+propertyName+"=([0-9a-fx]*) .*");
        Matcher m = p.matcher(metaLine);

        if (m.find()) {
            return  m.group(1);

        }
        return null;
    }
    protected void metadataKeyValProperties(String metaLine){
        Pattern p = Pattern.compile("(\\S+)=(\\S+)");    // a nonspace string then = and then a non space string
        Matcher m = p.matcher(metaLine);
        while (m.find()) {
            props.put( m.group(1),m.group(2));
        }
        if (props.get(TID) != null) {
            props.put(TID+"Long", new HexaLong((String)props.get(TID)));
        }
        if (props.get(NID) != null) {
            props.put(NID+"Long", new HexaLong((String) props.get(NID)));
        }
    }
    private void parseMetaLine(String metaLine) {
        Pattern p = Pattern.compile("^\"(.*)\".*");
        Matcher m = p.matcher(metaLine);

        if (m.find()) {
            props.put(NAME, m.group(1));
        }

        extractStatus(metaLine);
           metadataKeyValProperties(metaLine);

        if (metaLine.contains("\" "+DAEMON+" ")) {
            props.put(DAEMON, true);
        }

    }

    private void extractStatus(String metaLine) {
        int idx = metaLine.lastIndexOf('=');
        if (idx != -1) {
            String lastParam = metaLine.substring(idx);
            idx =         lastParam.indexOf(' ') ;
            if (idx != -1 ) {

                lastParam = lastParam.substring(idx+1);

                if (lastParam.length() > 0) {
                    props.put(STATUS, lastParam.trim());
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder mdStr = new StringBuilder();

        if (props.get(NAME) != null) {
            mdStr.append("\"" + props.get(NAME) + "\"");
        }
        if (props.get(DAEMON) != null) {
            mdStr.append(" "+DAEMON);
        }
        if (props.get(PRIO) != null) {
            mdStr.append(" "+PRIO+"=" + props.get(PRIO));
        }
        if (props.get(TID) != null) {
            mdStr.append(" "+TID+"=" + props.get(TID));
        }
        if (props.get(NID) != null) {
            mdStr.append(" "+NID+"=" + props.get(NID));
        }
        if (props.get(STATUS) != null) {
            mdStr.append(" " + props.get(STATUS));
        }


        if (props.get(STATE) != null) {
            mdStr.append("\n   java.lang.Thread.State: ").append( props.get(STATE));
        }
        if (props.get(STACK) != null) {
            mdStr.append("\n").append(props.get(STACK).toString()).append("\n");
        }
        if (props.get(LOS) != null) {
            mdStr.append("   Locked ownable synchronizers:\n").append(
                    props.get(LOS).toString());
        }
        return mdStr.toString();

    }

}
