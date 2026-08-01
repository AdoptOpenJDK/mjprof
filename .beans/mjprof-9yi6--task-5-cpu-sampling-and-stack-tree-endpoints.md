---
# mjprof-9yi6
title: 'Task 5: CPU sampling and stack-tree endpoints'
status: completed
type: task
priority: normal
created_at: 2026-07-31T13:29:27Z
updated_at: 2026-08-01T07:11:12Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-zjpj
---

Plan Task 5.

- [x] Tests first
- [x] HotThread record
- [x] /hot: fromSelfWithCpu + merge(Attr.TID) + sortDesc(Attr.CPU_NS), bounded samples/sleepMs/top with 400
- [x] /tree: treeText() (not the round-tripping tree() stage), text/plain
- [x] Fix round 1: validState() helper shared by byState and tree so a typo'd state 400s on both; /hot javadoc documents the ~98s worst case and that concurrent callers each hold a request thread
- [x] 139 tests (106 core, 19 cli, 14 demo)
- [x] Commits 9711232, 172437e

## Summary of Changes

The two endpoints a single snapshot cannot support: a sampled CPU profile and the merged stack tree.

Reviewer verified the traps were avoided — merge on TID rather than STACK (which throws), reliance on nulls-sorting-last so a CPU-less thread cannot top the ranking, count() carrying the real sample count, and treeText() instead of the tree() stage that round-trips through rendered text.

Review found /tree returned 200 with an empty tree for a typo'd state while /state/{state} returned 400 for the same input. Now consistent via one shared validator.
