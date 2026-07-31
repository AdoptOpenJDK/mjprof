---
# mjprof-ibp9
title: 'Task 1: split into mjprof-core and mjprof-cli modules'
status: todo
type: task
priority: high
created_at: 2026-07-31T08:26:56Z
updated_at: 2026-07-31T08:26:56Z
parent: mjprof-6l4o
---

Plan: docs/superpowers/plans/2026-07-31-core-cli-split-typed-api.md (Task 1)

Non-divisible: a half-renamed tree does not compile. ~90 files.

- [ ] Capture golden CLI output for 10 expressions BEFORE any move (cannot be reconstructed later)
- [ ] git mv files into mjprof-core / mjprof-cli
- [ ] Rewrite package + import statements (longest prefix first: plugin.types before plugin)
- [ ] Parent pom (packaging=pom), mjprof-core/pom.xml, mjprof-cli/pom.xml
- [ ] mvn clean package green, supported_monads.txt regenerated with 31 entries under core.plugins
- [ ] Golden diff clean for all 10 expressions
- [ ] Launcher scripts: main class + jar name (mjprof-cli-1.1.0)
- [ ] release.yml + jreleaser.yml repointed to mjprof-cli/target
- [ ] Commit
