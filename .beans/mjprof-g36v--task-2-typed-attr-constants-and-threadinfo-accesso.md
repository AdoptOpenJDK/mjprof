---
# mjprof-g36v
title: 'Task 2: typed Attr constants and ThreadInfo accessors'
status: completed
type: task
priority: normal
created_at: 2026-07-31T08:27:11Z
updated_at: 2026-07-31T08:56:35Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-ibp9
---

Plan Task 2.

- [x] AttrTest + ThreadInfoAccessorsTest failing first
- [x] 13 Attr constants sourced from ThreadInfoProps
- [x] ThreadInfo typed accessors; count() defaults to 1
- [x] stack() returns Profile (controller decision; ThreadDump already imports core.model)
- [x] Full suite green: 56 tests (37 core, 19 cli)
- [x] Commits 6cecdec, 36d8820

## Summary of Changes

Added Attr.NAME..COUNT constants and ThreadInfo name()/state()/status()/tid()/cpuNs()/wallMs()/cpuPct()/count()/stack()/daemon().

Review found one Important defect, fixed in round 1: the numeric accessors cast unconditionally, but JStackTextualFormatParser.metadataKeyValProperties stores every key=value header pair as a String, and toJStackFormat writes count/cpu_ns/wall_ms/%cpu into that line — so any dump rendered to text and re-parsed threw ClassCastException. Accessors now coerce Number|String and throw NumberFormatException on non-numeric input rather than returning a silent null.

Side effect worth recording: this disproved the spec's claim that cpu_ns is lost on a text round trip. Measured — all four numeric props survive. The spec has been corrected.
