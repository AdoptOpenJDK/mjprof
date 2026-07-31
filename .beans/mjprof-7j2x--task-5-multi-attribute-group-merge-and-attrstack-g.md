---
# mjprof-7j2x
title: 'Task 5: multi-attribute group, merge, and Attr.STACK guard'
status: todo
type: task
created_at: 2026-07-31T08:27:33Z
updated_at: 2026-07-31T08:27:33Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-b76p
---

Plan Task 5.

- [ ] GroupByPropMultiAttrTest + PipelineGroupingTest failing first
- [ ] GroupByProp(List<String>) added; (String) constructor kept so the group monad and its reflective instantiation are untouched
- [ ] Pipeline.group(Attr...) / merge() / merge(Attr)
- [ ] group/merge reject Attr.STACK: Profile has no value equality (aggregator mutates profiles, so it cannot be a hash key). Message points at merge(Attr.TID).tree()
- [ ] Re-diff golden 04-group: group monad output must be unchanged
- [ ] Commit
