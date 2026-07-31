---
# mjprof-b76p
title: 'Task 4: Pipeline builder - sources, generic map, terminals'
status: todo
type: task
priority: normal
created_at: 2026-07-31T08:27:11Z
updated_at: 2026-07-31T08:27:45Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-g9ee
    - mjprof-g36v
---

Plan Task 4. Also depends on Task 2 for Attr.

- [ ] PipelineTest failing first (8 tests)
- [ ] from/fromFile/fromStdin/fromJmx/fromJmxWithCpu via lazy Supplier so a Pipeline is reusable
- [ ] Four map overloads on intersection bounds (SingleThreadMapper/DumpReducer/Filter/ThreadInfoComparator & PipeHandler)
- [ ] VERIFY javac accepts the overloads; fallback is distinct names (mapThread/reduce/filter/sortBy) propagated to Tasks 5-7
- [ ] collect/collectOne/threads
- [ ] Commit
