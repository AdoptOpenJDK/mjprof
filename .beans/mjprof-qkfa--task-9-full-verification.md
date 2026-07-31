---
# mjprof-qkfa
title: 'Task 9: full verification'
status: todo
type: task
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T08:27:33Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-6zfz
---

Plan Task 9. Report only what actually ran.

- [ ] mvn clean package green, record test count
- [ ] Golden diff for all 10 expressions
- [ ] supported_monads.txt: 31 lines, all under core.plugins, at jar root
- [ ] mvn -pl mjprof-core dependency:tree shows no compile-scope third-party dep
- [ ] Native build: mvn -Pnative package -pl mjprof-cli -am, binary runs. If no GraalVM, say skipped, do not claim verified.
