---
# mjprof-6l4o
title: Split mjprof into core + cli modules with a typed pipeline API
status: in-progress
type: epic
priority: high
created_at: 2026-07-31T08:26:33Z
updated_at: 2026-07-31T08:26:33Z
---

Split the single artifact into a dependency-free mjprof-core library and mjprof-cli, rename packages to com.performizeit.mjprof.{core,cli}.*, and add a strongly typed Pipeline API driven by a synchronous runner.

Spec: docs/superpowers/specs/2026-07-31-core-cli-split-typed-api-design.md
Plan: docs/superpowers/plans/2026-07-31-core-cli-split-typed-api.md
Branch: core-cli-split

A Spring Boot service consuming this API over JMX is deliberately out of scope and gets its own spec once the API shape lands.
