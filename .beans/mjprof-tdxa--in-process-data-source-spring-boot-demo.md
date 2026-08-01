---
# mjprof-tdxa
title: In-process data source + Spring Boot demo
status: completed
type: epic
priority: high
created_at: 2026-07-31T13:28:48Z
updated_at: 2026-08-01T07:52:31Z
---

Plan: docs/superpowers/plans/2026-07-31-self-datasource-and-spring-demo.md
Branch: self-datasource-and-demo

All seven tasks complete. 143 tests (109 core, 19 cli, 15 demo), from a 114 baseline.

## Summary of Changes

SelfDataSource snapshots this JVM's own threads via ThreadMXBean — no JMX connection, no network, no second process. API-only: un-annotated, so supported_monads.txt stays at 31 and the CLI never sees it.

mjprof-demo is a third module: a Spring Boot service whose seven endpoints analyse their own process. Verified live, not just by unit test — BLOCKED threads really appear, /hot really ranks the CPU burners first, and stack search really returns matches.

The final whole-branch review caught a Critical the six per-task reviews structurally could not, because it originated in the plan itself: getThreadCpuTime is cumulative, but jmxc stores a per-interval delta and ThreadInfoAggregator sums across samples, so /hot reported ~N x lifetime CPU and would rank a long-idle thread above one burning a core right now. Now mirrors jmxc exactly.

Also fixed: non-daemon threads round-tripped as daemons through the text serializer, which tested the property's presence rather than its value.

Tracked follow-ups: release build scoping, dumpAllThreads cost on large thread counts, the O(n^2) stack renderer, the hex/decimal tid asymmetry, native-method frame rendering, and a ponytail comment naming the wrong ceiling. Native image remains unverified for lack of GraalVM, though the branch provably cannot change its contents.
