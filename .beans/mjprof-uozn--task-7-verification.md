---
# mjprof-uozn
title: 'Task 7: verification'
status: completed
type: task
priority: normal
created_at: 2026-07-31T13:29:27Z
updated_at: 2026-08-01T07:25:40Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-iuex
---

Plan Task 7 — verification only, no source changes.

- [x] mvn clean package: BUILD SUCCESS, 140 tests (107 core, 19 cli, 14 demo)
- [x] CLI golden gate 10/10 OK (03-sort compared against its authorized re-baseline)
- [x] supported_monads.txt exactly 31, no SelfDataSource or DumpsDataSource
- [x] mjprof-core: only test-scope junit, zero springframework; mjprof-demo: 19 springframework hits
- [x] CLI end to end, including the null-safe sort, no stack traces
- [x] All seven demo endpoints returned real data; NOTASTATE returned 400; graceful shutdown
- [x] /search?frame=DemoWorkload returned 6 matches — the weak unit test's gap covered empirically
- [x] Native SKIPPED: no GraalVM installed, none installed

## Summary of Changes

None — measurement only. The result that matters: the demo genuinely analyses its own process over HTTP, and stack search really works, which no unit test proves.
