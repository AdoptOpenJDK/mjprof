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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by life on 23/2/14.
 */
public class JStackMetadataStack {
    HashMap<String, Object> metaData = new HashMap<String, Object>();


    public JStackMetadataStack(String stackTrace) {
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
                metaData.put("stack", new JStackStack(linesOfStack));


                while ((s = reader.readLine()) != null) {
                   if (s.contains("Locked ownable synchronizers"))     break;

                }

                String linesOfLOS = "";
                while ((s = reader.readLine()) != null) {
                    if (s.trim().length() == 0) break;
                    linesOfLOS += s + "\n";
                }
                if (linesOfLOS.trim().length() > 0)
                    metaData.put("los", new JstackLockedOwbnableSynchronizers(linesOfLOS));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(this.toString());
  //      System.out.println(stackTrace);


    }
    public JStackMetadataStack(HashMap <String,Object> mtd) {
        metaData =mtd;
    }
    public HashMap<String, Object> cloneMetaData() {
        return (HashMap<String, Object>) metaData.clone();
    }

    private void parseThreadState(String threadState) {
        Pattern p = Pattern.compile("^[\\s]*java.lang.Thread.State: (.*)$");
        Matcher m = p.matcher(threadState);
        if (m.find()) {
            metaData.put("state", m.group(1));
        }
    }
    public Object getVal(String key) {
        return metaData.get(key);
    }

    private void parseMetaLine(String metaLine) {
        Pattern p = Pattern.compile("^\"(.*)\".* prio=(\\d*) tid=([0-9a-fx]*) nid=([0-9a-fx]*) (.*)");
        Matcher m = p.matcher(metaLine);

        if (m.find()) {
            metaData.put("name", m.group(1));
            metaData.put("prio", Integer.parseInt(m.group(2)));
            metaData.put("tid",  new HexaLong(m.group(3)));
            metaData.put("tidstr",  m.group(3));
            metaData.put("nid", new HexaLong(m.group(4)));
            metaData.put("nidstr", m.group(4));
            metaData.put("status", m.group(5));
        } else {
            System.out.println("not found" + metaLine);
        }
        if (metaLine.contains("\" daemon ")) {
            metaData.put("daemon", true);
        }

    }

    @Override
    public String toString() {
        String daemon = "";
        if (metaData.get("daemon") != null) daemon = " daemon";
        String str = "\"" + metaData.get("name") + "\"" + daemon +
                " prio=" + metaData.get("prio")
                + " tid=" + metaData.get("tidstr")
                + " nid=" + metaData.get("nidstr")
                + " " + metaData.get("status") + "\n";

        if (metaData.get("state") != null) {
            str += "   java.lang.Thread.State: " + metaData.get("state") + "\n";
        }
        if (metaData.get("stack") != null) {
            str += metaData.get("stack").toString() + "\n";
        }
        if (metaData.get("los") != null) {
            str += "   Locked ownable synchronizers:\n";
            str += metaData.get("los").toString();
        }
        return str;

    }

    public Set<String> getProps() {
        return metaData.keySet();
    }
}
