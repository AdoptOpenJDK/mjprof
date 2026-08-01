---
# mjprof-zjpj
title: 'Task 3: mjprof-demo module + /threads + /states'
status: completed
type: task
priority: normal
created_at: 2026-07-31T13:28:48Z
updated_at: 2026-07-31T14:10:08Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-hrtl
---

Plan Task 3.

- [x] Root pom gains mjprof-demo
- [x] mjprof-demo/pom.xml: Boot 3.5.16 via BOM import, mjprof-core only, explicit plugin version
- [x] MjprofDemoApplication, ThreadView, GroupCount, ThreadViewMapper, ThreadEndpoints
- [x] @WebMvcTest over /api/threads and /api/threads/states
- [x] 128 tests (106 core, 19 cli, 3 demo); supported_monads.txt still 31
- [x] Single JUnit 5.12.2 resolved, no conflict
- [x] Commit ac9ae5b

## Summary of Changes

Third module: Spring Boot service analysing its own process through Pipeline.fromSelf(). Endpoints return DTO records, never ThreadInfo, which is a property bag Jackson would mangle.

The plan had a real gap: it omitted -parameters, which spring-boot-starter-parent normally supplies and which this module cannot inherit because the reactor parent is com.performizeit:mjprof. Without it @RequestParam int frames fails to bind at runtime. Fixed at root cause with one property, scoped to the demo pom so core and cli are not recompiled with different flags.

Reviewer verified the histogram reports 1 for single-thread states (ThreadInfoAggregator only writes count on a collision, and count() defaults to 1), that no request shares Pipeline state, and that the frame-splitting approach yields top-of-stack frames first.
