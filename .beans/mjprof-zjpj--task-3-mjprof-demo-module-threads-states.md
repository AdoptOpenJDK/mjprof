---
# mjprof-zjpj
title: 'Task 3: mjprof-demo module + /threads + /states'
status: todo
type: task
priority: normal
created_at: 2026-07-31T13:28:48Z
updated_at: 2026-07-31T13:29:38Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-hrtl
---

Plan Task 3. Spring Boot 3.5.16 via BOM import (cannot use Boot starter parent — reactor parent is com.performizeit:mjprof).

- [ ] Add module to root pom
- [ ] mjprof-demo/pom.xml, depends on mjprof-core only
- [ ] MjprofDemoApplication, ThreadView, GroupCount, ThreadViewMapper, ThreadEndpoints
- [ ] @WebMvcTest over /api/threads and /api/threads/states
- [ ] Report resolved junit version (parent imports junit-bom 5.12.2, Boot brings its own)
- [ ] Commit
