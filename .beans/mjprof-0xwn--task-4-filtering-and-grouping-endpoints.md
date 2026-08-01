---
# mjprof-0xwn
title: 'Task 4: filtering and grouping endpoints'
status: completed
type: task
priority: normal
created_at: 2026-07-31T13:29:27Z
updated_at: 2026-07-31T14:15:59Z
parent: mjprof-tdxa
blocked_by:
    - mjprof-zjpj
---

Plan Task 4.

- [x] Tests first; pre-implementation run failed 6 of 9 as expected
- [x] /api/threads/state/{state} with Locale.ROOT uppercase and 400 on an unknown state
- [x] /api/threads/pools via withoutNameSuffix() + group(Attr.NAME)
- [x] /api/threads/search via contains(Attr.STACK, ...) with 400 on blank
- [x] 134 tests (106 core, 19 cli, 9 demo)
- [x] Commit ca56b76

## Summary of Changes

Three endpoints, each demonstrating a different API capability. Reviewer verified the state filter uppercases with Locale.ROOT (the default-locale variant is a real bug in Turkish locales), that pools reports 1 for singleton groups, that contains() on a stack is a substring match and legitimately allowed where group(Attr.STACK) is not, and that no request shares Pipeline state.

Two minors, both traceable to the plan's own text rather than the implementation: a javadoc claim about showing frames around the match (the mapper takes the top N), and a search test weak enough to pass if the filter always returned empty.
