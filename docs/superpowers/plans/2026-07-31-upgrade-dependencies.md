# Dependency Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade every dependency and Maven plugin in `pom.xml` to its latest GA version (verified against Maven Central on 2026-07-31).

**Architecture:** Single-file change (`pom.xml`), split into independently revertable commits: test dependency (JUnit BOM 5→6 major bump), runtime dependency (Gson), core build plugins, and the native-image plugin (0.x→1.x major bump). Each task's "test" is the Maven build/test cycle — this project has no new code, so the verification loop is `mvn test` / `mvn clean package`.

**Tech Stack:** Java 17, Maven, JUnit Jupiter, Gson, Reflections, GraalVM native-image (optional `native` profile).

## Global Constraints

- Only GA versions — no milestones, RCs, betas (`5.14.x`/`6.x` GA yes, `4.0.0-beta-1` no).
- Java baseline stays 17 (`maven.compiler.source/target` untouched).
- `org.reflections:reflections` stays at `0.10.2` — already the latest release on Maven Central.
- All edits are in `/Users/lifey/github/mjprof/pom.xml` unless a build failure forces a code change.
- The build executes code at `process-classes` (`PluginRepoBuilder` generates `supported_monads.txt`), so every verification must run with a working JDK 17.
- Do not commit if the verification step fails — diagnose or roll back the single version bump instead.

## Version Matrix (source of truth for all tasks)

| Artifact | Current | Target (latest GA) | pom.xml lines |
|---|---|---|---|
| `org.junit:junit-bom` | 5.10.3 | **6.1.2** | 184 |
| `com.google.code.gson:gson` | 2.12.1 | **2.14.0** | 214 |
| `org.reflections:reflections` | 0.10.2 | 0.10.2 (no change) | 202 |
| `org.codehaus.mojo:exec-maven-plugin` | 3.5.0 | **3.6.3** | 35 and 85 (two occurrences) |
| `org.graalvm.buildtools:native-maven-plugin` | 0.10.6 | **1.1.6** | 60 |
| `org.apache.maven.plugins:maven-resources-plugin` | 3.3.1 | **3.5.0** | 108 |
| `org.apache.maven.plugins:maven-assembly-plugin` | 3.7.1 | **3.8.0** | 135 |
| `org.jacoco:jacoco-maven-plugin` | 0.8.12 | **0.8.15** | 159 |

---

### Task 1: JUnit BOM 5.10.3 → 6.1.2

JUnit 6 is the latest GA line. It requires Java 17 (this project's baseline — OK). The test code uses only `org.junit.jupiter.api.Test` and `org.junit.jupiter.api.Assertions.*` (verified by grep across `src/test`), all of which exist unchanged in JUnit 6, so no test-code edits are expected.

**Files:**
- Modify: `pom.xml:181-188` (dependencyManagement junit-bom entry)

**Interfaces:**
- Consumes: nothing from other tasks.
- Produces: nothing later tasks rely on; independent commit.

- [ ] **Step 1: Establish baseline — run the full test suite on the current pom**

Run: `mvn test`
Expected: BUILD SUCCESS. If baseline fails, STOP — fix or report before any upgrade.

- [ ] **Step 2: Bump the BOM version and delete the stale milestone comment**

In `pom.xml`, replace:

```xml
            <dependency>
                <groupId>org.junit</groupId>
                <artifactId>junit-bom</artifactId>
                <version>5.10.3</version>
                <type>pom</type>
                <scope>import</scope>
                <!-- Not updating to 5.13.0-M2 as it's a milestone release -->
            </dependency>
```

with:

```xml
            <dependency>
                <groupId>org.junit</groupId>
                <artifactId>junit-bom</artifactId>
                <version>6.1.2</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
```

- [ ] **Step 3: Verify the new BOM resolves and all tests still run and pass**

Run: `mvn test`
Expected: BUILD SUCCESS, same test count as Step 1 (14 test classes, none skipped). Watch the Surefire summary line — JUnit 6 silently discovering zero tests would also print BUILD SUCCESS, so confirm `Tests run:` count is unchanged from baseline.

Fallback if JUnit 6 breaks (compile errors in tests or zero tests discovered and not quickly fixable): use `5.14.4` — the latest GA of the 5.x line — instead, and note why in the commit message.

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "build: upgrade junit-bom 5.10.3 -> 6.1.2"
```

---

### Task 2: Gson 2.12.1 → 2.14.0

**Files:**
- Modify: `pom.xml:211-215` (gson dependency)

**Interfaces:**
- Consumes: nothing from other tasks.
- Produces: nothing later tasks rely on; independent commit.

- [ ] **Step 1: Bump the version**

In `pom.xml`, replace:

```xml
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.12.1</version>
        </dependency>
```

with:

```xml
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.14.0</version>
        </dependency>
```

- [ ] **Step 2: Verify compile + tests**

Run: `mvn test`
Expected: BUILD SUCCESS, all tests pass. Gson is used at runtime (JSON output plugin), and its API has been stable across 2.x — no code changes expected.

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: upgrade gson 2.12.1 -> 2.14.0"
```

---

### Task 3: Core build plugins (exec, resources, assembly, jacoco)

Four plugin bumps that all affect the default (non-native) build, verified together by one full `mvn clean package`. exec-maven-plugin appears TWICE — inside the `native` profile and in the main `<build>` — both must move to 3.6.3.

**Files:**
- Modify: `pom.xml:35` (exec-maven-plugin in `native` profile)
- Modify: `pom.xml:85` (exec-maven-plugin in main build)
- Modify: `pom.xml:108` (maven-resources-plugin)
- Modify: `pom.xml:135` (maven-assembly-plugin)
- Modify: `pom.xml:159` (jacoco-maven-plugin)

**Interfaces:**
- Consumes: nothing from other tasks.
- Produces: nothing later tasks rely on; independent commit.

- [ ] **Step 1: Bump all four plugin versions (five edit sites)**

Replace `<version>3.5.0</version>` with `<version>3.6.3</version>` at BOTH exec-maven-plugin sites (pom.xml:35 and pom.xml:85 — confirm each sits directly under `<artifactId>exec-maven-plugin</artifactId>`).

Replace the maven-resources-plugin version:

```xml
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.3.1</version>
```

with:

```xml
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.5.0</version>
```

Replace the maven-assembly-plugin version:

```xml
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.7.1</version>
```

with:

```xml
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.8.0</version>
```

Replace the jacoco-maven-plugin version:

```xml
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.12</version>
```

with:

```xml
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.15</version>
```

- [ ] **Step 2: Verify no 3.5.0 / 3.3.1 / 3.7.1 / 0.8.12 remain**

Run: `grep -n "3.5.0\|3.3.1\|3.7.1\|0.8.12" pom.xml`
Expected: only the maven-resources-plugin `3.5.0` line (its new version) matches; nothing else.

- [ ] **Step 3: Verify the full default build end-to-end**

Run: `mvn clean package`
Expected: BUILD SUCCESS. Confirm all plugin-driven artifacts exist:
- `target/classes/supported_monads.txt` (exec-maven-plugin ran PluginRepoBuilder)
- `target/mjprof-1.1.0-jar-with-dependencies.jar` (assembly plugin)
- `target/site/jacoco/index.html` (jacoco report)
- Test summary shows the same test count as Task 1.

- [ ] **Step 4: Smoke-test the assembled jar**

Run: `java -jar target/mjprof-1.1.0-jar-with-dependencies.jar help | head -30`
Expected: monad help listing prints (proves supported_monads.txt was generated and packaged correctly).

- [ ] **Step 5: Commit**

```bash
git add pom.xml
git commit -m "build: upgrade exec/resources/assembly/jacoco maven plugins to latest GA"
```

---

### Task 4: native-maven-plugin 0.10.6 → 1.1.6

Major bump (0.x → 1.x, latest GA line of GraalVM buildtools). This only affects the optional `native` profile. Per CLAUDE.md, changes here can break the native build even when the jar works, so verify the profile as far as the local machine allows.

**Files:**
- Modify: `pom.xml:57-75` (native-maven-plugin in `native` profile)

**Interfaces:**
- Consumes: Task 3 must be done first (the `native` profile also runs the already-bumped exec-maven-plugin 3.6.3).
- Produces: nothing; final commit.

- [ ] **Step 1: Bump the version**

In `pom.xml`, replace:

```xml
                        <artifactId>native-maven-plugin</artifactId>
                        <version>0.10.6</version>
```

with:

```xml
                        <artifactId>native-maven-plugin</artifactId>
                        <version>1.1.6</version>
```

- [ ] **Step 2: Check whether GraalVM native-image is available locally**

Run: `native-image --version || echo "NO_NATIVE_IMAGE"`

- [ ] **Step 3a (native-image available): Run the full native build**

Run: `mvn -Pnative clean package`
Expected: BUILD SUCCESS and a `target/mjprof` native executable. Smoke test: `./target/mjprof help | head -5` prints monad help.

Fallback: if 1.1.6 fails against the installed GraalVM (buildtools 1.x tracks current GraalVM releases; the CI workflow pins `java-version: '17'` in `.github/workflows/release.yml`, whose GraalVM may be too old for 1.x), retry with `0.11.5` — the last GA of the 0.x line — and record the reason in the commit message. Do NOT change the CI workflow in this plan; if 1.1.6 needs a newer GraalVM in CI, report that to the user as a follow-up instead.

- [ ] **Step 3b (no native-image): Verify the profile still resolves**

Run: `mvn -Pnative clean process-classes`
Expected: BUILD SUCCESS through `process-classes` (plugin resolves and downloads; the native compile itself only runs at `package`, so this validates the version exists and the profile parses). Tell the user in the final summary that the native compile itself was NOT exercised locally and should be checked via the Release workflow.

- [ ] **Step 4: Run the default build once more to prove the profile change is inert for normal builds**

Run: `mvn clean package`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add pom.xml
git commit -m "build: upgrade native-maven-plugin 0.10.6 -> 1.1.6"
```
