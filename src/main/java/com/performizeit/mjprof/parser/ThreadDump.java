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

import com.performizeit.mjprof.model.JStackHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a thread dump consisting of a header and a list of thread information.
 * Updated to use Java 17 features and flexible list handling.
 */
public class ThreadDump {
  protected JStackHeader header;
  protected ArrayList<ThreadInfo> threadInfos = new ArrayList<>();
  protected int JNIglobalReferences = -1;

  public static final String JNI_GLOBAL_REFS = "JNI global references:";

  public JStackHeader getHeader() {
    return header;
  }

  public ThreadDump(JStackHeader header, ArrayList<ThreadInfo> threadInfos, int JNIglobalReferences) {
    this.header = header;
    this.threadInfos = threadInfos;
    this.JNIglobalReferences = JNIglobalReferences;
  }
  
  public ThreadDump(JStackHeader header, ArrayList<ThreadInfo> threadInfos) {
    this.header = header;
    this.threadInfos = threadInfos;
  }
  
  public ThreadDump(JStackHeader header, List<ThreadInfo> threadInfos) {
    this.header = header;
    this.threadInfos = new ArrayList<>(threadInfos);
  }

  public ThreadDump() {
    // Default constructor
  }

  public ArrayList<ThreadInfo> getThreadInfos() {
    return threadInfos;
  }

  public void addThreadInfo(ThreadInfo ti) {
    threadInfos.add(ti);
  }

  public void setThreadInfos(ArrayList<ThreadInfo> threadInfos) {
    this.threadInfos = threadInfos;
  }
  
  public void setThreadInfos(List<ThreadInfo> threadInfos) {
    this.threadInfos = new ArrayList<>(threadInfos);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(header).append("\n\n");

    for (ThreadInfo stack : threadInfos) {
      s.append(stack.toString()).append("\n");
    }

    return s.toString();
  }


  public void setHeader(String header) {
    this.header = new JStackHeader(header);
  }

  public void setHeader(JStackHeader header) {
    this.header = header;
  }

  public ArrayList<ThreadInfo> cloneStacks() {
    ArrayList<ThreadInfo> newStacks = new ArrayList<>();
    newStacks.addAll(getThreadInfos());
    return newStacks;
  }

  public int getJNIglobalReferences() {
    return JNIglobalReferences;
  }

  public void setJNIglobalReferences(int JNIglobalReferences) {
    this.JNIglobalReferences = JNIglobalReferences;
  }
}
