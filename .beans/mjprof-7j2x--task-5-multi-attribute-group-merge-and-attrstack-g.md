---
# mjprof-7j2x
title: 'Task 5: multi-attribute group, merge, and Attr.STACK guard'
status: completed
type: task
priority: normal
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T09:32:58Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-b76p
---

Plan Task 5.

- [x] GroupByPropMultiAttrTest + PipelineGroupingTest failing first
- [x] GroupByProp(List<String>) added; (String) ctor kept so the group monad's reflective instantiation is untouched
- [x] Pipeline.group(Attr...) / merge() / merge(Attr)
- [x] group/merge reject Attr.STACK, message points at merge(Attr.TID).tree()
- [x] Golden 04-group byte-identical — CLI group output did not move
- [x] Commit 411668b

## Summary of Changes

Multi-attribute grouping via ThreadInfoAggregator's already-composite key (the monad only ever passed a one-element list), plus merge()/merge(Attr) over AccumulateDumps.

Attr.STACK is rejected rather than silently doing nothing: Profile/SFNode have no equals/hashCode so the aggregator keys stacks by identity, and value equality is not addable because the aggregator mutates profiles while merging.

Reviewer confirmed by reading PluginUtils.initObj that the CLI's reflective constructor lookup resolves only GroupByProp(String), so the List constructor is unreachable from the command line. 82 tests (63 core, 19 cli).

Two test-coverage minors deferred to Task 7: STACK guard untested in a non-first varargs position, and merge() untested when chained with a following stage.
