# Design: split mjprof into `mjprof-core` + `mjprof-cli` with a typed pipeline API

Date: 2026-07-31
Status: approved

## Goal

Split the single-artifact project into a reusable library (`mjprof-core`) and the command-line
application (`mjprof-cli`), and give the library a strongly typed Java pipeline API so other JVM
programs can analyse thread dumps without shelling out or parsing text.

A Spring Boot service that consumes this API over JMX is the motivating consumer. It is **not** part
of this spec — its endpoints are constrained by the API shape defined here, so it gets its own spec
after this work lands.

## Why the API needs designing, not just extracting

The current entry point is `MJProf.main`. It parses a command-line expression, builds a chain of
`Pipe` threads held in **static** fields, starts them, and returns. Nothing joins the threads;
results are printed by an `Outputer` at the tail. That is fine for a process that exits after one
expression and unusable from a server: not reentrant, no return value, no error propagation.

Two facts about the existing code shape the design:

1. **The pipeline element type is `ThreadDump`, not `ThreadInfo`.** Every plugin base is
   `PipeHandler<ThreadDump,ThreadDump>` (`SingleThreadMapperBase`, `SingleThreadFilter`,
   `SortMapper`, `AccumulateDumps`, `GroupByProp`); sources are `GeneratorHandler<ThreadDump>`;
   terminals are `PipeHandler<ThreadDump,String>`. `MJProf`'s `Generator<ThreadInfo>` and
   `Pipe<ThreadInfo,ThreadInfo>` declarations are simply wrong and survive only because generics
   erase. A typed builder cannot inherit that mistake.
2. **A source emits many dumps.** `Generator` loops until `isDone()`, so `jmx/host:port/3/1000/`
   produces three dumps, and a file containing appended dumps produces one per dump. The API
   returns `List<ThreadDump>`.

## Module layout

```
mjprof/                  parent, packaging=pom, com.performizeit:mjprof:1.1.0
├── mjprof-core/         library. JDK-only deps; junit test-scope
└── mjprof-cli/          the `mjprof` command. depends mjprof-core; org.reflections
```

### `mjprof-core`

Packages (all renamed, see Package map):

- `core.api` — `Attr`, `BasePlugin`, `Param`, `Plugin`, `PluginCategory`, `ThreadInfoComparator`
- `core.model` — `JStackHeader`, `Profile`, `ProfileEntryHelper`, `ProfileVisitor`, `SFNode`,
  `ThreadInfoAggregator`
- `core.parser` — `HexaLong`, `JStackTextualFormatParser`, `ThreadContainer`, `ThreadDump`,
  `ThreadDumpTextualParser`, `ThreadInfo`, `ThreadInfoProps`, `ThreadLockedOwnableSynchronizers`
- `core.plumbing` — `Generator`, `GeneratorHandler`, `Pipe`, `PipeHandler` (was
  `com.performizeit.plumbing`)
- `core.plugin.types` — `DataSource`, `DumpReducer`, `Filter`, `Outputer`, `SingleThreadMapper`,
  `Terminal`
- `core.plugins.*` — every plugin, subpackages unchanged (`dataSource`, `filters`, `mappers`,
  `mappers.singlethread`, `mappers.singlethread.stackframe`, `mergers`, `comparators`, `terminals`,
  `output`, `combiners`)
- `core.jmx` — `JMXConnection` (was `com.performizeit.jmxsupport`)
- `core.pipeline` — **new**: `Pipeline`, `PipelineRunner`, `DumpsDataSource`, `MjprofException`

Build: compiler 17, jacoco. No exec-maven-plugin, no assembly, no code generation, no external
dependency. Test fixtures `src/test/res/*.txt` live here.

### `mjprof-cli`

- `cli.MJProf` — unchanged behavior, still the `mainClass`
- `cli.monads` — `MJStep`, `Macros`, `StepInfo`, `StepsRepository`
- `cli.plugin` — `PluginRepoBuilder`, `PluginRepositoryScanner`, `PluginUtils`
- `src/main/resources/internal.macros.properties`, `src/main/scripts/*`

Build: keeps the exec-maven-plugin `process-classes` execution generating `supported_monads.txt`,
the resources copy, the assembly `jar-with-dependencies`, and the whole `native` profile. Takes the
`org.reflections` dependency.

The expression DSL and the plugin registry are CLI-only. Consequences, all deliberate: the library
never pulls Reflections, never runs `StepsRepository`'s "instantiate every plugin at startup"
reflection pass, and cannot accidentally load the Swing viewer.

### Verified build facts

- `PluginRepoBuilder` scans `Reflections("com.performizeit")`. The rename keeps everything under
  that prefix, and core's plugins are discovered because exec's `<classpath/>` in `mjprof-cli`
  includes the core jar.
- `supported_monads.txt` is written to `mjprof-cli/target/classes` and read only by
  `StepsRepository`, so native-image reflection metadata generation is unaffected in principle —
  but the generated file now lists classes under the new package names, which the native build must
  be re-run to confirm.
- Only `MergedCalleesTest` reads fixture files, via relative paths (`src/test/res/test2.txt`). It
  moves to core with the fixtures; cli needs no fixtures.
- Tests moving to cli: `MJProfTest`, `MJStepTest`, `PluginTest`,
  `PluginWithDefaultConstructorTest`, `PluginWithParameterConstructorTest`. All others go to core.
- No core class depends on a cli class. The `"MJProf "` string inside `SnapshotToGui` is a window
  title, not a reference.
- `SnapshotToGui`'s `@Plugin` annotation is commented out — it is unregistered today and stays that
  way.

## Package map

Full rename to reflect the module boundary: `com.performizeit.mjprof.core.*` and
`com.performizeit.mjprof.cli.*`. `com.performizeit.plumbing` and `com.performizeit.jmxsupport`, both
currently outside the `mjprof` package, fold in as `core.plumbing` and `core.jmx`.

This touches nearly every file's `package` and `import` lines, and additionally:

- assembly manifest `mainClass` → `com.performizeit.mjprof.cli.MJProf`
- native-image plugin `mainClass` → same
- pom `exec` arguments: `com.performizeit.mjprof.cli.plugin.PluginRepoBuilder` and
  `...cli.plugin.PluginRepositoryScanner`
- `src/main/scripts/mjprof`, `mjprof.bat`: main class, and the jar name
  (`mjprof-1.0-jar-with-dependencies.jar` → `mjprof-cli-1.1.0-jar-with-dependencies.jar`; the
  current name is stale against a 1.1.0 pom)
- `.github/workflows/release.yml` and `jreleaser.yml`: artifact paths → `mjprof-cli/target`
- README examples that name classes or packages

Any third-party plugin jar compiled against the old package names breaks. Acceptable; documented in
the README.

## API surface (`com.performizeit.mjprof.core.pipeline`)

```java
List<ThreadDump> dumps = Pipeline.from(new JmxDataSourcePluginWithCpu("localhost:9010", 3, 1000, null, null))
    .contains(Attr.STATE, "RUNNABLE")
    .map(new TrimBelow("java.util.concurrent"))
    .group(Attr.STATE, Attr.NAME)
    .sortDesc(Attr.CPU_NS)
    .collect();
```

`Pipeline` is immutable-ish builder style: each method returns `Pipeline` and appends a stage. No
statics, no shared state between instances.

### Sources

```java
static <S extends DataSource & GeneratorHandler<ThreadDump>> Pipeline from(S source);
static Pipeline fromDumps(Collection<ThreadDump> dumps);                  // in-memory, via DumpsDataSource
static Pipeline fromFile(String path);
static Pipeline fromStdin();
static Pipeline fromJmx(String hostPort, int count, int sleepMs, String user, String pass);
static Pipeline fromJmxWithCpu(String hostPort, int count, int sleepMs, String user, String pass);
```

`DumpsDataSource` is a new ~15-line `DataSource` + `GeneratorHandler<ThreadDump>` over an iterator.
It carries no `@Plugin` annotation — it is API-only and must not appear in `supported_monads.txt`,
since an in-memory source is meaningless from a command line.

### Generic stage — `map`

Four overloads, one per plugin marker interface. The markers are distinct non-generic types, so the
overloads do not clash; the `PipeHandler` intersection bound makes the compiler, not a runtime cast,
enforce that the instance is a usable stage:

```java
public <P extends SingleThreadMapper    & PipeHandler<ThreadDump,ThreadDump>> Pipeline map(P stage);
public <P extends DumpReducer           & PipeHandler<ThreadDump,ThreadDump>> Pipeline map(P stage);
public <P extends Filter                & PipeHandler<ThreadDump,ThreadDump>> Pipeline map(P stage);
public <P extends ThreadInfoComparator  & PipeHandler<ThreadDump,ThreadDump>> Pipeline map(P stage);
```

A plugin implementing two of those markers produces an ambiguous-call **compile** error at the call
site. No plugin in the repo does. External plugin jars work with no registration step.

### Typed convenience methods

Thin wrappers constructing the same plugin instances:

| Method | Plugin |
|---|---|
| `contains(Attr, String)` / `notContains(Attr, String)` | `JStackFilterFieldContains` / `...NotContains` |
| `matches(Attr, String)` | `JStackFilterFieldRegexMatches` |
| `stackTop(int)` / `stackBottom(int)` / `trimBelow(String)` | `StackTop` / `StackBottom` / `TrimBelow` |
| `frameContains(String)` / `frameNotContains(String)` | `StackFrameContains` / `StackFrameNotContains` |
| `sort(Attr)` / `sortDesc(Attr)` | `PropComparator` / `ReversePropComparator` |
| `group(Attr...)` | `GroupByProp` |
| `merge()` / `merge(Attr)` | `AccumulateDumps` (default key `tid`) |
| `tree()` / `mergedCallees(String)` | `StackTreeMerger` / `MergedCallees` |
| `withoutProp(Attr)` / `withoutNameSuffix()` | `PropEliminator` / `RemoveThreadNameNumericalSuffix` |
| `withoutPackages()` / `withoutFileNames()` / `withoutAt()` | `PackageEliminator` / `FileNameEliminator` / `AtEliminator` |

Anything not listed is reachable through `map`.

### Terminals

```java
List<ThreadDump> collect();     // one entry per dump emitted by the source
ThreadDump       collectOne();  // MjprofException if the source emitted != 1
List<ThreadInfo> threads();     // collect() flattened
```

Existing `Outputer` and `Terminal` plugins stay CLI-only; the API never writes to a stream. Callers
serialise what they get.

### Typed attributes and accessors

`Attr` gains public static final constants sourced from `ThreadInfoProps`: `NAME`, `TID`, `NID`,
`DAEMON`, `PRIO`, `STATUS`, `STATE`, `CPU_NS`, `WALL_MS`, `CPU_PCT`, `STACK`, `LOS`, `COUNT`. Custom
props remain `new Attr("whatever")`. The existing `Attr(String)` constructor and CLI parameter
conversion are untouched.

`ThreadInfo` gains typed accessors wrapping `getVal` — `name()`, `state()`, `status()`, `tid()`,
`cpuNs()`, `wallMs()`, `cpuPct()`, `count()`, `stack()`, `daemon()` — so API consumers stop casting
`Object` at every call site. Existing `getVal`/`setVal` stay for plugins.

### Change to an existing plugin

`GroupByProp` gains a `GroupByProp(List<String>)` constructor feeding `ThreadInfoAggregator`'s
already multi-prop key, which today is fed a single-element list. The `(String)` constructor stays so
the `group` monad and its reflective instantiation are unchanged; the CLI remains single-attribute.

## Execution model

`PipelineRunner` (package-private, ~60 lines) replicates `Pipe`/`Generator` semantics
synchronously — no threads, no statics, reentrant:

```java
while (!source.isDone()) {
    ThreadDump d = source.generate();
    if (d != null) push(d, 0);        // stages 0..n; a null return drops the message
    if (!source.isDone()) source.sleepBetweenIteration();
}
for (int i = 0; i < stages.size(); i++) {
    ThreadDump r = stages.get(i).handleDone();
    if (r != null) push(r, i + 1);    // done-output flows only to the remaining stages
}
```

The `handleDone` fan-forward is load-bearing: `AccumulateDumps` returns `null` from every
`handleMsg` and emits the combined dump from `handleDone`, so a `merge()` stage only produces output
during the second loop. Whatever exits the last stage is appended to the result list.

Plugins carry no concurrency semantics — `handleMsg`/`handleDone` is a fold — so removing the
threads changes no plugin behavior.

**Blocking is on the caller's thread by design.** `fromJmx(hostPort, 3, 1000, …)` blocks about two
seconds inside `sleepBetweenIteration`. Correct for a CLI, and a server endpoint choosing `count>1`
is choosing to block. Stated in the `Pipeline` Javadoc so it is not discovered in production.

The CLI keeps the existing threaded `Pipe`/`Generator` plumbing, unchanged.

## Error handling

Today a failing data source kills a thread and the pipeline silently yields nothing. For a library
that is a correctness bug, and for an HTTP endpoint it is the difference between a 502 and a
misleading "0 threads".

- New unchecked `MjprofException` in `core.pipeline`.
- `PipelineRunner` wraps any exception from `generate`, `handleMsg`, or `handleDone`, naming the
  failing stage's class.
- JMX connection failures (wrong port, bad credentials, dead process) surface as `MjprofException`,
  never as an empty result.
- `PathDataSourcePlugin` on a missing file: current behavior is to log to stderr and yield nothing.
  Under the API it must throw. Implementation checks whether that can change in the plugin without
  altering CLI output, and if not, the runner detects the empty-source case for file sources.

## Collecting dumps over time

The `merge` monad (`AccumulateDumps`) already does cross-dump merging; combined with `fromDumps` a
caller can sample on its own schedule and analyse later.

In-memory, no persistence:

```java
List<ThreadDump> samples = new ArrayList<>();
samples.add(Pipeline.fromJmxWithCpu("localhost:9010", 1, 0, null, null).collectOne());  // on your cadence

List<ThreadInfo> profile = Pipeline.fromDumps(samples)
    .merge(Attr.TID)          // same thread across samples folds together
    .withoutNameSuffix()
    .group(Attr.NAME)         // then collapse pool threads
    .sortDesc(Attr.CPU_NS)
    .threads();
```

`ThreadInfoAggregator` sums `cpu_ns`, takes max `wall_ms`, recomputes `%cpu`, and merges each
thread's `Profile` into a stack tree with per-node `count` — so N cheap samples become a sampled
profile. `merge(Attr.TID)` folds per thread; `tree()` afterwards folds the merged profiles into one
stack tree. Folding by stack *as a grouping key* is not available — see open question 1.

Across restarts, persist as text and re-read:

```java
try (PrintWriter w = new PrintWriter(new FileWriter("session.txt", true))) {
    w.println(Pipeline.fromJmx("localhost:9010", 1, 0, null, null).collectOne());
}
// later, another JVM
Pipeline.fromFile("session.txt").merge().tree().collect();
```

This works because `StreamDataSourcePluginBase` splits a stream into dumps on date-prefixed lines,
the same mechanism `jstackOverTime.sh` relies on. Property fidelity is verified — `count`, `cpu_ns`,
`wall_ms` and `%cpu` all survive (see open question 2) — but this works only for jstack-format text.
Dumps taken over JMX carry a non-date-prefixed header, so concatenating them yields one unsplittable
blob; use `fromDumps` for those.

No Java serialization and no JSON format for `ThreadDump`. Add one when a real consumer needs to
store sessions across restarts.

## Verification

Core unit tests, over `src/test/res/test.txt`:

- filter, sort, `group(Attr...)` with one and with multiple attributes
- `map` escape hatch accepts a plugin with no convenience method
- `handleDone` fan-forward: a `merge()` stage followed by another stage produces the merged result
- `fromDumps` + `merge(Attr.TID)` over two hand-built dumps sums `cpu_ns` and `count`
- `collectOne()` throws `MjprofException` when the source emits more than one dump
- `MjprofException` on a missing file, and on `group`/`merge` handed `Attr.STACK`
- text round-trip fidelity is asserted, not assumed: a dump carrying `count`/`cpu_ns`/`wall_ms`/`%cpu`
  re-read from `toString()` output returns those values as numbers — they come back as Strings and the
  accessors coerce them (`ThreadInfoAccessorsTest`)
- concatenated JMX-sourced dumps do **not** re-split, because their header is not date-prefixed
- typed `Attr` constants resolve to the same strings as `ThreadInfoProps`

CLI regression guard for the rename: before splitting, capture `mjprof <expr>` stdout for about six
expressions against `src/test/res/test.txt` — including the README example, a macro (`running`,
`-jvm`), a `group`, a `merge`, and a `tree` — into a golden file; diff after the split. This is the
insurance that a ~90-file rename changed no behavior.

Build verification:

- `mvn clean package` builds both modules and the assembled cli jar runs an expression end to end
- `mvn -Pnative package` produces a working native binary
- generated `supported_monads.txt` lists every plugin under its new package name, same count as
  before the split

## Open questions — resolved against the code

1. **`Profile` has no `equals`/`hashCode`** (neither does `SFNode`), so `ThreadInfoAggregator` keys on
   object identity and `group(Attr.STACK)` / `merge(Attr.STACK)` group nothing. Adding value equality
   is not an option: the aggregator *mutates* profiles while merging (`Profile.addMulti`), and a
   mutable object cannot be a hash key. **Resolution:** `group(Attr...)` and `merge(Attr)` throw
   `MjprofException` when handed `Attr.STACK`, with a message pointing at `merge(Attr.TID).tree()` —
   which produces the merged stack tree that "group by stack" is actually reaching for. The `group`
   and `merge` monads keep their current silent behavior; only the API guards.
2. **CORRECTED during implementation — the text round-trip preserves the numeric props, and the
   original claim here was wrong.** `toJStackFormat` writes `count=`, `cpu_ns=`, `wall_ms=`, `%cpu=`
   into the thread header line, and `JStackTextualFormatParser.metadataKeyValProperties` reads every
   `key=value` pair on that line back via a generic `(\S+)=(\S+)` regex — as a **`String`**. The
   absence of a *named* parse case for those props is what made them look lost. Measured: all four
   survive `ThreadDump.toString()` → `parseStringRepresentation`.

   That same fact was a live `ClassCastException`: typed accessors casting straight to
   `Long`/`Double`/`Integer` threw on any re-parsed dump. **Resolution:** the numeric accessors coerce
   `Number` or `String`, and throw `NumberFormatException` on non-numeric input rather than returning a
   silent null, covered by a round-trip regression test in `ThreadInfoAccessorsTest`.

   The remaining limitation has nothing to do with fidelity: `JmxDataSourcePlugin` sets its header to
   `new Date() + "\nThread dump via JMX of ..."`, and `Date.toString()`
   ("Fri Jul 31 10:23:45 IDT 2026") does not match the `^\d\d\d\d-\d\d-\d\d` pattern
   `StreamDataSourcePluginBase` splits on — so concatenated **JMX-sourced** dumps do not re-split into
   separate dumps. `fromDumps` stays the recommended path for self-collected samples because it
   sidesteps text entirely.
3. **`PathDataSourcePlugin` already throws** `RuntimeException(FileNotFoundException)` from
   `initReader()` on a missing file. No plugin change needed; the runner wraps it in
   `MjprofException`.

## Follow-up: replace classpath scanning with an annotation processor

Not part of this spec. Recorded because the module split is what makes it possible.

`supported_monads.txt` is generated by running `PluginRepoBuilder` in a JVM at `process-classes`,
scanning the classpath with `Reflections("com.performizeit")`. That can become compile-time code
generation, but **not from `mjprof-cli`** — an annotation processor only sees the sources in its own
compilation unit, and cli receives the `@Plugin` classes as an already-compiled dependency jar, so a
cli-side processor would emit an empty list.

It works only where the plugins are compiled, and a processor cannot be used by the module that
defines it, so it needs its own module:

```
mjprof-plugin-processor/   ~50 lines, no dependencies
mjprof-core/               annotationProcessorPaths -> processor; emits META-INF/mjprof-plugins
mjprof-cli/                registry reads every such resource on the classpath and unions them
```

Payoff:

- the `org.reflections` dependency disappears — it exists only for this scan
- `exec-maven-plugin` disappears from the normal build, and with it the "the build must run a JVM"
  constraint documented in CLAUDE.md
- external plugin jars become discoverable in the native image. Today they rely on the runtime
  Reflections fallback, which is precisely the path that does not work under GraalVM. With merged
  resources, `getResources("META-INF/mjprof-plugins")` yields one entry per jar.

Does not help: the `native` profile's second exec run (`PluginRepositoryScanner` under the
native-image agent, recording plugin constructor reflection metadata) still needs a JVM.
Registration and reflection metadata are separate problems.

`ServiceLoader` is not an alternative — plugins share `BasePlugin`, but each has a non-default
constructor and `ServiceLoader` requires a no-arg one.

Deferred purely for blast radius: this spec already carries a ~90-file rename plus a new API, and the
processor touches plugin discovery, the native build, and external plugin compatibility at once.

## Out of scope

- The Spring Boot module. Follow-up spec.
- Migrating the CLI onto the synchronous runner and deleting `Pipe`/`Generator`/`GeneratorHandler`.
  Tempting, and the runner makes it possible; not this change.
- Typed convenience methods for every monad. `map` covers the rest.
- Making `Terminal` generic over a result type so `CountThreads` and friends are callable from the
  API.
- A JSON or binary serialization format for `ThreadDump`.
- Any change to expression parsing, macros, or CLI output formatting.
