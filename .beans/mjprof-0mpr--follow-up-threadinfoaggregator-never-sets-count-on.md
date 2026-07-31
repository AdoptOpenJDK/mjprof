---
# mjprof-0mpr
title: 'Follow-up: ThreadInfoAggregator never sets count on singleton groups'
status: todo
type: bug
created_at: 2026-07-31T09:48:46Z
updated_at: 2026-07-31T09:48:46Z
parent: mjprof-6l4o
---

Found during Task 6 of the core/cli split.

ThreadInfoAggregator.mergeCounts (model/ThreadInfoAggregator.java:32-38) only runs in the else branch of accumulateThreadInfo — i.e. when a second entry collides with an existing key. A group with exactly one member is inserted as-is and never gets a 'count' property at all.

Consequence: after group(...), some entries have count and some do not. ThreadInfo.count() papers over it by defaulting to 1, but anything reading the raw prop (sorting, output formatting, JSON serialization) sees a hole. The same conditional-population applies to cpu_ns, wall_ms and %cpu.

The immediate crash this caused (sorting on an absent attribute) is fixed separately by making PropComparator sort nulls last. This bean is the underlying data-model inconsistency: should a singleton group carry count=1 explicitly?

Not urgent — nothing is broken once the comparator is null-safe. Worth deciding before a JSON API exposes these props.
