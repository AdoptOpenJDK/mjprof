# In-Process Data Source + Spring Boot Demo — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an API-only data source that snapshots the threads of the JVM it is running inside, and a `mjprof-demo` Spring Boot module whose endpoints profile that same running process through `mjprof-core`.

**Architecture:** `SelfDataSource` reads `java.lang.management.ThreadMXBean` directly — no JMX connection, no network, no remote process — and converts each `java.lang.management.ThreadInfo` into mjprof's own `ThreadInfo`, mirroring what `JmxDataSourcePlugin.getProps` already does from `CompositeData`. It carries no `@Plugin` annotation, so it is invisible to the CLI's expression DSL and never enters `supported_monads.txt`; the only way in is `Pipeline.fromSelf(...)`. `mjprof-demo` is a third Maven module depending on `mjprof-core` only, exposing one REST endpoint per API capability, all pointed at itself.

**Tech Stack:** Java 17, Maven multi-module, JUnit 5 (junit-jupiter 5.12.2 via junit-bom), Spring Boot 3.5.16 (`spring-boot-dependencies` BOM import — the demo cannot use the Boot starter parent because the reactor parent is `com.performizeit:mjprof`), `java.lang.management` (JDK).

## Global Constraints

- Java source/target **17**; `project.build.sourceEncoding` **UTF-8**.
- groupId `com.performizeit`, parent artifactId `mjprof`, version **1.1.0** for all modules.
- **`mjprof-core` keeps ZERO external main-scope dependencies.** `SelfDataSource` may import only `java.*` and `com.performizeit.*`. Spring lives exclusively in `mjprof-demo`.
- **`SelfDataSource` must NOT carry a `@Plugin` annotation.** It is API-only — an in-process source is meaningless from a command line. `supported_monads.txt` must stay at exactly **31** entries.
- **No CLI behavior change.** The ten golden captures in `/tmp/mjprof-golden/` must still match (`03-sort` against `03-sort.fixround1.txt`, the other nine against their `NN-*.txt`).
- `mjprof-demo` depends on `mjprof-core` only — never on `mjprof-cli`, and nothing depends on `mjprof-demo`.
- Do not add `mjprof-demo` to `jreleaser.yml` or `.github/workflows/release.yml`. It is a sample, not a shipped artifact. The `native` profile lives only in `mjprof-cli` and must stay that way.
- No license header on new files (recent files in this repo have none).
- The API never writes to stdout/stderr and never starts a thread — except `DemoWorkload` in the demo module, whose entire purpose is to create threads worth looking at.

## API facts verified against the current code — build on these, do not re-derive

- `Profile.addSingle(StackTraceElement[])` exists (`core/model/Profile.java:82`) — the local `ThreadInfo.getStackTrace()` array feeds it directly.
- `JmxDataSourcePlugin.getProps` stores `NAME` (String), `TID` (**Long**), `STATE` (String), `STACK` (`Profile`). `SelfDataSource` mirrors this so both remote and local sources produce identically-shaped dumps.
- `ThreadInfo` has a public `ThreadInfo(Map<String,Object>)` constructor (`core/parser/ThreadInfo.java:35`).
- `JStackFilterFieldContains.filter` does `o.toString().contains(value)` (`core/plugins/filters/JStackFilterFieldContains.java`), so `contains(Attr.STACK, "java.net")` works on a `Profile`-valued property. Grouping by `STACK` is still rejected — `Profile` has no value equality.
- `Pipeline` factories follow a `fromX` naming convention: `from`, `fromFile`, `fromStdin`, `fromDumps`, `fromJmx`, `fromJmxWithCpu`.
- `Pipeline` holds its source as a `Supplier<GeneratorHandler<ThreadDump>>` and its stages as suppliers, so each `collect()` builds fresh objects. `from(S)` and the four `map(P)` overloads are the documented single-use exception.
- `Pipeline` terminals: `collect()` → `List<ThreadDump>`, `collectOne()`, `threads()` → `List<ThreadInfo>`, `treeText()` → `String`.
- `ThreadInfo` accessors: `name()`, `state()`, `status()`, `tid()`, `cpuNs()`, `wallMs()`, `cpuPct()`, `count()` (defaults to 1 when absent), `stack()` → `Profile`, `daemon()`.
- Sorting is null-safe: absent attributes sort last in both directions.
- `group(Attr...)` / `merge(Attr)` throw `MjprofException` for `Attr.STACK`.

## File Structure

**`mjprof-core` — modified**

| File | Change |
|---|---|
| `core/parser/ThreadInfo.java` | `tid()` coerces instead of casting (Task 1) |
| `core/pipeline/Pipeline.java` | `fromSelf()`, `fromSelf(int,int)`, `fromSelfWithCpu(int,int)` (Task 2) |

**`mjprof-core` — created**

| File | Responsibility |
|---|---|
| `core/pipeline/SelfDataSource.java` | Package-private in-process source over `ThreadMXBean`; no `@Plugin` |
| `src/test/.../core/pipeline/SelfDataSourceTest.java` | Tests, written before the implementation |

**`mjprof-demo` — created**

| File | Responsibility |
|---|---|
| `pom.xml` | Boot BOM import, `mjprof-core` dependency, `spring-boot-maven-plugin` |
| `demo/MjprofDemoApplication.java` | `main` + `@SpringBootApplication` |
| `demo/ThreadEndpoints.java` | One `@GetMapping` per API capability |
| `demo/ThreadView.java`, `demo/GroupCount.java`, `demo/HotThread.java` | JSON DTO records — `ThreadInfo` is a property bag and is not bean-shaped, so it is never serialized directly |
| `demo/ThreadViewMapper.java` | `ThreadInfo` → DTO conversion, including frame extraction |
| `demo/DemoWorkload.java` | Starts named threads in known states so the endpoints show something |
| `src/main/resources/application.properties` | Port and app name |
| `src/test/.../demo/ThreadEndpointsTest.java` | `@WebMvcTest` over every endpoint |
| `README.md` | How to run it and what each endpoint demonstrates |

**Modified at the root:** `pom.xml` (add `<module>mjprof-demo</module>`), `CLAUDE.md`, `README.md`.

**Task dependency order**

```
Task 1 (tid coercion)
  └── Task 2 (SelfDataSource + fromSelf)
        └── Task 3 (demo skeleton + /threads + /threads/states)
              ├── Task 4 (/threads/state/{state}, /pools, /search)
              └── Task 5 (/threads/hot, /threads/tree)
                    └── Task 6 (DemoWorkload + demo README + docs)
                          └── Task 7 (verification)
```

---

### Task 1: `tid()` must coerce, not cast

`ThreadInfo.tid()` does `(String) getVal(TID)`. The text parser stores `TID` as a String (hex, e.g. `0x00007f...`), but `JmxDataSourcePlugin` stores it as a **Long** — so `tid()` throws `ClassCastException` on any JMX-sourced dump today. `SelfDataSource` will also store a Long, and the demo's DTO mapper calls `tid()` on every thread, so this must be fixed first. This is the same defect family as the numeric accessors, which were already fixed by coercing.

**Files:**
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/parser/ThreadInfo.java`
- Test: `mjprof-core/src/test/java/com/performizeit/mjprof/core/parser/ThreadInfoAccessorsTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: `String ThreadInfo.tid()` — returns the `TID` property as a String regardless of whether it was stored as a `String` or a `Long`; null when absent. Tasks 2 and 3 depend on this.

- [ ] **Step 1: Write the failing tests**

Add to `ThreadInfoAccessorsTest`:

```java
  @Test
  void tidStoredAsALongIsReturnedAsAString() {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.TID, 42L);   // what JmxDataSourcePlugin and SelfDataSource store
    assertEquals("42", ti.tid());
  }

  @Test
  void tidStoredAsAStringIsUnchanged() {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.TID, "0x00007f9e");   // what the jstack text parser stores
    assertEquals("0x00007f9e", ti.tid());
  }

  @Test
  void tidIsNullWhenAbsent() {
    assertNull(new ThreadInfo().tid());
  }
```

- [ ] **Step 2: Run the tests to verify they fail**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=ThreadInfoAccessorsTest
```

Expected: `tidStoredAsALongIsReturnedAsAString` fails with `ClassCastException: class java.lang.Long cannot be cast to class java.lang.String`. The other two pass already.

- [ ] **Step 3: Make `tid()` coerce**

Replace the body of `tid()`:

```java
  public String tid() {
    Object value = getVal(ThreadInfoProps.TID);
    // the jstack text parser stores a hex String; the JMX and in-process sources store a Long
    return value == null ? null : value.toString();
  }
```

- [ ] **Step 4: Run the tests to verify they pass**

```bash
mvn -pl mjprof-core test -Dtest=ThreadInfoAccessorsTest
```

Expected: all pass.

- [ ] **Step 5: Run the full suite**

```bash
mvn test
```

Expected: BUILD SUCCESS. 111 tests passed before this task (92 `mjprof-core`, 19 `mjprof-cli`); expect 114 now. Report the actual numbers.

- [ ] **Step 6: Commit**

```bash
git add mjprof-core/src/main/java/com/performizeit/mjprof/core/parser/ThreadInfo.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/parser/ThreadInfoAccessorsTest.java
git commit -m "fix: ThreadInfo.tid() coerces instead of casting

The text parser stores tid as a hex String but the JMX source stores a
Long, so tid() threw ClassCastException on every JMX-sourced dump."
```

---

### Task 2: `SelfDataSource` — snapshot this process

Tests come first here, explicitly: the whole point of an in-process source is that it can be tested without any external JVM, so there is no excuse for writing it blind.

**Files:**
- Create: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/SelfDataSource.java`
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java`
- Test: `mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/SelfDataSourceTest.java`

**Interfaces:**
- Consumes: `MjprofException`, `Pipeline.add`, the `Attr` constants, `ThreadInfo.tid()` from Task 1.
- Produces, all used by Tasks 3–5:
  - `static Pipeline Pipeline.fromSelf()` — one snapshot, no CPU
  - `static Pipeline Pipeline.fromSelf(int count, int sleepMs)` — `count` snapshots, `sleepMs` apart, no CPU
  - `static Pipeline Pipeline.fromSelfWithCpu(int count, int sleepMs)` — same, plus `cpu_ns` per thread
  - package-private `SelfDataSource(int count, int sleepMs, boolean collectCpu)`
  - Each produced `ThreadInfo` carries `NAME` (String), `TID` (Long), `STATE` (String, a `Thread.State` name), `DAEMON` (Boolean), `STACK` (`Profile`), and `CPUNS` (Long) when CPU collection is on and supported.

- [ ] **Step 1: Write the failing tests**

Create `mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/SelfDataSourceTest.java`:

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.api.Plugin;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelfDataSourceTest {

  @Test
  void isNotRegisteredAsAMonad() {
    assertNull(SelfDataSource.class.getAnnotation(Plugin.class),
        "an in-process source is meaningless from the command line and must stay out of "
            + "supported_monads.txt");
  }

  @Test
  void snapshotContainsTheThreadRunningThisTest() {
    String thisThread = Thread.currentThread().getName();
    List<ThreadInfo> threads = Pipeline.fromSelf().threads();

    assertFalse(threads.isEmpty());
    assertTrue(threads.stream().anyMatch(t -> thisThread.equals(t.name())),
        "expected to find '" + thisThread + "' among " + threads.size() + " threads");
  }

  @Test
  void everyThreadHasANameAStateAndATid() {
    for (ThreadInfo t : Pipeline.fromSelf().threads()) {
      assertNotNull(t.name(), "name");
      assertNotNull(t.state(), "state of " + t.name());
      assertNotNull(t.tid(), "tid of " + t.name());
      // state must be a real Thread.State constant, not free text
      Thread.State.valueOf(t.state());
    }
  }

  @Test
  void theCallingThreadsStackIsCaptured() {
    String thisThread = Thread.currentThread().getName();
    ThreadInfo me = Pipeline.fromSelf()
        .contains(Attr.NAME, thisThread)
        .threads().stream().findFirst().orElseThrow();

    String stack = String.valueOf(me.stack());
    assertTrue(stack.contains("SelfDataSourceTest"),
        "the captured stack should mention this test class, was:\n" + stack);
  }

  @Test
  void countControlsHowManySnapshotsAreTaken() {
    assertEquals(3, Pipeline.fromSelf(3, 0).collect().size());
  }

  @Test
  void mergingSelfSamplesFoldsEachThreadIntoOneEntry() {
    List<ThreadInfo> merged = Pipeline.fromSelf(3, 0).merge(Attr.TID).threads();
    List<ThreadInfo> single = Pipeline.fromSelf(1, 0).threads();

    // same thread appearing in 3 samples collapses to one entry, so merging 3 samples
    // must not produce roughly 3x the threads of a single sample
    assertTrue(merged.size() < single.size() * 3,
        "merged=" + merged.size() + " single=" + single.size());
  }

  @Test
  void cpuTimeIsPopulatedWhenRequested() {
    Assumptions.assumeTrue(ManagementFactory.getThreadMXBean().isThreadCpuTimeSupported(),
        "this JVM does not support per-thread CPU time");

    List<ThreadInfo> threads = Pipeline.fromSelfWithCpu(1, 0).threads();
    assertTrue(threads.stream().anyMatch(t -> t.cpuNs() != null && t.cpuNs() > 0),
        "expected at least one thread with positive cpu_ns");
  }

  @Test
  void cpuTimeIsAbsentWhenNotRequested() {
    assertTrue(Pipeline.fromSelf().threads().stream().allMatch(t -> t.cpuNs() == null));
  }

  @Test
  void headerIsDatePrefixedSoWrittenSnapshotsResplit(@TempDir Path dir) throws IOException {
    // JmxDataSourcePlugin's header uses Date.toString(), which does NOT match the
    // ^\d\d\d\d-\d\d-\d\d pattern the stream parser splits on, so concatenated JMX dumps
    // collapse into one. The in-process source must not repeat that mistake.
    List<ThreadDump> two = Pipeline.fromSelf(2, 0).collect();
    Path file = dir.resolve("self.txt");
    Files.writeString(file, two.get(0) + "\n" + two.get(1) + "\n");

    assertEquals(2, Pipeline.fromFile(file.toString()).collect().size(),
        "two written snapshots must read back as two dumps");
  }

  @Test
  void groupingSelfSnapshotByStateCoversEveryThread() {
    List<ThreadInfo> all = Pipeline.fromSelf().threads();
    List<ThreadInfo> grouped = Pipeline.fromSelf().group(Attr.STATE).threads();

    assertTrue(grouped.size() <= all.size());
    assertTrue(grouped.stream().mapToInt(ThreadInfo::count).sum() > 0);
  }

  @Test
  void invalidSamplingParametersAreRejected() {
    assertThrows(MjprofException.class, () -> Pipeline.fromSelf(0, 0));
    assertThrows(MjprofException.class, () -> Pipeline.fromSelf(-1, 0));
    assertThrows(MjprofException.class, () -> Pipeline.fromSelf(1, -5));
    assertThrows(MjprofException.class, () -> Pipeline.fromSelfWithCpu(0, 0));
  }
}
```

Note `groupingSelfSnapshotByStateCoversEveryThread` deliberately does **not** assert that the grouped counts sum to exactly `all.size()`: the two calls take two separate snapshots of a live JVM, so the thread count can legitimately differ between them. Do not "tighten" that into an equality — it would be flaky.

- [ ] **Step 2: Run the tests to verify they fail**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=SelfDataSourceTest
```

Expected: compilation failure — `cannot find symbol: class SelfDataSource`, `method fromSelf()`.

- [ ] **Step 3: Write `SelfDataSource`**

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.model.Profile;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import com.performizeit.mjprof.core.plugin.types.DataSource;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Snapshots the threads of the JVM this code is running in, straight off {@link ThreadMXBean} — no
 * JMX connection, no network, no remote process.
 *
 * <p>Deliberately NOT annotated with @Plugin: an in-process source cannot mean anything on a command
 * line, and it must not appear in supported_monads.txt. Reach it through Pipeline.fromSelf().
 *
 * <p>Produces the same property shape as the remote JMX source, so downstream stages cannot tell the
 * two apart: name, tid, state, daemon, stack, and optionally cpu_ns.
 */
final class SelfDataSource implements DataSource, GeneratorHandler<ThreadDump> {

  /** Matches the ^\d\d\d\d-\d\d-\d\d prefix the stream parser splits concatenated dumps on. */
  private static final DateTimeFormatter HEADER_STAMP =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final int count;
  private final int sleepMs;
  private final boolean collectCpu;
  private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

  private int produced = 0;

  SelfDataSource(int count, int sleepMs, boolean collectCpu) {
    if (count < 1) {
      throw new MjprofException("fromSelf: count must be at least 1, got " + count);
    }
    if (sleepMs < 0) {
      throw new MjprofException("fromSelf: sleepMs must not be negative, got " + sleepMs);
    }
    this.count = count;
    this.sleepMs = sleepMs;
    this.collectCpu = collectCpu;
  }

  @Override
  public ThreadDump generate() {
    ThreadDump dump = new ThreadDump();
    dump.setHeader(LocalDateTime.now().format(HEADER_STAMP)
        + "\nFull thread dump of this process (pid " + ProcessHandle.current().pid() + ")");

    boolean withCpu = collectCpu
        && threadMXBean.isThreadCpuTimeSupported()
        && threadMXBean.isThreadCpuTimeEnabled();

    // lock info is not mapped into the model, so don't pay to collect it
    for (java.lang.management.ThreadInfo live : threadMXBean.dumpAllThreads(false, false)) {
      if (live == null) {
        continue;   // thread terminated between enumeration and dump
      }
      dump.addThreadInfo(convert(live, withCpu));
    }
    produced++;
    return dump;
  }

  private ThreadInfo convert(java.lang.management.ThreadInfo live, boolean withCpu) {
    HashMap<String, Object> props = new HashMap<>();
    props.put(ThreadInfoProps.NAME, live.getThreadName());
    props.put(ThreadInfoProps.TID, live.getThreadId());
    props.put(ThreadInfoProps.STATE, live.getThreadState().name());
    props.put(ThreadInfoProps.DAEMON, live.isDaemon());

    Profile profile = new Profile();
    profile.addSingle(live.getStackTrace());
    props.put(ThreadInfoProps.STACK, profile);

    if (withCpu) {
      long cpuNs = threadMXBean.getThreadCpuTime(live.getThreadId());
      if (cpuNs >= 0) {   // -1 means the thread is already gone or measurement is off
        props.put(ThreadInfoProps.CPUNS, cpuNs);
      }
    }
    return new ThreadInfo(props);
  }

  @Override
  public boolean isDone() {
    return produced >= count;
  }

  @Override
  public void sleepBetweenIteration() {
    if (sleepMs <= 0) {
      return;
    }
    try {
      Thread.sleep(sleepMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MjprofException("interrupted while sampling this process", e);
    }
  }
}
```

- [ ] **Step 4: Add the `fromSelf` factories to `Pipeline`**

Insert after `fromStdin()`, following the existing lazy-supplier pattern so the Pipeline stays reusable. Validation is eager (constructing `SelfDataSource` once at factory time to fail fast on bad parameters, exactly as `fromJmx` validates its `hostPort` eagerly):

```java
  /** Takes one snapshot of the threads of the JVM this code is running in. */
  public static Pipeline fromSelf() {
    return fromSelf(1, 0);
  }

  /**
   * Samples the threads of the JVM this code is running in.
   *
   * @param count   number of snapshots to take
   * @param sleepMs milliseconds to wait between snapshots; blocks the calling thread
   */
  public static Pipeline fromSelf(int count, int sleepMs) {
    return selfPipeline(count, sleepMs, false);
  }

  /**
   * As {@link #fromSelf(int, int)}, additionally recording per-thread CPU time, so several samples
   * merged on {@code Attr.TID} give a sampled CPU profile of this process.
   */
  public static Pipeline fromSelfWithCpu(int count, int sleepMs) {
    return selfPipeline(count, sleepMs, true);
  }

  private static Pipeline selfPipeline(int count, int sleepMs, boolean collectCpu) {
    new SelfDataSource(count, sleepMs, collectCpu);   // eager parameter validation
    return new Pipeline(() -> new SelfDataSource(count, sleepMs, collectCpu), List.of());
  }
```

If `Pipeline`'s private constructor takes stage suppliers rather than stages, match whatever the existing `fromStdin()` passes for its second argument — read it rather than copying the `List.of()` above blindly.

- [ ] **Step 5: Run the tests to verify they pass**

```bash
mvn -pl mjprof-core test -Dtest=SelfDataSourceTest
```

Expected: all 11 tests pass. If `headerIsDatePrefixedSoWrittenSnapshotsResplit` fails, the header format is wrong — fix `HEADER_STAMP`, not the test.

- [ ] **Step 6: Run the full suite and confirm the plugin index is untouched**

```bash
mvn clean package -DskipTests -q
grep -c . mjprof-cli/target/classes/supported_monads.txt          # expect exactly 31
grep SelfDataSource mjprof-cli/target/classes/supported_monads.txt && echo "BUG: it got registered"
mvn test
```

Expected: 31 entries, no `SelfDataSource`, all tests pass. Report the counts.

- [ ] **Step 7: Commit**

```bash
git add mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/SelfDataSource.java \
        mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/SelfDataSourceTest.java
git commit -m "feat: in-process data source for self thread analysis

Snapshots this JVM's own threads via ThreadMXBean, producing the same
property shape as the remote JMX source. API-only: no @Plugin, so it
stays out of supported_monads.txt and the expression DSL. Unlike the JMX
source its header is date-prefixed, so written snapshots re-split."
```

---

### Task 3: `mjprof-demo` module, application, and the first two endpoints

A vertical slice: module builds, app boots, two endpoints return real JSON about the running process.

**Files:**
- Create: `mjprof-demo/pom.xml`, `mjprof-demo/src/main/java/com/performizeit/mjprof/demo/MjprofDemoApplication.java`, `.../demo/ThreadView.java`, `.../demo/GroupCount.java`, `.../demo/ThreadViewMapper.java`, `.../demo/ThreadEndpoints.java`, `mjprof-demo/src/main/resources/application.properties`, `mjprof-demo/src/test/java/com/performizeit/mjprof/demo/ThreadEndpointsTest.java`
- Modify: root `pom.xml`

**Interfaces:**
- Consumes: `Pipeline.fromSelf()`, `group(Attr...)`, `contains(Attr,String)`, `stackTop(int)`, `threads()`, `ThreadInfo` accessors.
- Produces, used by Tasks 4–6:
  - `record ThreadView(String name, String state, String tid, boolean daemon, List<String> topFrames)`
  - `record GroupCount(String key, int threads)`
  - `ThreadViewMapper.toView(ThreadInfo, int maxFrames)` → `ThreadView`; `ThreadViewMapper.frames(ThreadInfo, int limit)` → `List<String>`
  - `ThreadEndpoints` with `GET /api/threads` and `GET /api/threads/states`

- [ ] **Step 1: Add the module to the root pom**

In root `pom.xml`, extend `<modules>`:

```xml
    <modules>
        <module>mjprof-core</module>
        <module>mjprof-cli</module>
        <module>mjprof-demo</module>
    </modules>
```

- [ ] **Step 2: Write `mjprof-demo/pom.xml`**

The demo cannot use `spring-boot-starter-parent` as its parent (the reactor parent is `com.performizeit:mjprof`), so it imports the Boot BOM instead and pins the Boot plugin version explicitly.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.performizeit</groupId>
        <artifactId>mjprof</artifactId>
        <version>1.1.0</version>
    </parent>

    <artifactId>mjprof-demo</artifactId>
    <name>mjprof Spring Boot demo</name>
    <description>Sample service that analyses its own threads through the mjprof-core API</description>

    <properties>
        <spring-boot.version>3.5.16</spring-boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>com.performizeit</groupId>
            <artifactId>mjprof-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

The reactor parent already contributes a `junit-jupiter` test dependency and imports `junit-bom` 5.12.2, while `spring-boot-starter-test` brings its own JUnit. After the first build, run `mvn -pl mjprof-demo dependency:tree | grep -i junit` and report the resolved version — if two different JUnit 5 minor versions appear, say so rather than ignoring it.

- [ ] **Step 3: Write the application, DTOs, mapper and controller**

`mjprof-demo/src/main/resources/application.properties`:

```properties
spring.application.name=mjprof-demo
server.port=8080
```

`MjprofDemoApplication.java`:

```java
package com.performizeit.mjprof.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MjprofDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(MjprofDemoApplication.class, args);
  }
}
```

`ThreadView.java`:

```java
package com.performizeit.mjprof.demo;

import java.util.List;

/**
 * JSON view of one thread. mjprof's ThreadInfo is a heterogeneous property bag whose keys depend on
 * the data source, and its accessors are not bean-shaped, so it is never serialized directly — this
 * record is the stable wire contract.
 */
public record ThreadView(String name, String state, String tid, boolean daemon,
                         List<String> topFrames) {
}
```

`GroupCount.java`:

```java
package com.performizeit.mjprof.demo;

/** One bucket of a grouped dump: the grouping key and how many threads collapsed into it. */
public record GroupCount(String key, int threads) {
}
```

`ThreadViewMapper.java`:

```java
package com.performizeit.mjprof.demo;

import com.performizeit.mjprof.core.parser.ThreadInfo;

import java.util.Arrays;
import java.util.List;

final class ThreadViewMapper {

  private ThreadViewMapper() {
  }

  static ThreadView toView(ThreadInfo thread, int maxFrames) {
    return new ThreadView(thread.name(), thread.state(), thread.tid(), thread.daemon(),
        frames(thread, maxFrames));
  }

  /**
   * Renders a thread's stack into individual frame lines. mjprof models a stack as a Profile tree
   * whose toString is the rendered form, so splitting that is the honest way to get frames out.
   */
  static List<String> frames(ThreadInfo thread, int limit) {
    if (thread.stack() == null || limit <= 0) {
      return List.of();
    }
    return Arrays.stream(thread.stack().toString().split("\n"))
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .limit(limit)
        .toList();
  }
}
```

`ThreadEndpoints.java`:

```java
package com.performizeit.mjprof.demo;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.pipeline.Pipeline;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Each endpoint demonstrates one capability of the mjprof-core API, applied to this very process.
 * Every call takes a fresh snapshot on the request thread.
 */
@RestController
@RequestMapping("/api/threads")
public class ThreadEndpoints {

  /** Every live thread, with the top frames of its stack. */
  @GetMapping
  public List<ThreadView> threads(@RequestParam(defaultValue = "3") int frames) {
    return Pipeline.fromSelf().threads().stream()
        .map(t -> ThreadViewMapper.toView(t, frames))
        .toList();
  }

  /** Thread-state histogram, built with the API's group() rather than by counting in Java. */
  @GetMapping("/states")
  public List<GroupCount> states() {
    return Pipeline.fromSelf()
        .group(Attr.STATE)
        .threads().stream()
        .map(t -> new GroupCount(t.state(), t.count()))
        .sorted((a, b) -> Integer.compare(b.threads(), a.threads()))
        .toList();
  }
}
```

- [ ] **Step 4: Write the endpoint test**

`mjprof-demo/src/test/java/com/performizeit/mjprof/demo/ThreadEndpointsTest.java`:

```java
package com.performizeit.mjprof.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Assertions check structure, never counts or timings — the subject under test is a live JVM whose
 * thread set changes between requests.
 */
@WebMvcTest(ThreadEndpoints.class)
class ThreadEndpointsTest {

  @Autowired
  MockMvc mockMvc;

  @Test
  void threadsEndpointListsThisProcessThreads() throws Exception {
    mockMvc.perform(get("/api/threads"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").exists())
        .andExpect(jsonPath("$[0].state").exists())
        .andExpect(jsonPath("$[0].tid").exists());
  }

  @Test
  void threadsEndpointHonoursTheFrameLimit() throws Exception {
    mockMvc.perform(get("/api/threads").param("frames", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].topFrames.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(1)));
  }

  @Test
  void statesEndpointReturnsAHistogram() throws Exception {
    mockMvc.perform(get("/api/threads/states"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].key").exists())
        .andExpect(jsonPath("$[0].threads").exists());
  }
}
```

- [ ] **Step 5: Build and run the demo tests**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-demo -am test
```

Expected: BUILD SUCCESS, 3 tests pass. If `$[0].topFrames` is empty for the first thread, that is legitimate — some JVM-internal threads have no Java stack. In that case change the assertion to target a thread that does have frames rather than relaxing it to nothing: assert `jsonPath("$[?(@.topFrames.length() > 0)]").exists()`.

- [ ] **Step 6: Confirm the whole reactor still builds and the CLI is untouched**

```bash
mvn clean package -DskipTests -q
grep -c . mjprof-cli/target/classes/supported_monads.txt   # expect 31
mvn test
```

Report the per-module test counts.

- [ ] **Step 7: Commit**

```bash
git add pom.xml mjprof-demo
git commit -m "feat: mjprof-demo Spring Boot module with thread and state endpoints

Third module depending on mjprof-core only. Exposes /api/threads and
/api/threads/states, both analysing the running process through the
typed Pipeline API. DTO records rather than serializing ThreadInfo,
which is a property bag and not bean-shaped."
```

---

### Task 4: Filtering and grouping endpoints

**Files:**
- Modify: `mjprof-demo/src/main/java/com/performizeit/mjprof/demo/ThreadEndpoints.java`
- Modify: `mjprof-demo/src/test/java/com/performizeit/mjprof/demo/ThreadEndpointsTest.java`

**Interfaces:**
- Consumes: everything from Task 3, plus `contains(Attr,String)`, `withoutNameSuffix()`, `group(Attr...)`, `stackTop(int)`.
- Produces: `GET /api/threads/state/{state}`, `GET /api/threads/pools`, `GET /api/threads/search`.

- [ ] **Step 1: Write the failing tests**

Add to `ThreadEndpointsTest`:

```java
  @Test
  void stateEndpointFiltersToThatStateOnly() throws Exception {
    mockMvc.perform(get("/api/threads/state/RUNNABLE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].state",
            org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("RUNNABLE"))));
  }

  @Test
  void stateEndpointAcceptsLowerCase() throws Exception {
    mockMvc.perform(get("/api/threads/state/runnable"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void unknownStateIsRejectedWithBadRequest() throws Exception {
    mockMvc.perform(get("/api/threads/state/NOT_A_STATE"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void poolsEndpointCollapsesNumberedThreadNames() throws Exception {
    mockMvc.perform(get("/api/threads/pools"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].key").exists())
        .andExpect(jsonPath("$[0].threads").exists());
  }

  @Test
  void searchEndpointFindsThreadsWhoseStackMentionsAFrame() throws Exception {
    // every thread serving this request has java.* frames on its stack
    mockMvc.perform(get("/api/threads/search").param("frame", "java"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void searchEndpointRequiresANonBlankFrame() throws Exception {
    mockMvc.perform(get("/api/threads/search").param("frame", "  "))
        .andExpect(status().isBadRequest());
  }
```

- [ ] **Step 2: Run them to verify they fail**

```bash
mvn -pl mjprof-demo -am test -Dtest=ThreadEndpointsTest
```

Expected: the four new endpoint tests fail with 404 (no handler), and the two validation tests fail because nothing rejects the input yet.

- [ ] **Step 3: Add the endpoints**

Append to `ThreadEndpoints`:

```java
  /** Threads in one state, e.g. /api/threads/state/BLOCKED — the API's contains() filter. */
  @GetMapping("/state/{state}")
  public List<ThreadView> byState(@PathVariable String state,
                                  @RequestParam(defaultValue = "5") int frames) {
    String wanted = state.toUpperCase(Locale.ROOT);
    try {
      Thread.State.valueOf(wanted);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "unknown thread state '" + state + "'; expected one of "
              + Arrays.toString(Thread.State.values()));
    }
    return Pipeline.fromSelf()
        .contains(Attr.STATE, wanted)
        .threads().stream()
        .map(t -> ThreadViewMapper.toView(t, frames))
        .toList();
  }

  /**
   * Thread pools by size: strips the numeric suffix from thread names so pool-1-thread-{1,2,3}
   * collapse into one entry, then groups on the name.
   */
  @GetMapping("/pools")
  public List<GroupCount> pools() {
    return Pipeline.fromSelf()
        .withoutNameSuffix()
        .group(Attr.NAME)
        .threads().stream()
        .map(t -> new GroupCount(t.name(), t.count()))
        .sorted((a, b) -> Integer.compare(b.threads(), a.threads()))
        .toList();
  }

  /** Threads whose stack mentions the given text, trimmed to the frames around it. */
  @GetMapping("/search")
  public List<ThreadView> search(@RequestParam String frame,
                                 @RequestParam(defaultValue = "10") int frames) {
    if (frame == null || frame.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "frame must not be blank");
    }
    return Pipeline.fromSelf()
        .contains(Attr.STACK, frame)
        .threads().stream()
        .map(t -> ThreadViewMapper.toView(t, frames))
        .toList();
  }
```

Add imports: `java.util.Arrays`, `java.util.Locale`, `org.springframework.http.HttpStatus`, `org.springframework.web.bind.annotation.PathVariable`, `org.springframework.web.server.ResponseStatusException`.

`contains(Attr.STACK, frame)` works because the `contains` filter compares `getVal(attr).toString()`, and a stack is a `Profile` whose `toString` is its rendered form. Note this is a plain substring match, not a regex — `matches(Attr.STACK, regex)` is the regex variant if you want one later.

- [ ] **Step 4: Run the tests to verify they pass**

```bash
mvn -pl mjprof-demo -am test -Dtest=ThreadEndpointsTest
```

Expected: all 9 tests pass. If `stateEndpointFiltersToThatStateOnly` returns an empty array, that is fine — `everyItem` on an empty list passes, and the request thread is usually RUNNABLE anyway.

- [ ] **Step 5: Commit**

```bash
git add mjprof-demo
git commit -m "feat: demo endpoints for state filtering, pools and stack search"
```

---

### Task 5: CPU sampling and stack-tree endpoints

The two endpoints that exercise the parts of the API a single snapshot cannot: multi-sample merging and the merged stack tree.

**Files:**
- Create: `mjprof-demo/src/main/java/com/performizeit/mjprof/demo/HotThread.java`
- Modify: `mjprof-demo/src/main/java/com/performizeit/mjprof/demo/ThreadEndpoints.java`
- Modify: `mjprof-demo/src/test/java/com/performizeit/mjprof/demo/ThreadEndpointsTest.java`

**Interfaces:**
- Consumes: `Pipeline.fromSelfWithCpu(int,int)`, `merge(Attr)`, `sortDesc(Attr)`, `treeText()`, `contains(Attr,String)`.
- Produces: `record HotThread(String name, String tid, Long cpuNs, int samples, List<String> topFrames)`; `GET /api/threads/hot`; `GET /api/threads/tree`.

- [ ] **Step 1: Write the failing tests**

Add to `ThreadEndpointsTest`:

```java
  @Test
  void hotEndpointRanksThreadsByCpu() throws Exception {
    mockMvc.perform(get("/api/threads/hot")
            .param("samples", "2").param("sleepMs", "1").param("top", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()", org.hamcrest.Matchers.lessThanOrEqualTo(5)))
        .andExpect(jsonPath("$[0].name").exists())
        .andExpect(jsonPath("$[0].samples").exists());
  }

  @Test
  void hotEndpointRejectsAnAbsurdSampleCount() throws Exception {
    mockMvc.perform(get("/api/threads/hot").param("samples", "0"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/threads/hot").param("samples", "1000"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void treeEndpointReturnsPlainText() throws Exception {
    mockMvc.perform(get("/api/threads/tree"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("text/plain"))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankOrNullString())));
  }
```

Add `import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;`.

- [ ] **Step 2: Run them to verify they fail**

```bash
mvn -pl mjprof-demo -am test -Dtest=ThreadEndpointsTest
```

Expected: 404s for the new paths.

- [ ] **Step 3: Write `HotThread` and the endpoints**

`HotThread.java`:

```java
package com.performizeit.mjprof.demo;

import java.util.List;

/**
 * One thread in a sampled CPU profile. {@code samples} is how many snapshots the thread appeared in,
 * which merging on the thread id accumulates as the count.
 */
public record HotThread(String name, String tid, Long cpuNs, int samples, List<String> topFrames) {
}
```

Append to `ThreadEndpoints`:

```java
  private static final int MAX_SAMPLES = 50;
  private static final int MAX_SLEEP_MS = 2_000;

  /**
   * Sampled CPU profile of this process: takes several CPU-aware snapshots, merges them per thread
   * id so cpu_ns sums and stacks fold into a tree, then ranks by CPU.
   *
   * <p>Blocks the request thread for roughly samples * sleepMs.
   */
  @GetMapping("/hot")
  public List<HotThread> hot(@RequestParam(defaultValue = "5") int samples,
                             @RequestParam(defaultValue = "100") int sleepMs,
                             @RequestParam(defaultValue = "10") int top,
                             @RequestParam(defaultValue = "5") int frames) {
    if (samples < 1 || samples > MAX_SAMPLES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "samples must be between 1 and " + MAX_SAMPLES + ", got " + samples);
    }
    if (sleepMs < 0 || sleepMs > MAX_SLEEP_MS) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "sleepMs must be between 0 and " + MAX_SLEEP_MS + ", got " + sleepMs);
    }
    if (top < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "top must be at least 1");
    }
    return Pipeline.fromSelfWithCpu(samples, sleepMs)
        .merge(Attr.TID)
        .sortDesc(Attr.CPU_NS)
        .threads().stream()
        .limit(top)
        .map(t -> new HotThread(t.name(), t.tid(), t.cpuNs(), t.count(),
            ThreadViewMapper.frames(t, frames)))
        .toList();
  }

  /**
   * All stacks of this process merged into one call tree, as text — the API's treeText() terminal,
   * which is what the CLI's {@code tree} monad prints.
   */
  @GetMapping(value = "/tree", produces = MediaType.TEXT_PLAIN_VALUE)
  public String tree(@RequestParam(required = false) String state) {
    Pipeline pipeline = Pipeline.fromSelf();
    if (state != null && !state.isBlank()) {
      pipeline = pipeline.contains(Attr.STATE, state.toUpperCase(Locale.ROOT));
    }
    return pipeline.treeText();
  }
```

Add import `org.springframework.http.MediaType`.

Note the guard rails: an endpoint that samples on the request thread is a self-inflicted denial of service if a caller asks for 10000 samples a second apart, so `samples` and `sleepMs` are bounded. Keep them.

- [ ] **Step 4: Run the tests to verify they pass**

```bash
mvn -pl mjprof-demo -am test -Dtest=ThreadEndpointsTest
```

Expected: all 12 tests pass. `hotEndpointRanksThreadsByCpu` must stay fast — it uses `samples=2&sleepMs=1`. If `$[0].cpuNs` is null on a JVM without per-thread CPU support, that is acceptable: the assertion checks `name` and `samples`, not `cpuNs`. Do not add an assertion on `cpuNs` being non-null; it is environment-dependent.

- [ ] **Step 5: Commit**

```bash
git add mjprof-demo
git commit -m "feat: demo endpoints for sampled CPU profile and merged stack tree"
```

---

### Task 6: Demo workload, demo README, and project docs

Without threads worth looking at, every endpoint returns the same handful of JVM internals. `DemoWorkload` creates named threads in distinct, recognizable states.

**Files:**
- Create: `mjprof-demo/src/main/java/com/performizeit/mjprof/demo/DemoWorkload.java`, `mjprof-demo/README.md`
- Modify: `README.md`, `CLAUDE.md`

**Interfaces:**
- Consumes: nothing from mjprof.
- Produces: a `@Component` that starts daemon threads named `demo-busy-N`, `demo-blocked-N`, `demo-waiting-N`. Not referenced by any endpoint — it exists to populate the process.

- [ ] **Step 1: Write `DemoWorkload`**

```java
package com.performizeit.mjprof.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Populates this process with threads in recognizable states, so the endpoints have something
 * interesting to report. All threads are daemons and all are named with a demo- prefix.
 *
 * <p>ponytail: busy threads burn CPU in a tight loop rather than doing useful work — fine for a
 * demo, and it makes /api/threads/hot show a clear winner. Replace with real work if this ever
 * becomes more than a sample.
 */
@Component
public class DemoWorkload {

  private final int busyThreads;
  private final List<Thread> threads = new ArrayList<>();
  private final Object contendedLock = new Object();
  private final CountDownLatch neverCountedDown = new CountDownLatch(1);
  private volatile boolean running = true;

  public DemoWorkload(@Value("${demo.busy-threads:2}") int busyThreads) {
    this.busyThreads = busyThreads;
  }

  @PostConstruct
  void start() {
    for (int i = 1; i <= busyThreads; i++) {
      start("demo-busy-" + i, this::burnCpu);
    }
    // one thread holds the lock and sleeps; two more pile up BLOCKED behind it
    start("demo-lock-holder", this::holdLock);
    for (int i = 1; i <= 2; i++) {
      start("demo-blocked-" + i, this::waitForLock);
    }
    start("demo-waiting-1", this::waitForever);
  }

  private void start(String name, Runnable body) {
    Thread t = new Thread(body, name);
    t.setDaemon(true);
    t.start();
    threads.add(t);
  }

  private void burnCpu() {
    long sink = 0;
    while (running) {
      for (int i = 0; i < 1_000_000; i++) {
        sink += i * 31L;
      }
      if (sink == Long.MIN_VALUE) {
        return;   // never true; keeps the JIT from eliminating the loop
      }
    }
  }

  private void holdLock() {
    while (running) {
      synchronized (contendedLock) {
        sleep(500);
      }
      sleep(5);
    }
  }

  private void waitForLock() {
    while (running) {
      synchronized (contendedLock) {
        sleep(1);
      }
    }
  }

  private void waitForever() {
    try {
      neverCountedDown.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      running = false;
    }
  }

  @PreDestroy
  void stop() {
    running = false;
    threads.forEach(Thread::interrupt);
  }
}
```

`@WebMvcTest` does not load `@Component` beans, so this does not run during the endpoint tests — which is deliberate: those tests must not depend on demo threads existing.

- [ ] **Step 2: Verify the workload actually produces the states it claims**

Start the app and check, rather than assuming:

```bash
cd /Users/lifey/github/mjprof
mvn -q -pl mjprof-demo -am spring-boot:run &
sleep 20
curl -s localhost:8080/api/threads/states
curl -s "localhost:8080/api/threads/state/BLOCKED" | head -40
curl -s "localhost:8080/api/threads/hot?samples=3&sleepMs=100&top=3"
curl -s "localhost:8080/api/threads/pools" | head -20
curl -s "localhost:8080/api/threads/tree" | head -30
curl -s "localhost:8080/api/threads/search?frame=DemoWorkload" | head -20
kill %1
```

Expected: `states` shows RUNNABLE plus BLOCKED and/or TIMED_WAITING; `hot` ranks `demo-busy-*` at the top; `search?frame=DemoWorkload` finds the demo threads. **Record the actual output of each call in your report** — this is the only end-to-end evidence that the API works through HTTP. If BLOCKED never appears, say so rather than claiming it does; lock contention is timing-dependent and the demo may need a longer hold.

- [ ] **Step 3: Write `mjprof-demo/README.md`**

````markdown
# mjprof-demo

A Spring Boot service that analyses **its own** threads through the `mjprof-core` API. No jstack, no
JMX connection, no second process — every endpoint snapshots the JVM it is running in via
`Pipeline.fromSelf()`.

## Run

```bash
mvn -pl mjprof-demo -am spring-boot:run
```

Listens on port 8080. `DemoWorkload` starts a few daemon threads at boot (CPU-burning, lock-blocked,
waiting) so the endpoints have something to show. Control the busy ones with
`--demo.busy-threads=4`.

## Endpoints

| Endpoint | mjprof capability it demonstrates |
|---|---|
| `GET /api/threads?frames=3` | `fromSelf()` + `threads()` — raw snapshot |
| `GET /api/threads/states` | `group(Attr.STATE)` — grouping collapses threads and reports `count` |
| `GET /api/threads/state/{state}` | `contains(Attr.STATE, …)` — filtering |
| `GET /api/threads/pools` | `withoutNameSuffix()` + `group(Attr.NAME)` — collapsing `pool-1-thread-N` |
| `GET /api/threads/search?frame=X` | `contains(Attr.STACK, X)` — substring match over rendered stacks |
| `GET /api/threads/hot?samples=5&sleepMs=100&top=10` | `fromSelfWithCpu()` + `merge(Attr.TID)` + `sortDesc(Attr.CPU_NS)` — sampled CPU profile |
| `GET /api/threads/tree[?state=RUNNABLE]` | `treeText()` — all stacks merged into one call tree |

## Notes

- Sampling runs on the request thread. `/hot` blocks for roughly `samples * sleepMs`, and both
  parameters are bounded so a caller cannot stall the service.
- `ThreadInfo` is a heterogeneous property bag and is not bean-shaped, so responses use explicit DTO
  records (`ThreadView`, `GroupCount`, `HotThread`) rather than serializing it directly.
- This module is a sample. It is not published and not part of the release.
````

- [ ] **Step 4: Update the project docs**

In the root `README.md`, in the Java API section, add a short pointer:

```markdown
A runnable example lives in `mjprof-demo` — a Spring Boot service whose endpoints analyse their own
process through this API (`Pipeline.fromSelf()`), including a sampled CPU profile and a merged stack
tree. See `mjprof-demo/README.md`.
```

Also document the new source in that section's factory list: `Pipeline.fromSelf()`, `fromSelf(count, sleepMs)` and `fromSelfWithCpu(count, sleepMs)` snapshot the current JVM with no JMX connection, and are API-only — there is no equivalent monad on the command line.

In `CLAUDE.md`:
- Add `mjprof-demo` to the Modules section: depends on `mjprof-core` only, Spring Boot 3.5.16 via a BOM import (it cannot use the Boot starter parent because the reactor parent is `com.performizeit:mjprof`), not published, not part of the release, excluded from the `native` profile.
- Note that `SelfDataSource` is intentionally un-annotated so it stays out of `supported_monads.txt`, and that this is the pattern for any future API-only source.
- **Leave the final `beans prime` line exactly as it is.**

- [ ] **Step 5: Full build and commit**

```bash
mvn clean package
```

Expected: BUILD SUCCESS across all three modules.

```bash
git add mjprof-demo README.md CLAUDE.md
git commit -m "feat: demo workload, demo README and docs for the in-process source"
```

---

### Task 7: Verification

No source changes. Run, observe, report honestly. A truthful "skipped because X" beats an optimistic pass.

**Files:** none.

- [ ] **Step 1: Clean full build**

```bash
cd /Users/lifey/github/mjprof
mvn clean package
```

Report BUILD SUCCESS/FAILURE and per-module test counts (114+ core, 19 cli, 12 demo expected — report actuals).

- [ ] **Step 2: The CLI golden gate — all ten expressions**

Capture from the freshly built jar and compare against `/tmp/mjprof-golden/`. Commands are in the Task 1 brief of the previous plan (`.superpowers/sdd/2026-07-31-core-cli-split-typed-api/task-1-brief.md`, Step 8): fixture `mjprof-core/src/test/res/test.txt`, jar `mjprof-cli/target/mjprof-cli-1.1.0-jar-with-dependencies.jar`, main class `com.performizeit.mjprof.cli.MJProf`, `-Dmacros.configFile=/dev/null`. Compare `03-sort` against `03-sort.fixround1.txt` and the other nine against their `NN-*.txt`. Report each by name OK or DIFF. If a golden file is missing, say which.

- [ ] **Step 3: Plugin index unchanged**

```bash
grep -c . mjprof-cli/target/classes/supported_monads.txt          # expect 31
grep -E "SelfDataSource|DumpsDataSource" mjprof-cli/target/classes/supported_monads.txt \
  && echo "BUG: an API-only source got registered"
```

- [ ] **Step 4: Core still has no external main-scope dependency**

```bash
mvn -q -pl mjprof-core dependency:tree
```

Expected: only test-scope junit. Any compile-scope third-party entry is a defect — name it. Spring must appear **only** under `mjprof-demo`:

```bash
mvn -q -pl mjprof-demo dependency:tree | grep -c springframework   # expect > 0
mvn -q -pl mjprof-core dependency:tree | grep -c springframework   # expect 0
mvn -q -pl mjprof-demo dependency:tree | grep -i junit             # report the resolved version(s)
```

- [ ] **Step 5: The native profile is unaffected**

```bash
mvn -Pnative package -DskipTests 2>&1 | tail -20
```

Expected: it reaches `native-maven-plugin` and fails only if this machine has no GraalVM, with the plugin's own "native-image is not installed in your JAVA_HOME" message. If a GraalVM **is** present, the build should produce `mjprof-cli/target/mjprof` — run `mjprof-cli/target/mjprof 'path/mjprof-core/src/test/res/test.txt/.count'` and report the output, since that also validates the generated `reflect-config.json`. If no GraalVM, report SKIPPED with the reason; do not install a toolchain.

- [ ] **Step 6: The demo runs for real**

Start it, hit every endpoint, kill it — same calls as Task 6 Step 2. Paste the actual response of each into the report. This is the only evidence the API works over HTTP against a live process.

- [ ] **Step 7: Report**

One line per check with PASS / FAIL / SKIPPED and its evidence, structured so a reader deciding whether to merge can scan it.

---

## Notes for the implementer

- Tests before implementation is a hard requirement in Task 2 — it was explicitly requested. Write the test file, watch it fail to compile, then implement.
- Do not give `SelfDataSource` a `@Plugin` annotation, and do not add a `self` monad to the CLI. It was explicitly scoped as API-only, and `supported_monads.txt` staying at 31 is the check.
- Assertions about a live JVM's threads must test structure, never counts or timings. Two snapshots of the same process legitimately differ. If you catch yourself asserting an exact thread count, stop.
- `mjprof-core` must stay dependency-free. If you find yourself wanting a JSON library in core, the answer is no — the demo module owns serialization.
- Known deferred items you may notice and should leave alone: `JmxDataSourcePlugin` calls `System.exit(1)` on a malformed host:port and NPEs on null credentials; `ThreadInfoAggregator` never sets `count` on singleton groups; `Terminal`-category plugins have no API counterpart; `MjprofException` lives in `core.pipeline` so `parser`/`model` cannot throw it. All tracked separately.
