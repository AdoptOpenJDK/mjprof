---
# mjprof-hrtl
title: 'Task 2: SelfDataSource + Pipeline.fromSelf'
status: in-progress
type: task
priority: normal
created_at: 2026-07-31T13:29:27Z
updated_at: 2026-07-31T13:35:06Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-cnq1
---

Plan Task 2. Tests BEFORE implementation — explicitly requested.

- [ ] SelfDataSourceTest written first (11 tests), watch it fail to compile
- [ ] SelfDataSource over ThreadMXBean: NAME/TID(Long)/STATE/DAEMON/STACK, optional CPUNS
- [ ] NO @Plugin annotation; supported_monads.txt stays 31
- [ ] Date-prefixed header so written snapshots re-split (unlike the JMX source)
- [ ] fromSelf() / fromSelf(count,sleepMs) / fromSelfWithCpu(count,sleepMs)
- [ ] Commit
