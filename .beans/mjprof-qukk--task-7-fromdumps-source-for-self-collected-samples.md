---
# mjprof-qukk
title: 'Task 7: fromDumps source for self-collected samples'
status: todo
type: task
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T08:27:33Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-7j2x
---

Plan Task 7. Covers the requirement to collect dumps on your own schedule and merge them later.

- [ ] PipelineFromDumpsTest failing first (6 tests)
- [ ] DumpsDataSource: package-private, NO @Plugin annotation, List.copyOf snapshot
- [ ] Pipeline.fromDumps(Collection<ThreadDump>)
- [ ] merge(Attr.TID) sums cpu_ns across samples and sets count
- [ ] collect() twice must give the same result; if a stateful stage breaks that, switch Pipeline to hold stage suppliers
- [ ] Confirm supported_monads.txt still has 31 entries and no DumpsDataSource
- [ ] Commit
