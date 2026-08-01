---
# mjprof-hrtl
title: 'Task 2: SelfDataSource + Pipeline.fromSelf'
status: completed
type: task
priority: normal
created_at: 2026-07-31T13:29:27Z
updated_at: 2026-07-31T14:02:02Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-cnq1
---

Plan Task 2.

- [x] SelfDataSourceTest written FIRST; compile failure captured before implementing
- [x] SelfDataSource over ThreadMXBean: NAME/TID(Long)/STATE/DAEMON/STACK, optional CPUNS, -1 never stored
- [x] No @Plugin; supported_monads.txt still 31, SelfDataSource absent
- [x] Date-prefixed header verified by writing two snapshots and reading back two dumps
- [x] fromSelf() / fromSelf(count,sleepMs) / fromSelfWithCpu(count,sleepMs) with eager validation
- [x] 125 tests (106 core, 19 cli)
- [x] Commit b94927d

## Summary of Changes

In-process source: no JMX connection, no network, no second process. Reviewer traced the counter logic against PipelineRunner and confirmed fromSelf(3,0) yields exactly 3 dumps, that the supplier resets state per collect(), that a dead thread appearing as null from dumpAllThreads is skipped, and that a -1 CPU reading is never stored.

Unlike the JMX source its header is date-prefixed, so written snapshots re-split instead of collapsing into one.

Three minors deferred to Task 6: a javadoc parity claim (the JMX source never sets DAEMON), an untested reuse path, and one live-CPU assertion.
