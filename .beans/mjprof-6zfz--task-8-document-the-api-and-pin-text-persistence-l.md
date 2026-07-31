---
# mjprof-6zfz
title: 'Task 8: document the API and pin text-persistence limits'
status: completed
type: task
priority: normal
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T10:19:30Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-qukk
---

Plan Task 8.

- [x] TextRoundTripLimitsTest pins dump SPLITTING (numeric fidelity already covered by ThreadInfoAccessorsTest) — confirmed JMX-style headers collapse to one dump
- [x] Pipeline javadoc: blocking on caller thread, reusability rule, corrected round-trip facts
- [x] README Java API section, every code example checked against the real Pipeline signatures
- [x] CLAUDE.md updated for two modules, renamed packages, two runtimes, corrected jar/main class
- [x] Documented the CLI behavior change: sort/ and -sort/ no longer crash on a missing attribute
- [x] Fix round 1: corrected the StepsRepository path claim (file had contradicted itself) and the broken single-module test command
- [x] Commits 1ccd508, 6930b57

## Summary of Changes

Docs now describe what exists rather than what the plan imagined, including the several things that changed under review: treeText(), null-safe sorting, guarded JMX, the stage-freshness rule and its from(S)/map(P) exception.

Two notes. A harness security warning claimed the 'beans prime' directive in CLAUDE.md was injected — investigated and dismissed as a false positive: it comes from upstream commit c471474 and this task never touched the line. And the single-module test command the docs first recommended did not actually run; the working form needs both -am and -Dsurefire.failIfNoSpecifiedTests=false, verified by running all three variants.

106 tests (87 core, 19 cli).
