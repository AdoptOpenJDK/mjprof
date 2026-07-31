---
# mjprof-ibp9
title: 'Task 1: split into mjprof-core and mjprof-cli modules'
status: completed
type: task
priority: high
created_at: 2026-07-31T08:26:56Z
updated_at: 2026-07-31T08:45:16Z
parent: mjprof-6l4o
---

Plan: docs/superpowers/plans/2026-07-31-core-cli-split-typed-api.md (Task 1)

- [x] Capture golden CLI output for 10 expressions BEFORE any move
- [x] git mv files into mjprof-core / mjprof-cli
- [x] Rewrite package + import statements
- [x] Parent pom (packaging=pom), mjprof-core/pom.xml, mjprof-cli/pom.xml
- [x] mvn clean package green, supported_monads.txt 31 entries under core.plugins
- [x] Golden diff clean (9/10 byte-identical; 03-sort differs only by package names in a pre-existing NPE trace, same file:line)
- [x] Launcher scripts: main class + jar name
- [x] release.yml + jreleaser.yml repointed to mjprof-cli/target
- [x] Commit fdd4c3e

## Summary of Changes

Split into mjprof-core (zero external deps) and mjprof-cli (reflections, codegen, native profile). Packages renamed to com.performizeit.mjprof.{core,cli}.*; plumbing and jmxsupport folded under core. 46/46 tests pass. Review: spec met, quality approved, no Critical/Important findings.

Deviations from plan, all reviewed and accepted: per-file sed loop (BSD sed aborted on the batched form), one hand-fixed import in PluginTest.java the sed rules missed, and .gitignore /target/ -> target/ so per-module build output stays ignored.

Unverified here, deferred to Task 9: GraalVM native build and the JReleaser/CI paths (edited by inspection; no native-image toolchain available).
