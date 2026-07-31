# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

mjprof is a command-line "monadic" jstack/thread-dump analyzer. A command is a chain of small composable steps (monads) — data source → filters/mappers → terminal/output. Steps are separated by `.` or spaces; a step's arguments are wrapped in `/.../` and separated by `,` (slashes are used because `()[]{}` are shell-special). Escape a literal comma inside args as `,,`.

Example: `jstack -l <pid> | mjprof contains/state,RUNNABLE/.tree`

## Build & test

Requires GraalVM JDK (see `.sdkmanrc`: `java=22.3.r19-grl`); compiler target is Java 17.

- Build fat jar: `mvn clean package` → `target/mjprof-1.0-jar-with-dependencies.jar` (main class `com.performizeit.mjprof.MJProf`).
- Native image: `mvn -Pnative package` (GraalVM `native-maven-plugin`).
- All tests: `mvn test` (JUnit 5 / Jupiter).
- Single test: `mvn test -Dtest=MJProfTest` or a method `mvn test -Dtest=StackFrameTest#someMethod`.
- Run without full package: `mvn exec:java -Dexec.mainClass=com.performizeit.mjprof.MJProf -Dexec.args="..."` (or run the jar directly).

Coverage via JaCoCo runs during the `test` phase.

## Plugin (monad) system — the core abstraction

Every user-facing step is a **plugin** class annotated with `@Plugin(name=..., params={...}, category=..., description=...)` (`api/Plugin.java`). `category` is a `PluginCategory` enum value; the value determines which interface the class must implement and where MJProf places it in the pipeline:

| Category | Interface (`plugin/types/`) | Role |
|---|---|---|
| `DATA_SOURCE` | `DataSource` | Generates thread dumps (jmx, path, stdin, visualvm) |
| `FILTER` | `Filter` | Keep/drop whole threads |
| `SINGLE_THREAD_MAPPER` | `SingleThreadMapper` | Transform one `ThreadInfo` (trim stack, drop props) |
| `DUMP_REDUCER` | `DumpReducer` | Operate on a whole dump (group, merge) |
| `THREAD_INFO_COMPARTAOR` | `ThreadInfoComparator` | Sorting |
| `TERMINAL` | `Terminal` | Consume the stream (count, tree, flat, list) |
| `OUTPUTER` | `Outputer` | Write results (stdout, snapshot, gui) |

Implementations live under `plugins/<category>/`.

### Plugin discovery is two-phase — this matters

1. At build (`process-classes` phase), `PluginRepoBuilder` uses the `reflections` library to scan `com.performizeit` for `@Plugin` classes and writes their FQNs to `target/classes/supported_monads.txt`.
2. At runtime, `StepsRepository` (static init) reads `/supported_monads.txt` from the classpath. **Only if that file is missing** does it fall back to a live reflection scan (`resolvePluginsDynamically`).

The generated file exists because reflection-based scanning does **not** work in GraalVM native images. `StepsRepository.addPluginToRepo` also eagerly instantiates each plugin once so GraalVM records the reflective constructor call (native profile additionally runs `PluginRepositoryScanner` under the native-image agent to capture reflection metadata).

**Consequence:** after adding/removing/renaming a plugin or changing its `@Plugin` name, you must rebuild so `supported_monads.txt` regenerates — a stale file silently drops or misnames monads.

## Execution model

`MJProf.main`:
1. Parse the command line into `List<MJStep>` (`parseCommandLine` → `splitCommandLine` respecting `/.../` arg boundaries; `findNextSeperator` handles the `.`/space separator logic).
2. Expand macros (`Macros`, from `internal.macros.properties` + external `-Dmacros.configFile`). A macro name expands to a sub-chain of steps.
3. Auto-insert defaults: prepend `stdin` if no explicit data source; append `stdout` if the last step isn't an outputer.
4. `constructPlumbing`: turn each step into a `Pipe` (a `Thread` with a `BlockingQueue`, in `plumbing/`). Data sources become `Generator`s feeding the chain. Pipes are linked producer→consumer; `producerDone` propagates completion down the chain. So the pipeline is genuinely multi-threaded, one thread per stage.

`buildArgsArray` coerces string args to the declared `Param` types (int/long/boolean/`Attr`/String), applying `optional`/`defaultValue`.

## Data model & parsing

- `parser/` — `ThreadDumpTextualParser` / `JStackTextualFormatParser` turn jstack text into `ThreadDump` → `ThreadInfo` (thread props: state, nid, tid, name, stack, los, cpu, etc., keyed by `ThreadInfoProps` / `Attr`).
- `model/` — `Profile`, `SFNode` (stack-frame tree), `ThreadInfoAggregator`, `ProfileEntryHelper` support the tree/flat/group/merge terminals and reducers.

## Adding a new monad (checklist)

1. Create a class under `plugins/<category>/` implementing the matching `plugin/types/*` interface.
2. Annotate with `@Plugin(name="...", params={@Param(...)}, category=PluginCategory.X, description="...")`.
3. `mvn package` (regenerates `supported_monads.txt`).
4. Add a test under the mirrored path in `src/test/java`; sample dumps live in `src/test/res/`.

**IMPORTANT**: before you do anything else, run the `beans prime` command and heed its output.
