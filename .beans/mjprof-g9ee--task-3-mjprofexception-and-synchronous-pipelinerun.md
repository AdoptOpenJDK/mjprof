---
# mjprof-g9ee
title: 'Task 3: MjprofException and synchronous PipelineRunner'
status: completed
type: task
priority: normal
created_at: 2026-07-31T08:27:11Z
updated_at: 2026-07-31T09:09:40Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-ibp9
---

Plan Task 3.

- [x] PipelineRunnerTest failing first
- [x] MjprofException with (String) and (String,Throwable)
- [x] PipelineRunner: drive source, push through stages, null drops, handleDone fan-forward to LATER stages only
- [x] All five call sites wrap RuntimeException naming the failing class
- [x] sleepBetweenIteration only between dumps
- [x] Fix round 1: added two-accumulator test pinning the ascending handleDone walk (mutation-verified: fails when reversed, passes when ascending)
- [x] Commits 620cf50, 0b18f3b

## Summary of Changes

Synchronous, thread-free execution engine for the typed API. Same plugin objects the CLI runs through Pipe threads, folded on the calling thread instead: handleMsg per dump, then a handleDone walk whose output flows only to later stages. That walk is what makes AccumulateDumps (merge) work at all.

Review found the implementation correct but the regression net thin: reversing the handleDone loop still passed 7/7. Closed with a two-chained-accumulator test that discriminates the order, verified by actually mutating the loop. Matters because Task 5's merge() depends on that ordering.

PipelineRunnerTest 8/8; full suite 64 (45 core, 19 cli).
