package com.performizeit.mjprof.model;

import com.performizeit.mjprof.parser.ThreadInfo;

import java.util.*;

import static com.performizeit.mjprof.parser.ThreadInfoProps.*;

public class ThreadInfoAggregator {
  private final List<String> props;
  private final Set<String> propsMap = new HashSet<>();
  private final Map<List<Object>, ThreadInfo> aggregator = new HashMap<>();

  public ThreadInfoAggregator(List<String> props) {
    this.props = props;
    propsMap.addAll(props);
  }

  public void accumulateThreadInfo(ThreadInfo threadInfo) {
    var key = generateKey(threadInfo);
    ThreadInfo target = aggregator.get(key);
    if (target == null) {
      aggregator.put(key, threadInfo);
    } else {
      mergeProfiles(target, threadInfo);
      mergeCounts(target, threadInfo);
      mergeCPU(target, threadInfo);
      mergeNonKeyProps(target, threadInfo);
    }
  }

  public ArrayList<ThreadInfo> getAggrInfos() {
    return new ArrayList<>(aggregator.values());
  }

  private void mergeCounts(ThreadInfo target, ThreadInfo threadInfo) {
    Integer countTarget = (Integer) target.getVal(COUNT);
    Integer count = (Integer) threadInfo.getVal(COUNT);
    if (count == null) count = 1;
    if (countTarget == null) countTarget = 1;
    target.setVal(COUNT, countTarget + count);
  }

  private double percentDouble(long nom, long denom) {
    return ((double) (nom * 100 * 100 / denom)) / 100.0;
  }

  private void mergeCPU(ThreadInfo target, ThreadInfo threadInfo) {
    Long cpuTarget = (Long) target.getVal(CPUNS);
    Long cpu = (Long) threadInfo.getVal(CPUNS);
    
    if (cpu != null && cpuTarget != null) { // both have CPU data
      target.setVal(CPUNS, cpuTarget + cpu);
    }

    Long wallTarget = (Long) target.getVal(WALL);
    Long wall = (Long) threadInfo.getVal(WALL);
    
    if (wall != null || wallTarget != null) {
      if (wall == null) wall = 0L;
      if (wallTarget == null) wallTarget = 0L;
      if (wallTarget < wall) wallTarget = wall;
      target.setVal(WALL, wallTarget);
    }
    
    if ((wall != null || wallTarget != null) && 
        cpu != null && cpuTarget != null && wallTarget > 0) {
      target.setVal(CPU_PREC, percentDouble(
          cpuTarget, 
          (wallTarget * 1000 * 1000)
      ));
    }
  }

  private void mergeProfiles(ThreadInfo target, ThreadInfo threadInfo) {
    Profile targetProfile = (Profile) target.getVal(STACK);
    Profile prof = (Profile) threadInfo.getVal(STACK);
    
    if (prof == null) return;
    if (targetProfile == null) {
      target.setVal(STACK, prof);
      return;
    }
    targetProfile.addMulti(prof);
  }

  public void mergeNonKeyProps(ThreadInfo target, ThreadInfo threadInfo) {
    ArrayList<String> keys = new ArrayList<>(threadInfo.getProps());
    for (String prop1 : keys) {
      if (propsMap.contains(prop1)) continue; // key properties are not merged
      if (prop1.equals(STACK) || prop1.equals(COUNT) || prop1.equals(CPUNS) ||
          prop1.equals(WALL) || prop1.equals(CPU_PREC))
        continue; // already merged specially

      Object valTarget = target.getVal(prop1);
      Object val = threadInfo.getVal(prop1);
      if (valTarget == null) {
        target.setVal(prop1, val);
      } else if (val != null && !val.equals(valTarget)) {   // we have more than one
        if (prop1.equals(DAEMON)) {
          target.setVal(prop1, false);
        } else if (prop1.equals(LOS)) {
          target.setVal(prop1, "\t- *");
        } else {
          target.setVal(prop1, "*");
        }
      }
    }
  }

  ArrayList<Object> generateKey(ThreadInfo threadInfo) {
    ArrayList<Object> key = new ArrayList<>();
    for (String prop : props) {
      key.add(threadInfo.getVal(prop));
    }
    return key;
  }
}
