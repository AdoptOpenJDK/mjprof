# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

mjprof is a command-line "monadic" jstack/thread-dump analyzer. A command is a chain of small composable steps (monads) — data source → filters/mappers → terminal/output. Steps are separated by `.` or spaces; a step's arguments are wrapped in `/.../` and separated by `,` (slashes are used because `()[]{}` are shell-special). Escape a literal comma inside args as `,,`.

Example: `jstack -l <pid> | mjprof contains/state,RUNNABLE/.tree`

`mjprof-core` also exposes a typed, thread-free Java API (`com.performizeit.mjprof.core.pipeline.Pipeline`) for embedding — see `README.md`'s "Java API" section.

## Modules

This is a two-module Maven reactor (root `pom.xml` is `packaging=pom`, `<modules>mjprof-core, mjprof-cli</modules>`):

- **`mjprof-core`** (`com.performizeit.mjprof.core.*`) — thread-dump parsing, the data model, every plugin implementation, the plugin type interfaces, the threaded `Pipe`/`Generator` plumbing, and the typed `Pipeline`/`PipelineRunner` API. **Zero external main-scope dependencies** — keep it that way; anything that pulls in a library goes in `mjprof-cli` instead.
- **`mjprof-cli`** (`com.performizeit.mjprof.cli.*`) — the expression DSL (`monads/MJStep`, `monads/Macros`, `internal.macros.properties`), the plugin registry (`monads/StepsRepository`, `plugin/PluginRepoBuilder`, `plugin/PluginRepositoryScanner`), `MJProf.main`, and the `native` Maven profile. Depends on `mjprof-core` plus the `reflections` library.

## Build & test

Requires GraalVM JDK (see `.sdkmanrc`: `java=22.3.r19-grl`); compiler target is Java 17.

- All tests, both modules, from the repo root: `mvn test` (JUnit 5 / Jupiter).
- Single test class/method needs `-pl` to target a module, and the two modules are asymmetric. `mjprof-core` has no reactor dependency, so `mvn -pl mjprof-core test -Dtest=StackTreeTest` works standalone. `mjprof-cli` depends on `mjprof-core` through the reactor (it isn't `mvn install`-ed into the local repo), so a bare `-pl mjprof-cli` fails dependency resolution — it needs `-am` to build `mjprof-core` first; that in turn makes surefire run in `mjprof-core` too, which errors out because the named class doesn't live there, so also pass `-Dsurefire.failIfNoSpecifiedTests=false`: `mvn -pl mjprof-cli -am test -Dtest=MJProfTest#someMethod -Dsurefire.failIfNoSpecifiedTests=false`.
- Build fat jar: `mvn clean package` → `mjprof-cli/target/mjprof-cli-1.1.0-jar-with-dependencies.jar` (main class `com.performizeit.mjprof.cli.MJProf`).
- Native image: `mvn -Pnative package -pl mjprof-cli -am` (GraalVM `native-maven-plugin`; `-am` also builds `mjprof-core` first since the profile lives only in `mjprof-cli`).
- Run without full package: `mvn -pl mjprof-cli exec:java -Dexec.mainClass=com.performizeit.mjprof.cli.MJProf -Dexec.args="..."` (or run the jar directly).

Coverage via JaCoCo runs during the `test` phase in each module.

## Plugin (monad) system — the core abstraction

Every user-facing step is a **plugin** class annotated with `@Plugin(name=..., params={...}, category=..., description=...)` (`mjprof-core`'s `core/api/Plugin.java`). `category` is a `PluginCategory` enum value; the value determines which interface the class must implement and where MJProf places it in the pipeline:

| Category | Interface (`core/plugin/types/`) | Role |
|---|---|---|
| `DATA_SOURCE` | `DataSource` | Generates thread dumps (jmx, path, stdin, visualvm) |
| `FILTER` | `Filter` | Keep/drop whole threads |
| `SINGLE_THREAD_MAPPER` | `SingleThreadMapper` | Transform one `ThreadInfo` (trim stack, drop props) |
| `DUMP_REDUCER` | `DumpReducer` | Operate on a whole dump (group, merge) |
| `THREAD_INFO_COMPARTAOR` | `ThreadInfoComparator` | Sorting |
| `TERMINAL` | `Terminal` | Consume the stream (count, tree, flat, list) |
| `OUTPUTER` | `Outputer` | Write results (stdout, snapshot, gui) |

Implementations live under `mjprof-core`'s `core/plugins/<category>/`.

### Two execution runtimes share the same plugins

Every plugin instance is also a `PipeHandler<ThreadDump, ThreadDump>` (or the `ThreadInfo`-level equivalent), and there are **two** ways that gets driven:

1. **CLI**: `mjprof-cli`'s `MJProf.constructPlumbing` wraps each step in a `core/plumbing/Pipe` — a `Thread` with a `BlockingQueue` — chained via `setOutgoingPipe`; data sources become `core/plumbing/Generator` threads. Genuinely multi-threaded, one thread per stage.
2. **API**: `mjprof-core`'s `pipeline/PipelineRunner` drives the same plugin instances synchronously on the calling thread, no threads or queues, via `pipeline/Pipeline`'s typed builder methods.

**Consequence:** a new or changed plugin must keep working under both. Don't assume `handleMsg`/`handleDone` are only ever called from a `Pipe`'s thread — `PipelineRunner` calls them directly and reuses the same instance across the whole run.

### Plugin discovery is two-phase — this matters

1. At build (`mjprof-cli`'s `process-classes` phase), `cli/plugin/PluginRepoBuilder` uses the `reflections` library to scan `com.performizeit` for `@Plugin` classes and writes their FQNs to `mjprof-cli/target/classes/supported_monads.txt`.
2. At runtime, `cli/monads/StepsRepository` (static init) reads `/supported_monads.txt` from the classpath. **Only if that file is missing** does it fall back to a live reflection scan (`resolvePluginsDynamically`).

The generated file exists because reflection-based scanning does **not** work in GraalVM native images.

The same `PluginRepoBuilder` run also writes `META-INF/native-image/reflect-config.json` (every plugin, `allDeclaredConstructors`) and `resource-config.json` (`supported_monads.txt`, `internal.macros.properties`) — generated deterministically from the discovered plugin list, on a plain JDK. It used to be produced by running `PluginRepositoryScanner` under the `native-image-agent` in the `native` profile, but that agent library ships only with GraalVM, so `mvn -Pnative package` failed on an ordinary JDK before native-image was ever reached. `PluginRepositoryScanner` is kept as a manual way to cross-check the generated config against what the agent would record (run it under a GraalVM with `-agentlib:native-image-agent=config-output-dir=...`); it is no longer part of any build.

**Consequence:** after adding/removing/renaming a plugin or changing its `@Plugin` name, you must rebuild `mjprof-cli` so `supported_monads.txt` regenerates — a stale file silently drops or misnames monads. Changing a plugin constructor's signature or parameter types can likewise break the native build even when the jar works fine, since `reflect-config.json` is regenerated from the plugin list and `StepsRepository.addPluginToRepo` instantiates every plugin at startup.

## Execution model (CLI)

`MJProf.main` (`mjprof-cli`):
1. Parse the command line into `List<MJStep>` (`parseCommandLine` → `splitCommandLine` respecting `/.../` arg boundaries; `findNextSeperator` handles the `.`/space separator logic).
2. Expand macros (`Macros`, from `internal.macros.properties` + external `-Dmacros.configFile`). A macro name expands to a sub-chain of steps.
3. Auto-insert defaults: prepend `stdin` if no explicit data source; append `stdout` if the last step isn't an outputer.
4. `constructPlumbing`: turn each step into a `Pipe` (from `mjprof-core`'s `plumbing/`). Data sources become `Generator`s feeding the chain. Pipes are linked producer→consumer; `producerDone` propagates completion down the chain.

`buildArgsArray` coerces string args to the declared `Param` types (int/long/boolean/`Attr`/String), applying `optional`/`defaultValue`.

## Data model & parsing (mjprof-core)

- `core/parser/` — `ThreadDumpTextualParser` / `JStackTextualFormatParser` turn jstack text into `ThreadDump` → `ThreadInfo` (thread props: state, nid, tid, name, stack, los, cpu, etc., keyed by `ThreadInfoProps` / `Attr`).
- `core/model/` — `Profile`, `SFNode` (stack-frame tree), `ThreadInfoAggregator`, `ProfileEntryHelper` support the tree/flat/group/merge terminals and reducers.

## Adding a new monad (checklist)

1. Create a class under `mjprof-core`'s `core/plugins/<category>/` implementing the matching `core/plugin/types/*` interface. New plugins go in `mjprof-core`; anything that touches the expression DSL, macros or the plugin registry goes in `mjprof-cli`.
2. Annotate with `@Plugin(name="...", params={@Param(...)}, category=PluginCategory.X, description="...")`.
3. `mvn package` (regenerates `mjprof-cli/target/classes/supported_monads.txt`).
4. Add a test under the mirrored path in the owning module's `src/test/java`; sample dumps live in `mjprof-core/src/test/res/`.
5. Verify the plugin works under both runtimes: the threaded CLI path and the synchronous `PipelineRunner` (add a `Pipeline` test in `mjprof-core` if there's a convenience method for it).

**IMPORTANT**: before you do anything else, run the `beans prime` command and heed its output.
