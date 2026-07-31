---
# mjprof-qukk
title: 'Task 7: fromDumps source for self-collected samples'
status: completed
type: task
priority: normal
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T10:04:16Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-7j2x
---

Plan Task 7.

- [x] PipelineFromDumpsTest failing first
- [x] DumpsDataSource: no @Plugin, snapshots its input
- [x] Pipeline.fromDumps(Collection<ThreadDump>)
- [x] merge(Attr.TID) sums cpu_ns across self-collected samples (asserted 60L from 10+20+30)
- [x] collect() twice gives the same result — required making Pipeline hold stage SUPPLIERS, since AccumulateDumps and TreeMergeStage are stateful
- [x] supported_monads.txt still 31 entries, no DumpsDataSource
- [x] Closed three deferred test gaps: multi-dump flattening, Attr.STACK in non-first varargs position, merge() chained with a later stage
- [x] Commit 523a49f

## Summary of Changes

Delivers the owner's actual request: sample on your own schedule, keep the ThreadDump objects, then merge and analyse later.

The deferred stage-sharing hazard turned out to be real, not theoretical. collect()-ing twice from a pipeline containing merge() double-counted, because Pipeline copied the stage list but shared the stage objects. Pipeline now stores stage factories and builds fresh instances per run. from(S) and the four map(P) overloads still share the caller's instance — unavoidable, and documented as single-use.

104 tests (85 core, 19 cli).
