---
# mjprof-uozn
title: 'Task 7: verification'
status: todo
type: task
priority: normal
created_at: 2026-07-31T13:29:27Z
updated_at: 2026-07-31T13:29:38Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-iuex
---

Plan Task 7. No source changes; measure and report honestly.

- [ ] mvn clean package, per-module test counts
- [ ] CLI golden gate 10/10 (03-sort vs its re-baseline)
- [ ] supported_monads.txt still 31, no SelfDataSource/DumpsDataSource
- [ ] Spring only under mjprof-demo; core dependency-free
- [ ] native profile unaffected (SKIPPED if no GraalVM — do not install)
- [ ] Demo runs over HTTP; paste real responses
