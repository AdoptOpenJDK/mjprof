---
# mjprof-qkfa
title: 'Task 9: full verification'
status: completed
type: task
priority: normal
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T10:23:18Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-6zfz
---

Plan Task 9 — verification only, no source changes.

- [x] mvn clean package: BUILD SUCCESS
- [x] 106 tests pass (87 core, 19 cli)
- [x] Golden gate 10/10 OK (03-sort compared against its authorized re-baseline)
- [x] supported_monads.txt: 31 entries, all under core.plugins, at jar root, no DumpsDataSource
- [x] mjprof-core dependency:tree: only test-scope junit, no compile-scope third party
- [x] Fat jar runs real expressions including the now null-safe sort
- [x] Typed API smoke test compiled and ran against mjprof-core/target/classes
- [x] Native build SKIPPED — no GraalVM on this machine

## Summary of Changes

No code. Measurement only.

The check worth recording: a throwaway program compiled against mjprof-core and ran the API end to end — Pipeline.fromFile -> contains(STATE,RUNNABLE) -> group(STATE) printed state=RUNNABLE count=6, and fromDumps + merge(TID) folded two hand-built dumps into one. No golden file covers the API, so this is the only direct evidence the branch's purpose works.

Native image remains unverified: no GraalVM is installed and installing one was out of scope. That is the single outstanding gap for merge.
