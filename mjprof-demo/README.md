# mjprof-demo

A Spring Boot service that analyses **its own** threads through the `mjprof-core` API. No jstack, no
JMX connection, no second process — every endpoint snapshots the JVM it is running in via
`Pipeline.fromSelf()`.

## Run

```bash
mvn -pl mjprof-demo -am spring-boot:run
```

Listens on port 8080; override with `PORT=18080` (or `--server.port=18080`) when 8080 is taken.
`DemoWorkload` starts a few daemon threads at boot (CPU-burning, lock-blocked, waiting) so the
endpoints have something to show. Control the busy ones with `--demo.busy-threads=4`.

## Endpoints

| Endpoint | mjprof capability it demonstrates |
|---|---|
| `GET /api/threads?frames=3` | `fromSelf()` + `threads()` — raw snapshot |
| `GET /api/threads/states` | `group(Attr.STATE)` — grouping collapses threads and reports `count` |
| `GET /api/threads/state/{state}` | `contains(Attr.STATE, …)` — filtering |
| `GET /api/threads/pools` | `withoutNameSuffix()` + `group(Attr.NAME)` — collapsing `pool-1-thread-N` |
| `GET /api/threads/search?frame=X` | `contains(Attr.STACK, X)` — substring match over rendered stacks |
| `GET /api/threads/hot?samples=5&sleepMs=100&top=10&frames=5` | `fromSelfWithCpu()` + `merge(Attr.TID)` + `sortDesc(Attr.CPU_NS)` — sampled CPU profile |
| `GET /api/threads/tree[?state=RUNNABLE]` | `treeText()` — all stacks merged into one call tree |

## Notes

- Sampling runs on the request thread. `/hot` blocks for roughly `samples * sleepMs` — every sample
  measures a `sleepMs` CPU interval, so the last one sleeps too. The bounds (`samples` <= 50,
  `sleepMs` <= 2000) only cap a single request at ~100s; they are a demo-grade guard, not a rate or
  concurrency limit, and each concurrent caller holds its own request thread for its whole run, so
  enough slow callers will occupy the servlet thread pool (200 threads by default).
- `ThreadInfo` is a heterogeneous property bag and is not bean-shaped, so responses use explicit DTO
  records (`ThreadView`, `GroupCount`, `HotThread`) rather than serializing it directly.
- This module is a sample. It is not published and not part of the release.
