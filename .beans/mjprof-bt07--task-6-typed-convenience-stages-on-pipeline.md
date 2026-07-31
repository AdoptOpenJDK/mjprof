---
# mjprof-bt07
title: 'Task 6: typed convenience stages on Pipeline'
status: completed
type: task
priority: normal
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T09:55:44Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-7j2x
---

Plan Task 6.

- [x] Grepped every plugin constructor signature before wrapping (evidence in report)
- [x] 17 typed convenience methods, each verified wired to the right plugin
- [x] tree() kept as chainable stage (round trip proven byte-identical on the real fixture)
- [x] treeText() added as the honest String terminal (owner decision: ship both)
- [x] PropComparator/ReversePropComparator sort absent attributes last in BOTH directions via protected compareNonNull (owner decision)
- [x] Restored sortDesc(Attr.COUNT) test + asc/desc null-sort regressions
- [x] tree() content-level test + test documenting that chaining an attribute filter after tree() drops the synthetic entry
- [x] ponytail: comment on Profile(String)'s "]\ " format sniffing
- [x] Golden: only 03-sort moved, as authorized; other nine byte-identical
- [x] Commits 427f5ac, 75db870

## Summary of Changes

Ergonomic layer over the plugins. Two things went beyond the original scope, both on the owner's call.

First, the plan was wrong: it assumed tree() could wrap StackTreeMerger as a dump-to-dump stage, but StackTreeMerger is a Terminal emitting a String. Shipping both forms — tree() as a chainable stage (rendering and re-parsing, verified lossless on the real fixture) and treeText() as the honest terminal.

Second, PropComparator had no null safety at all, so sorting on any attribute some threads lack threw NullPointerException. This was a pre-existing CLI crash (.sort/state/ throws on master) and it made the spec's own documented sortDesc(Attr.CPU_NS) examples unusable. Now absent values sort last in both directions. This deliberately changes CLI behavior and the 03-sort golden was re-baselined; the old NPE capture is preserved on disk.

95 tests (76 core, 19 cli).
