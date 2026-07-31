---
# mjprof-g36v
title: 'Task 2: typed Attr constants and ThreadInfo accessors'
status: todo
type: task
created_at: 2026-07-31T08:27:11Z
updated_at: 2026-07-31T08:27:11Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-ibp9
---

Plan Task 2.

- [ ] AttrTest + ThreadInfoAccessorsTest failing first
- [ ] Attr.NAME/TID/NID/DAEMON/PRIO/STATUS/STATE/CPU_NS/WALL_MS/CPU_PCT/STACK/LOS/COUNT sourced from ThreadInfoProps
- [ ] ThreadInfo name()/state()/status()/tid()/cpuNs()/wallMs()/cpuPct()/count()/stack()/daemon(); count() defaults to 1
- [ ] Full suite green (Attr is used by CLI param conversion)
- [ ] Commit
