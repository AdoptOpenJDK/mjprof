---
# mjprof-tdxa
title: In-process data source + Spring Boot demo
status: in-progress
type: epic
priority: high
created_at: 2026-07-31T13:28:48Z
updated_at: 2026-07-31T13:28:48Z
---

Plan: docs/superpowers/plans/2026-07-31-self-datasource-and-spring-demo.md
Branch: core-cli-split

API-only SelfDataSource over ThreadMXBean (no JMX connection, no @Plugin, invisible to the CLI), plus an mjprof-demo Spring Boot module whose endpoints analyse their own process through mjprof-core.
