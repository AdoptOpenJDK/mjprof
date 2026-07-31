---
# mjprof-6zfz
title: 'Task 8: document the API and pin text-persistence limits'
status: todo
type: task
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T08:27:33Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-qukk
---

Plan Task 8.

- [ ] TextRoundTripLimitsTest: name/state survive, cpu_ns is LOST (toJStackFormat writes it, parser never reads it back)
- [ ] Pipeline javadoc: blocking on caller thread; text round-trip loses count/cpu_ns/wall_ms/%cpu; JMX headers are not date-prefixed so concatenated JMX dumps do not re-split
- [ ] README: Java API section with dependency coords and three examples
- [ ] CLAUDE.md: two modules, new package names, -pl test commands, two runtimes (threaded Pipe for CLI, synchronous runner for API), core stays dependency-free, codegen now in mjprof-cli
- [ ] Commit
