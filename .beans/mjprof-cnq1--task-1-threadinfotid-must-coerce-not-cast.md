---
# mjprof-cnq1
title: 'Task 1: ThreadInfo.tid() must coerce, not cast'
status: completed
type: task
priority: normal
created_at: 2026-07-31T13:28:48Z
updated_at: 2026-07-31T13:35:06Z
parent: mjprof-tdxa
---

Plan Task 1.

- [x] Three failing tests first (Long / String / absent)
- [x] tid() coerces via toString()
- [x] Full suite green: 114 tests (95 core, 19 cli)
- [x] Commit 61dbfbb

## Summary of Changes

tid() cast its value to String, but JmxDataSourcePlugin stores the thread id as a Long, so it threw ClassCastException on every JMX-sourced dump. Now coerces. Prerequisite for SelfDataSource (also stores a Long) and the demo's DTO mapper.

Review: spec met, approved. Two minors recorded, one of which (state() being equally unsafe) was adjudicated a false positive — JMX exposes threadState as a String.
