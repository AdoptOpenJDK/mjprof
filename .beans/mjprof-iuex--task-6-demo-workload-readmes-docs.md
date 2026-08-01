---
# mjprof-iuex
title: 'Task 6: demo workload, READMEs, docs'
status: completed
type: task
priority: normal
created_at: 2026-07-31T13:28:48Z
updated_at: 2026-08-01T07:20:44Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-9yi6
---

Plan Task 6.

- [x] DemoWorkload: daemon threads only, volatile stop flag, @PreDestroy interrupts, JIT-proof busy loop, demo.busy-threads configurable
- [x] Live verification: BLOCKED appeared (demo-lock-holder, demo-blocked-1); /hot ranked demo-busy-1/2 first (~51.8s vs ~2.25s)
- [x] mjprof-demo/README.md with an endpoint-to-capability table
- [x] Root README and CLAUDE.md cover fromSelf and the third module; beans prime line untouched
- [x] All six carried-over minors fixed
- [x] 140 tests (107 core, 19 cli, 14 demo)
- [x] Commit c006484

## Summary of Changes

The demo now has threads worth looking at, and the docs describe what was actually observed rather than what was hoped for — the implementer ran the app and recorded real endpoint output before writing the claims.

Reviewer confirmed no deadlock is possible (single lock, no nesting), that blocked threads park rather than spin, that the anti-dead-code-elimination idiom in the busy loop genuinely holds, and that DemoWorkload stays out of the @WebMvcTest slice so the endpoint tests do not depend on demo threads existing.
