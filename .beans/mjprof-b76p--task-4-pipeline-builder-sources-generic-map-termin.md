---
# mjprof-b76p
title: 'Task 4: Pipeline builder - sources, generic map, terminals'
status: completed
type: task
priority: normal
created_at: 2026-07-31T08:27:11Z
updated_at: 2026-07-31T09:25:01Z
parent: mjprof-6l4o
blocked_by:
    - mjprof-g9ee
    - mjprof-g36v
---

Plan Task 4.

- [x] PipelineTest failing first
- [x] Lazy Supplier source so a Pipeline is reusable across collect() calls
- [x] Four map overloads on intersection bounds — COMPILED, no erasure clash, no fallback needed
- [x] collect/collectOne/threads
- [x] Fix round 1: eager hostPort validation (MjprofException, no System.exit), null credentials normalized to "", three real JMX tests replacing one that passed for the wrong reason, collectOne names its source
- [x] Commits d339673, edd3a37

## Summary of Changes

Public typed builder for the library. Stages are plugin instances; the compiler checks their constructor args.

Review caught two defects that would have shipped a library able to kill its host process. JmxDataSourcePlugin's constructor calls System.exit(1) on a malformed host:port and NPEs on null credentials — both fine for a CLI, fatal for a Spring service. Guarded at the API boundary (eager JMXServiceURL validation using the identical URL template, null creds normalized to "") so plugin behavior and CLI semantics are untouched.

Also replaced unreachableJmxThrowsMjprofException, which passed because of the null-credential NPE rather than any network failure, with three tests that each pin one path: dead endpoint, null-creds-as-no-auth (asserting the cause is not NPE), and malformed host without JVM exit.

PipelineTest 10 tests; full suite 74 (55 core, 19 cli). Design claim confirmed: intersection-bounded overloads work, so Tasks 5-7 keep the planned API.
