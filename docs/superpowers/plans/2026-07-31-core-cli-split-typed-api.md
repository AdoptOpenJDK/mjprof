# mjprof core/cli split + typed pipeline API — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split mjprof into a dependency-free `mjprof-core` library and the `mjprof-cli` command, rename packages to `com.performizeit.mjprof.{core,cli}.*`, and add a strongly typed `Pipeline` API over the existing plugins driven by a synchronous, thread-free runner.

**Architecture:** Core holds the parser, model, plumbing interfaces, every `@Plugin` implementation, and the new `core.pipeline` package. CLI keeps `MJProf`, the expression DSL, macros, the plugin registry, the Reflections dependency, the `supported_monads.txt` code generation and the GraalVM `native` profile. The typed API takes plugin *instances* (constructor params are the typed arguments, checked by the compiler via intersection-type bounds) and runs them as a fold — `handleMsg` per dump, then a `handleDone` fan-forward — with no threads and no static state.

**Tech Stack:** Java 17, Maven multi-module, JUnit 5 (junit-jupiter 5.12.2 via junit-bom), jacoco, GraalVM native-maven-plugin 0.10.6, org.reflections 0.10.2 (cli only).

**Spec:** `docs/superpowers/specs/2026-07-31-core-cli-split-typed-api-design.md`

## Global Constraints

- Java source/target **17**. `project.build.sourceEncoding` = **UTF-8**.
- groupId `com.performizeit`, parent artifactId `mjprof`, version **1.1.0** for all modules.
- `mjprof-core` has **no** external main-scope dependency. JDK only. junit-jupiter test scope only.
- `mjprof-cli` depends on `mjprof-core` and `org.reflections:reflections:0.10.2` (with the existing `com.google.code.findbugs:jsr305` exclusion).
- Package renames, exhaustive — no file keeps an old package:
  - `com.performizeit.mjprof.api` → `com.performizeit.mjprof.core.api`
  - `com.performizeit.mjprof.model` → `com.performizeit.mjprof.core.model`
  - `com.performizeit.mjprof.parser` → `com.performizeit.mjprof.core.parser`
  - `com.performizeit.plumbing` → `com.performizeit.mjprof.core.plumbing`
  - `com.performizeit.mjprof.plugin.types` → `com.performizeit.mjprof.core.plugin.types`
  - `com.performizeit.mjprof.plugins` → `com.performizeit.mjprof.core.plugins`
  - `com.performizeit.jmxsupport` → `com.performizeit.mjprof.core.jmx`
  - `com.performizeit.mjprof.MJProf` → `com.performizeit.mjprof.cli.MJProf`
  - `com.performizeit.mjprof.monads` → `com.performizeit.mjprof.cli.monads`
  - `com.performizeit.mjprof.plugin` (`PluginRepoBuilder`, `PluginRepositoryScanner`, `PluginUtils`) → `com.performizeit.mjprof.cli.plugin`
- Everything stays under the `com.performizeit` prefix so `Reflections("com.performizeit")` in `PluginRepoBuilder` keeps working unchanged.
- Do **not** change expression parsing, macro expansion, or CLI output formatting. The golden-file check in Task 1 is the gate.
- Do **not** add `@Plugin` to any new class. `DumpsDataSource` must not appear in `supported_monads.txt`.
- The API never writes to stdout/stderr and never starts a thread.
- GPLv3 file headers: new files in this project carry no header (recent files like `Pipe.java`, `ThreadInfoAggregator.java` have none). Do not add one.

---

## File Structure

**Created — `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/`**

| File | Responsibility |
|---|---|
| `MjprofException.java` | Unchecked exception for every API-surfaced failure |
| `PipelineRunner.java` | Synchronous fold: drive a source, push dumps through stages, fan `handleDone` forward |
| `DumpsDataSource.java` | In-memory `DataSource` over a `Collection<ThreadDump>` |
| `Pipeline.java` | The public builder: sources, `map` overloads, typed convenience methods, terminals |

**Modified — core**

| File | Change |
|---|---|
| `core/api/Attr.java` | Add typed constants (`Attr.NAME` … `Attr.COUNT`) |
| `core/parser/ThreadInfo.java` | Add typed accessors (`name()`, `state()`, `cpuNs()`, …) |
| `core/plugins/mappers/GroupByProp.java` | Add `GroupByProp(List<String>)` for multi-attribute grouping |

**Modified — build/infra**

`pom.xml` (becomes parent), `mjprof-core/pom.xml` (new), `mjprof-cli/pom.xml` (new), `src/main/scripts/mjprof`, `src/main/scripts/mjprof.bat`, `.github/workflows/release.yml`, `jreleaser.yml`, `README.md`, `CLAUDE.md`.

**Task order and dependencies**

```
Task 1 (module split + rename)  ← everything depends on this
  ├── Task 2 (Attr constants, ThreadInfo accessors)
  │     └── Task 4 (Pipeline: sources, map, terminals)   ← needs Task 3
  ├── Task 3 (MjprofException + PipelineRunner)
  │     └── Task 4
  │           ├── Task 5 (GroupByProp multi-attr + group/merge + STACK guard)
  │           ├── Task 6 (typed convenience methods)
  │           └── Task 7 (DumpsDataSource + fromDumps + sampled profile)
  └── Task 8 (docs + text-persistence limits)
        └── Task 9 (full build + native verification)
```

---

### Task 1: Split into two modules and rename packages

Mechanical but large (~90 files). It cannot be split further: a half-moved tree does not compile. The golden-file capture in Step 1 happens **before** any move and is the only proof the rename changed no behavior.

**Files:**
- Create: `mjprof-core/pom.xml`, `mjprof-cli/pom.xml`
- Modify: `pom.xml` (becomes `packaging=pom` parent), `src/main/scripts/mjprof`, `src/main/scripts/mjprof.bat`, `.github/workflows/release.yml`, `jreleaser.yml`
- Move: all of `src/main/java`, `src/main/resources`, `src/main/scripts`, `src/test/java`, `src/test/res` into the two modules
- Test: existing test classes, relocated; plus `/tmp/mjprof-golden/` scratch files (not committed)

**Interfaces:**
- Consumes: nothing.
- Produces: the package names listed in Global Constraints. Every later task's imports depend on them. Also produces module roots `mjprof-core/` and `mjprof-cli/` — later tasks put core code in `mjprof-core/src/main/java/com/performizeit/mjprof/core/...` and core tests in `mjprof-core/src/test/java/com/performizeit/mjprof/core/...`.

- [ ] **Step 1: Capture the golden baseline BEFORE touching anything**

Build the current tree and record CLI output for a set of expressions. These become the regression gate.

```bash
cd /Users/lifey/github/mjprof
mvn -q clean package -DskipTests
mkdir -p /tmp/mjprof-golden
JAR=target/mjprof-1.1.0-jar-with-dependencies.jar
run() { java -cp "$JAR" -Dmacros.configFile=/dev/null com.performizeit.mjprof.MJProf "$1" > "/tmp/mjprof-golden/$2.txt" 2>&1; }
run 'path/src/test/res/test.txt/.list'                                    01-list
run 'path/src/test/res/test.txt/.contains/name,main/'                     02-contains
run 'path/src/test/res/test.txt/.sort/state/'                             03-sort
run 'path/src/test/res/test.txt/.group/state/'                            04-group
run 'path/src/test/res/test.txt/.merge'                                   05-merge
run 'path/src/test/res/test.txt/.tree'                                    06-tree
run 'path/src/test/res/test.txt/.running'                                 07-macro-running
run 'path/src/test/res/test.txt/.-jvm'                                    08-macro-nojvm
run 'path/src/test/res/test.txt/.contains/name,main/.bottom/3/'           09-bottom
run 'path/src/test/res/test.txt/.count'                                   10-count
run 'help'                                                                11-help
wc -l /tmp/mjprof-golden/*.txt
cp target/classes/supported_monads.txt /tmp/mjprof-golden/supported_monads.before.txt
wc -l /tmp/mjprof-golden/supported_monads.before.txt   # expect 31
```

If any of `01`–`11` is empty or contains a stack trace, stop and inspect — a broken baseline is worse than none. `11-help` will contain the old class names in the synopsis only if the synopsis prints them; expect it to differ later only in the "Expanded Expression" line, which is fine to eyeball.

- [ ] **Step 2: Create the module directory skeleton and move files with `git mv`**

```bash
cd /Users/lifey/github/mjprof
mkdir -p mjprof-core/src/main/java/com/performizeit/mjprof/core
mkdir -p mjprof-core/src/test/java/com/performizeit/mjprof/core
mkdir -p mjprof-core/src/test/res
mkdir -p mjprof-cli/src/main/java/com/performizeit/mjprof/cli
mkdir -p mjprof-cli/src/main/resources mjprof-cli/src/main/scripts
mkdir -p mjprof-cli/src/test/java/com/performizeit/mjprof/cli

C=mjprof-core/src/main/java/com/performizeit/mjprof/core
L=mjprof-cli/src/main/java/com/performizeit/mjprof/cli
S=src/main/java/com/performizeit/mjprof

# core
git mv $S/api            $C/api
git mv $S/model          $C/model
git mv $S/parser         $C/parser
git mv $S/plugins        $C/plugins
git mv $S/plugin/types   $C/plugin_types_tmp && mkdir -p $C/plugin && git mv $C/plugin_types_tmp $C/plugin/types
git mv src/main/java/com/performizeit/plumbing   $C/plumbing
git mv src/main/java/com/performizeit/jmxsupport $C/jmx

# cli
git mv $S/MJProf.java                       $L/MJProf.java
git mv $S/monads                            $L/monads
mkdir -p $L/plugin
git mv $S/plugin/PluginRepoBuilder.java        $L/plugin/PluginRepoBuilder.java
git mv $S/plugin/PluginRepositoryScanner.java  $L/plugin/PluginRepositoryScanner.java
git mv $S/plugin/PluginUtils.java              $L/plugin/PluginUtils.java

git mv src/main/resources/internal.macros.properties mjprof-cli/src/main/resources/
git mv src/main/scripts/mjprof     mjprof-cli/src/main/scripts/mjprof
git mv src/main/scripts/mjprof.bat mjprof-cli/src/main/scripts/mjprof.bat
git mv src/main/scripts/jstackOverTime.sh mjprof-cli/src/main/scripts/jstackOverTime.sh

# tests: core gets everything except the expression/registry tests
CT=mjprof-core/src/test/java/com/performizeit/mjprof/core
LT=mjprof-cli/src/test/java/com/performizeit/mjprof/cli
T=src/test/java/com/performizeit/mjprof
git mv $T/model    $CT/model
git mv $T/parser   $CT/parser
git mv $T/plugins  $CT/plugins
mkdir -p $LT/plugin
git mv $T/MJProfTest.java $LT/MJProfTest.java
git mv $T/MJStepTest.java $LT/MJStepTest.java
git mv $T/plugin/PluginTest.java                        $LT/plugin/PluginTest.java
git mv $T/plugin/PluginWithDefaultConstructorTest.java  $LT/plugin/PluginWithDefaultConstructorTest.java
git mv $T/plugin/PluginWithParameterConstructorTest.java $LT/plugin/PluginWithParameterConstructorTest.java
git mv src/test/res/test.txt      mjprof-core/src/test/res/test.txt
git mv src/test/res/test1.txt     mjprof-core/src/test/res/test1.txt
git mv src/test/res/test2.txt     mjprof-core/src/test/res/test2.txt
git mv src/test/res/resTest2.txt  mjprof-core/src/test/res/resTest2.txt

find src -type d -empty -delete
```

- [ ] **Step 3: Rewrite package and import statements**

Order matters — longest prefix first, or `...plugin.types` gets mangled by the `...plugin` rule. Note there is no bare `com.performizeit.mjprof.plugin` → `...cli.plugin` sed: `PluginUtils`/`PluginRepoBuilder` live in a package whose declaration is exactly `com.performizeit.mjprof.plugin`, handled by the last two rules.

```bash
cd /Users/lifey/github/mjprof
FILES=$(find mjprof-core/src mjprof-cli/src -name '*.java')
sed -i '' \
  -e 's/com\.performizeit\.mjprof\.plugin\.types/com.performizeit.mjprof.core.plugin.types/g' \
  -e 's/com\.performizeit\.mjprof\.plugins/com.performizeit.mjprof.core.plugins/g' \
  -e 's/com\.performizeit\.mjprof\.api/com.performizeit.mjprof.core.api/g' \
  -e 's/com\.performizeit\.mjprof\.model/com.performizeit.mjprof.core.model/g' \
  -e 's/com\.performizeit\.mjprof\.parser/com.performizeit.mjprof.core.parser/g' \
  -e 's/com\.performizeit\.plumbing/com.performizeit.mjprof.core.plumbing/g' \
  -e 's/com\.performizeit\.jmxsupport/com.performizeit.mjprof.core.jmx/g' \
  -e 's/com\.performizeit\.mjprof\.monads/com.performizeit.mjprof.cli.monads/g' \
  -e 's/^package com\.performizeit\.mjprof\.plugin;/package com.performizeit.mjprof.cli.plugin;/' \
  -e 's/com\.performizeit\.mjprof\.plugin\.Plugin\([A-Za-z]*\)/com.performizeit.mjprof.cli.plugin.Plugin\1/g' \
  $FILES

# MJProf's own package declaration and the tests that import it
sed -i '' -e 's/^package com\.performizeit\.mjprof;/package com.performizeit.mjprof.cli;/' \
  mjprof-cli/src/main/java/com/performizeit/mjprof/cli/MJProf.java \
  mjprof-cli/src/test/java/com/performizeit/mjprof/cli/MJProfTest.java \
  mjprof-cli/src/test/java/com/performizeit/mjprof/cli/MJStepTest.java
sed -i '' -e 's/^package com\.performizeit\.mjprof\.plugin;/package com.performizeit.mjprof.cli.plugin;/' \
  mjprof-cli/src/test/java/com/performizeit/mjprof/cli/plugin/*.java

# no old package name may survive
grep -rn "com\.performizeit\.plumbing\|com\.performizeit\.jmxsupport" mjprof-core mjprof-cli && echo "LEFTOVER — fix before continuing"
grep -rln "^package com\.performizeit\.mjprof;" mjprof-core mjprof-cli && echo "LEFTOVER — fix before continuing"
```

Test files declare `package com.performizeit.mjprof.plugins...` etc. and are covered by the same rules. `PluginTest` and the two `PluginWith*ConstructorTest` fixtures are annotated `@Plugin` and now sit in `com.performizeit.mjprof.cli.plugin` — still under `com.performizeit`, so `PluginTest`'s own registry lookups keep working.

- [ ] **Step 4: Write the parent pom**

Replace `pom.xml` with a parent that holds shared config only. Everything build-related that was in it moves to `mjprof-cli/pom.xml` (Step 6) except jacoco and dependencyManagement, which are shared.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.performizeit</groupId>
    <artifactId>mjprof</artifactId>
    <version>1.1.0</version>
    <packaging>pom</packaging>

    <name>A command line monadic java profiler</name>
    <url>https://github.com/AdoptOpenJDK/mjprof</url>

    <developers>
        <developer>
            <id>lifey</id>
            <name>Haim Yadid</name>
            <email>haim.yadid@gmail.com</email>
        </developer>
    </developers>

    <modules>
        <module>mjprof-core</module>
        <module>mjprof-cli</module>
    </modules>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.junit</groupId>
                <artifactId>junit-bom</artifactId>
                <version>5.12.2</version>
                <type>pom</type>
                <scope>import</scope>
                <!-- 5.13.x is milestone-only as of 2026-07-31; stay on latest stable -->
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.13</version>
                <executions>
                    <execution>
                        <id>jacoco-prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>jacoco-report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 5: Write `mjprof-core/pom.xml`**

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.performizeit</groupId>
        <artifactId>mjprof</artifactId>
        <version>1.1.0</version>
    </parent>

    <artifactId>mjprof-core</artifactId>
    <name>mjprof core library</name>
    <description>Thread dump parsing, model, plugins and the typed pipeline API</description>
</project>
```

No dependencies block: junit-jupiter is inherited from the parent's `<dependencies>`. No external main-scope dependency by design.

- [ ] **Step 6: Write `mjprof-cli/pom.xml`**

Everything build-related from the old root pom lands here, with the two exec class names updated to `...cli.plugin.*` and the assembly/native `mainClass` to `...cli.MJProf`.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.performizeit</groupId>
        <artifactId>mjprof</artifactId>
        <version>1.1.0</version>
    </parent>

    <artifactId>mjprof-cli</artifactId>
    <name>mjprof command line</name>
    <description>The mjprof command: expression DSL, macros and plugin registry</description>

    <dependencies>
        <dependency>
            <groupId>com.performizeit</groupId>
            <artifactId>mjprof-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.reflections</groupId>
            <artifactId>reflections</artifactId>
            <version>0.10.2</version>
            <exclusions>
                <exclusion>  <!-- We don't need findbugs as a dependency  -->
                    <groupId>com.google.code.findbugs</groupId>
                    <artifactId>jsr305</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <profiles>
        <profile>
            <id>native</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>exec-maven-plugin</artifactId>
                        <version>3.5.0</version>
                        <executions>
                            <execution>
                                <id>create-plugin-reflection-metadata</id>
                                <goals>
                                    <goal>exec</goal>
                                </goals>
                                <phase>prepare-package</phase>
                                <configuration>
                                    <executable>java</executable>
                                    <arguments>
                                        <argument>
                                            -agentlib:native-image-agent=config-output-dir=${project.build.outputDirectory}/META-INF/native-image
                                        </argument>
                                        <argument>-classpath</argument>
                                        <classpath/>
                                        <argument>com.performizeit.mjprof.cli.plugin.PluginRepositoryScanner</argument>
                                    </arguments>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                    <plugin>
                        <groupId>org.graalvm.buildtools</groupId>
                        <artifactId>native-maven-plugin</artifactId>
                        <version>0.10.6</version>
                        <extensions>true</extensions>
                        <executions>
                            <execution>
                                <id>build-native</id>
                                <goals>
                                    <goal>compile-no-fork</goal>
                                </goals>
                                <phase>package</phase>
                            </execution>
                        </executions>
                        <configuration>
                            <mainClass>com.performizeit.mjprof.cli.MJProf</mainClass>
                            <fallback>false</fallback>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.5.0</version>
                <executions>
                    <execution>
                        <id>enumerate-supported-plugins</id>
                        <goals>
                            <goal>exec</goal>
                        </goals>
                        <phase>process-classes</phase>
                        <configuration>
                            <executable>java</executable>
                            <arguments>
                                <argument>-classpath</argument>
                                <classpath/>
                                <argument>com.performizeit.mjprof.cli.plugin.PluginRepoBuilder</argument>
                                <argument>${project.build.outputDirectory}/supported_monads.txt</argument>
                            </arguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.5.0</version>
                <executions>
                    <execution>
                        <id>copy-property-files</id>
                        <phase>process-classes</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${basedir}/target/classes</outputDirectory>
                            <encoding>UTF-8</encoding>
                            <resources>
                                <resource>
                                    <directory>${basedir}/src</directory>
                                    <includes>
                                        <include>**/*.properties</include>
                                    </includes>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.8.0</version>
                <executions>
                    <execution>
                        <id>create-jars-with-dependencies</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                        <configuration>
                            <archive>
                                <manifest>
                                    <mainClass>com.performizeit.mjprof.cli.MJProf</mainClass>
                                </manifest>
                            </archive>
                            <descriptorRefs>
                                <descriptorRef>jar-with-dependencies</descriptorRef>
                            </descriptorRefs>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 7: Build and run all tests**

```bash
cd /Users/lifey/github/mjprof
mvn clean package
```

Expected: `BUILD SUCCESS`, both modules built, all pre-existing tests pass, and `mjprof-cli/target/classes/supported_monads.txt` exists.

If `PluginRepoBuilder` fails to find plugins (empty file), the cause is Reflections not scanning the core jar from the exec classpath. Fix by making the scan explicit rather than guessing: check that `mvn -pl mjprof-cli dependency:build-classpath` lists `mjprof-core`, and that the classes inside it are under `com/performizeit/mjprof/core/plugins/`.

- [ ] **Step 8: Verify the plugin index and diff the golden output**

```bash
cd /Users/lifey/github/mjprof
sort mjprof-cli/target/classes/supported_monads.txt > /tmp/mjprof-golden/monads.after.txt
wc -l /tmp/mjprof-golden/monads.after.txt        # expect 31, same as before
grep -c "com.performizeit.mjprof.core.plugins" /tmp/mjprof-golden/monads.after.txt  # expect 31

JAR=mjprof-cli/target/mjprof-cli-1.1.0-jar-with-dependencies.jar
run() { java -cp "$JAR" -Dmacros.configFile=/dev/null com.performizeit.mjprof.cli.MJProf "$1" > "/tmp/mjprof-golden/$2.after.txt" 2>&1; }
run 'path/mjprof-core/src/test/res/test.txt/.list'                          01-list
run 'path/mjprof-core/src/test/res/test.txt/.contains/name,main/'           02-contains
run 'path/mjprof-core/src/test/res/test.txt/.sort/state/'                   03-sort
run 'path/mjprof-core/src/test/res/test.txt/.group/state/'                  04-group
run 'path/mjprof-core/src/test/res/test.txt/.merge'                         05-merge
run 'path/mjprof-core/src/test/res/test.txt/.tree'                          06-tree
run 'path/mjprof-core/src/test/res/test.txt/.running'                       07-macro-running
run 'path/mjprof-core/src/test/res/test.txt/.-jvm'                          08-macro-nojvm
run 'path/mjprof-core/src/test/res/test.txt/.contains/name,main/.bottom/3/' 09-bottom
run 'path/mjprof-core/src/test/res/test.txt/.count'                         10-count

for n in 01-list 02-contains 03-sort 04-group 05-merge 06-tree 07-macro-running 08-macro-nojvm 09-bottom 10-count; do
  # the only legitimate difference is the fixture path echoed in headers, if any
  diff <(sed 's#mjprof-core/src/test/res#src/test/res#g' "/tmp/mjprof-golden/$n.after.txt") \
       "/tmp/mjprof-golden/$n.txt" > /dev/null && echo "OK   $n" || echo "DIFF $n"
done
```

Expected: `OK` for all ten. Any `DIFF` is a real regression from the rename — investigate before committing, do not rationalize it.

- [ ] **Step 9: Update the launcher scripts**

`mjprof-cli/src/main/scripts/mjprof` — the jar name is also being corrected from the stale `1.0`:

```bash
#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -cp "$DIR/mjprof-cli-1.1.0-jar-with-dependencies.jar:$DIR/plugins/*" -Dmacros.configFile=${DIR}/macros.properties com.performizeit.mjprof.cli.MJProf $*
```

(`$JAVA_HOME/lib/tools.jar` is dropped — it has not existed since Java 9 and this is a Java 17 project.)

Apply the same two substitutions to `mjprof-cli/src/main/scripts/mjprof.bat`: jar name and main class. Read the file first and preserve its Windows-specific structure and line endings.

- [ ] **Step 10: Repoint release automation**

In `.github/workflows/release.yml` and `jreleaser.yml`, every path referring to the built artifact moves from `target/` to `mjprof-cli/target/`, and artifact names from `mjprof-1.1.0` to `mjprof-cli-1.1.0`. Read both files, update each occurrence, and grep to confirm nothing still points at the old root `target/`:

```bash
grep -n "target/\|mjprof-1\.\|artifactId" .github/workflows/release.yml jreleaser.yml
```

Do not restructure the workflows; change only paths and artifact names.

- [ ] **Step 11: Commit**

```bash
cd /Users/lifey/github/mjprof
git add -A
git commit -m "refactor: split into mjprof-core and mjprof-cli modules

Moves the library (parser, model, plumbing, plugins, jmx) into
mjprof-core with no external dependencies, and the command (MJProf,
expression DSL, macros, plugin registry, Reflections, supported_monads
codegen, native profile) into mjprof-cli.

Packages renamed to com.performizeit.mjprof.{core,cli}.*; plumbing and
jmxsupport fold under core. CLI output verified byte-identical against a
golden capture of ten expressions taken before the split."
```

---

### Task 2: Typed attributes and thread accessors

Small, independent, and every later task reads better for it.

**Files:**
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/api/Attr.java`
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/parser/ThreadInfo.java`
- Test: `mjprof-core/src/test/java/com/performizeit/mjprof/core/api/AttrTest.java` (create), `mjprof-core/src/test/java/com/performizeit/mjprof/core/parser/ThreadInfoAccessorsTest.java` (create)

**Interfaces:**
- Consumes: package names from Task 1.
- Produces:
  - `Attr` constants: `Attr.NAME`, `TID`, `NID`, `DAEMON`, `PRIO`, `STATUS`, `STATE`, `CPU_NS`, `WALL_MS`, `CPU_PCT`, `STACK`, `LOS`, `COUNT` — all `public static final Attr`.
  - `ThreadInfo` accessors: `String name()`, `String state()`, `String status()`, `String tid()`, `Long cpuNs()`, `Long wallMs()`, `Double cpuPct()`, `int count()`, `Object stack()`, `boolean daemon()`.

- [ ] **Step 1: Write the failing tests**

`mjprof-core/src/test/java/com/performizeit/mjprof/core/api/AttrTest.java`:

```java
package com.performizeit.mjprof.core.api;

import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttrTest {

  @Test
  void constantsMatchThreadInfoProps() {
    assertEquals(ThreadInfoProps.NAME, Attr.NAME.getAttrName());
    assertEquals(ThreadInfoProps.TID, Attr.TID.getAttrName());
    assertEquals(ThreadInfoProps.NID, Attr.NID.getAttrName());
    assertEquals(ThreadInfoProps.DAEMON, Attr.DAEMON.getAttrName());
    assertEquals(ThreadInfoProps.PRIO, Attr.PRIO.getAttrName());
    assertEquals(ThreadInfoProps.STATUS, Attr.STATUS.getAttrName());
    assertEquals(ThreadInfoProps.STATE, Attr.STATE.getAttrName());
    assertEquals(ThreadInfoProps.CPUNS, Attr.CPU_NS.getAttrName());
    assertEquals(ThreadInfoProps.WALL, Attr.WALL_MS.getAttrName());
    assertEquals(ThreadInfoProps.CPU_PREC, Attr.CPU_PCT.getAttrName());
    assertEquals(ThreadInfoProps.STACK, Attr.STACK.getAttrName());
    assertEquals(ThreadInfoProps.LOS, Attr.LOS.getAttrName());
    assertEquals(ThreadInfoProps.COUNT, Attr.COUNT.getAttrName());
  }

  @Test
  void customAttrStillWorks() {
    assertEquals("whatever", new Attr("whatever").getAttrName());
  }
}
```

`mjprof-core/src/test/java/com/performizeit/mjprof/core/parser/ThreadInfoAccessorsTest.java`:

```java
package com.performizeit.mjprof.core.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadInfoAccessorsTest {

  private ThreadInfo sample() {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, "worker-1");
    ti.setVal(ThreadInfoProps.STATE, "RUNNABLE");
    ti.setVal(ThreadInfoProps.STATUS, "runnable");
    ti.setVal(ThreadInfoProps.TID, "0x00007f");
    ti.setVal(ThreadInfoProps.CPUNS, 1_500L);
    ti.setVal(ThreadInfoProps.WALL, 40L);
    ti.setVal(ThreadInfoProps.CPU_PREC, 3.75);
    ti.setVal(ThreadInfoProps.COUNT, 4);
    ti.setVal(ThreadInfoProps.DAEMON, true);
    return ti;
  }

  @Test
  void accessorsReadTypedValues() {
    ThreadInfo ti = sample();
    assertEquals("worker-1", ti.name());
    assertEquals("RUNNABLE", ti.state());
    assertEquals("runnable", ti.status());
    assertEquals("0x00007f", ti.tid());
    assertEquals(1_500L, ti.cpuNs());
    assertEquals(40L, ti.wallMs());
    assertEquals(3.75, ti.cpuPct());
    assertEquals(4, ti.count());
    assertTrue(ti.daemon());
  }

  @Test
  void missingPropsAreNullAndCountDefaultsToOne() {
    ThreadInfo empty = new ThreadInfo();
    assertNull(empty.name());
    assertNull(empty.cpuNs());
    assertNull(empty.cpuPct());
    assertFalse(empty.daemon());
    assertEquals(1, empty.count(), "a thread with no count prop represents one thread");
  }
}
```

`count()` returning 1 when absent is deliberate and matches `ThreadInfoAggregator.mergeCounts`, which treats a missing count as 1.

- [ ] **Step 2: Run the tests to verify they fail**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest='AttrTest+ThreadInfoAccessorsTest'
```

Expected: compilation failure — `cannot find symbol: variable NAME` / `method name()`.

- [ ] **Step 3: Add the constants to `Attr`**

Replace the body of `Attr.java`, keeping the existing constructor, field and `toString`:

```java
package com.performizeit.mjprof.core.api;

import com.performizeit.mjprof.core.parser.ThreadInfoProps;

public class Attr {
  public static final Attr NAME = new Attr(ThreadInfoProps.NAME);
  public static final Attr TID = new Attr(ThreadInfoProps.TID);
  public static final Attr NID = new Attr(ThreadInfoProps.NID);
  public static final Attr DAEMON = new Attr(ThreadInfoProps.DAEMON);
  public static final Attr PRIO = new Attr(ThreadInfoProps.PRIO);
  public static final Attr STATUS = new Attr(ThreadInfoProps.STATUS);
  public static final Attr STATE = new Attr(ThreadInfoProps.STATE);
  public static final Attr CPU_NS = new Attr(ThreadInfoProps.CPUNS);
  public static final Attr WALL_MS = new Attr(ThreadInfoProps.WALL);
  public static final Attr CPU_PCT = new Attr(ThreadInfoProps.CPU_PREC);
  public static final Attr STACK = new Attr(ThreadInfoProps.STACK);
  public static final Attr LOS = new Attr(ThreadInfoProps.LOS);
  public static final Attr COUNT = new Attr(ThreadInfoProps.COUNT);

  public String attrName;

  public Attr(String attrName) {
    this.attrName = attrName;
  }

  public String getAttrName() {
    return attrName;
  }

  @Override
  public String toString() {
    return attrName;
  }
}
```

- [ ] **Step 4: Add the accessors to `ThreadInfo`**

Insert before the closing brace of `ThreadInfo.java`:

```java
  public String name() {
    return (String) getVal(ThreadInfoProps.NAME);
  }

  public String state() {
    return (String) getVal(ThreadInfoProps.STATE);
  }

  public String status() {
    return (String) getVal(ThreadInfoProps.STATUS);
  }

  public String tid() {
    return (String) getVal(ThreadInfoProps.TID);
  }

  public Long cpuNs() {
    return (Long) getVal(ThreadInfoProps.CPUNS);
  }

  public Long wallMs() {
    return (Long) getVal(ThreadInfoProps.WALL);
  }

  public Double cpuPct() {
    return (Double) getVal(ThreadInfoProps.CPU_PREC);
  }

  /** Number of threads this entry represents; 1 when the dump was never grouped or merged. */
  public int count() {
    Integer c = (Integer) getVal(ThreadInfoProps.COUNT);
    return c == null ? 1 : c;
  }

  public Object stack() {
    return getVal(ThreadInfoProps.STACK);
  }

  public boolean daemon() {
    return Boolean.TRUE.equals(getVal(ThreadInfoProps.DAEMON));
  }
```

`stack()` returns `Object` rather than `Profile` on purpose: `core.parser` must not depend on `core.model`, and `Profile` lives in `core.model`. Confirm the direction of that dependency before changing it — if `parser` already imports `model` (it does, `ThreadDump` imports `JStackHeader`), tighten the return type to `Profile` and update the test to assert the type.

- [ ] **Step 5: Run the tests to verify they pass**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest='AttrTest+ThreadInfoAccessorsTest'
```

Expected: both classes pass.

- [ ] **Step 6: Run the whole suite — `Attr` is used by CLI param conversion**

```bash
mvn test
```

Expected: all pass. `MJProf.buildArgsArray` constructs `new Attr(val)`, untouched by this change.

- [ ] **Step 7: Commit**

```bash
git add mjprof-core/src/main/java/com/performizeit/mjprof/core/api/Attr.java \
        mjprof-core/src/main/java/com/performizeit/mjprof/core/parser/ThreadInfo.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/api/AttrTest.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/parser/ThreadInfoAccessorsTest.java
git commit -m "feat: typed Attr constants and ThreadInfo accessors"
```

---

### Task 3: `MjprofException` and the synchronous runner

**Files:**
- Create: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/MjprofException.java`
- Create: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/PipelineRunner.java`
- Test: `mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineRunnerTest.java` (create)

**Interfaces:**
- Consumes: `Attr` (Task 2, not required here but same module), `GeneratorHandler<ThreadDump>`, `PipeHandler<ThreadDump,ThreadDump>` from `core.plumbing`.
- Produces:
  - `public class MjprofException extends RuntimeException` with `(String)` and `(String, Throwable)` constructors.
  - `class PipelineRunner` (package-private) with `static List<ThreadDump> run(GeneratorHandler<ThreadDump> source, List<PipeHandler<ThreadDump,ThreadDump>> stages)`. Task 4 calls exactly this.

- [ ] **Step 1: Write the failing test**

`mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineRunnerTest.java`:

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;
import com.performizeit.mjprof.core.plumbing.PipeHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineRunnerTest {

  /** Minimal source over a fixed list of dumps. */
  private static GeneratorHandler<ThreadDump> source(ThreadDump... dumps) {
    Iterator<ThreadDump> it = List.of(dumps).iterator();
    return new GeneratorHandler<>() {
      public ThreadDump generate() {
        return it.hasNext() ? it.next() : null;
      }

      public boolean isDone() {
        return !it.hasNext();
      }

      public void sleepBetweenIteration() {
      }
    };
  }

  private static ThreadDump dumpNamed(String threadName) {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, threadName);
    ThreadDump d = new ThreadDump();
    d.setHeader(new JStackHeader("2026-07-31 10:00:00"));
    ArrayList<ThreadInfo> tis = new ArrayList<>();
    tis.add(ti);
    d.setThreadInfos(tis);
    return d;
  }

  /** Passes every dump through untouched. */
  private static PipeHandler<ThreadDump, ThreadDump> passThrough() {
    return new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        return msg;
      }

      public ThreadDump handleDone() {
        return null;
      }
    };
  }

  /** Swallows every dump, emits a single summary dump on done — the AccumulateDumps shape. */
  private static PipeHandler<ThreadDump, ThreadDump> collectOnDone() {
    List<ThreadDump> seen = new ArrayList<>();
    return new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        seen.add(msg);
        return null;
      }

      public ThreadDump handleDone() {
        return dumpNamed("merged-" + seen.size());
      }
    };
  }

  @Test
  void emitsOneResultPerSourceDump() {
    List<ThreadDump> out = PipelineRunner.run(source(dumpNamed("a"), dumpNamed("b")), List.of());
    assertEquals(2, out.size());
    assertEquals("a", out.get(0).getThreadInfos().get(0).name());
    assertEquals("b", out.get(1).getThreadInfos().get(0).name());
  }

  @Test
  void stageReturningNullDropsTheDump() {
    PipeHandler<ThreadDump, ThreadDump> dropAll = new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        return null;
      }

      public ThreadDump handleDone() {
        return null;
      }
    };
    List<ThreadDump> out = PipelineRunner.run(source(dumpNamed("a")), List.of(dropAll));
    assertTrue(out.isEmpty());
  }

  @Test
  void handleDoneOutputFlowsThroughRemainingStages() {
    // stage 0 accumulates and emits on done; stage 1 must see that emitted dump
    List<String> namesSeenByStage1 = new ArrayList<>();
    PipeHandler<ThreadDump, ThreadDump> recorder = new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        namesSeenByStage1.add(msg.getThreadInfos().get(0).name());
        return msg;
      }

      public ThreadDump handleDone() {
        return null;
      }
    };

    List<ThreadDump> out = PipelineRunner.run(
        source(dumpNamed("a"), dumpNamed("b"), dumpNamed("c")),
        List.of(collectOnDone(), recorder));

    assertEquals(List.of("merged-3"), namesSeenByStage1,
        "the done-output of stage 0 must be pushed through stage 1");
    assertEquals(1, out.size());
    assertEquals("merged-3", out.get(0).getThreadInfos().get(0).name());
  }

  @Test
  void doneOutputDoesNotReenterEarlierStages() {
    // stage 1 emits on done; stage 0 must not see it (no loops)
    List<String> namesSeenByStage0 = new ArrayList<>();
    PipeHandler<ThreadDump, ThreadDump> recorder = new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        namesSeenByStage0.add(msg.getThreadInfos().get(0).name());
        return msg;
      }

      public ThreadDump handleDone() {
        return null;
      }
    };

    PipelineRunner.run(source(dumpNamed("a")), List.of(recorder, collectOnDone()));

    assertEquals(List.of("a"), namesSeenByStage0);
  }

  @Test
  void sourceFailureIsWrappedWithTheFailingClassName() {
    GeneratorHandler<ThreadDump> boom = new GeneratorHandler<>() {
      public ThreadDump generate() {
        throw new IllegalStateException("no such file");
      }

      public boolean isDone() {
        return false;
      }

      public void sleepBetweenIteration() {
      }
    };

    MjprofException e = assertThrows(MjprofException.class,
        () -> PipelineRunner.run(boom, List.of()));
    assertTrue(e.getMessage().contains("no such file"), e.getMessage());
    assertEquals(IllegalStateException.class, e.getCause().getClass());
  }

  @Test
  void stageFailureIsWrappedWithTheFailingStageClassName() {
    PipeHandler<ThreadDump, ThreadDump> boom = new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        throw new IllegalArgumentException("bad prop");
      }

      public ThreadDump handleDone() {
        return null;
      }
    };

    MjprofException e = assertThrows(MjprofException.class,
        () -> PipelineRunner.run(source(dumpNamed("a")), List.of(boom)));
    assertTrue(e.getMessage().contains("bad prop"), e.getMessage());
  }

  @Test
  void sleepBetweenIterationIsCalledOncePerNonFinalDump() {
    int[] sleeps = {0};
    Iterator<ThreadDump> it = List.of(dumpNamed("a"), dumpNamed("b")).iterator();
    GeneratorHandler<ThreadDump> counting = new GeneratorHandler<>() {
      public ThreadDump generate() {
        return it.hasNext() ? it.next() : null;
      }

      public boolean isDone() {
        return !it.hasNext();
      }

      public void sleepBetweenIteration() {
        sleeps[0]++;
      }
    };

    PipelineRunner.run(counting, List.of(passThrough()));
    assertEquals(1, sleeps[0], "sleep only between dumps, never after the last");
  }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=PipelineRunnerTest
```

Expected: compilation failure — `cannot find symbol: class MjprofException` / `class PipelineRunner`.

- [ ] **Step 3: Write `MjprofException`**

Plain-text Javadoc, no `{@link Pipeline}` — that class does not exist until Task 4.

```java
package com.performizeit.mjprof.core.pipeline;

/** Thrown for any failure surfaced through the Pipeline API. */
public class MjprofException extends RuntimeException {

  public MjprofException(String message) {
    super(message);
  }

  public MjprofException(String message, Throwable cause) {
    super(message, cause);
  }
}
```

- [ ] **Step 4: Write `PipelineRunner`**

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;
import com.performizeit.mjprof.core.plumbing.PipeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a source and a chain of stages synchronously on the calling thread.
 *
 * <p>Mirrors the semantics of the threaded Generator/Pipe plumbing used by the command line: each
 * dump produced by the source is pushed through every stage in order, a stage returning null drops
 * the message, and once the source is exhausted each stage's handleDone output is pushed through the
 * stages after it.
 */
final class PipelineRunner {

  private PipelineRunner() {
  }

  static List<ThreadDump> run(GeneratorHandler<ThreadDump> source,
                              List<PipeHandler<ThreadDump, ThreadDump>> stages) {
    List<ThreadDump> results = new ArrayList<>();

    while (!isDone(source)) {
      ThreadDump generated = generate(source);
      if (generated != null) {
        push(generated, 0, stages, results);
      }
      if (!isDone(source)) {
        sleep(source);
      }
    }

    for (int i = 0; i < stages.size(); i++) {
      ThreadDump done = handleDone(stages.get(i));
      if (done != null) {
        push(done, i + 1, stages, results);
      }
    }
    return results;
  }

  private static void push(ThreadDump dump, int fromStage,
                           List<PipeHandler<ThreadDump, ThreadDump>> stages,
                           List<ThreadDump> results) {
    ThreadDump current = dump;
    for (int i = fromStage; i < stages.size(); i++) {
      PipeHandler<ThreadDump, ThreadDump> stage = stages.get(i);
      try {
        current = stage.handleMsg(current);
      } catch (RuntimeException e) {
        throw new MjprofException(
            "stage " + stage.getClass().getName() + " failed: " + e.getMessage(), e);
      }
      if (current == null) {
        return;
      }
    }
    results.add(current);
  }

  private static ThreadDump generate(GeneratorHandler<ThreadDump> source) {
    try {
      return source.generate();
    } catch (RuntimeException e) {
      throw new MjprofException(
          "data source " + source.getClass().getName() + " failed: " + e.getMessage(), e);
    }
  }

  private static boolean isDone(GeneratorHandler<ThreadDump> source) {
    try {
      return source.isDone();
    } catch (RuntimeException e) {
      throw new MjprofException(
          "data source " + source.getClass().getName() + " failed: " + e.getMessage(), e);
    }
  }

  private static void sleep(GeneratorHandler<ThreadDump> source) {
    try {
      source.sleepBetweenIteration();
    } catch (RuntimeException e) {
      throw new MjprofException(
          "data source " + source.getClass().getName() + " failed: " + e.getMessage(), e);
    }
  }

  private static ThreadDump handleDone(PipeHandler<ThreadDump, ThreadDump> stage) {
    try {
      return stage.handleDone();
    } catch (RuntimeException e) {
      throw new MjprofException(
          "stage " + stage.getClass().getName() + " failed: " + e.getMessage(), e);
    }
  }
}
```

Note the loop calls `isDone()` **before** the first `generate()`, matching `Generator.run`. A source that reports `isDone()==true` up front (an empty file) produces zero dumps rather than an exception — the empty-source case is `Pipeline`'s to interpret, not the runner's.

- [ ] **Step 5: Run the test to verify it passes**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=PipelineRunnerTest
```

Expected: all 7 tests pass.

- [ ] **Step 6: Commit**

```bash
git add mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/ \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/
git commit -m "feat: synchronous pipeline runner with MjprofException"
```

---

### Task 4: `Pipeline` — sources, generic `map`, terminals

**Files:**
- Create: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java`
- Test: `mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineTest.java` (create)

**Interfaces:**
- Consumes: `PipelineRunner.run(...)` and `MjprofException` (Task 3); `Attr` constants and `ThreadInfo.name()`/`state()` (Task 2).
- Produces, all used by Tasks 5–7:
  - `static <S extends DataSource & GeneratorHandler<ThreadDump>> Pipeline from(S source)`
  - `static Pipeline fromFile(String path)`, `static Pipeline fromStdin()`
  - `static Pipeline fromJmx(String hostPort, int count, int sleepMs, String user, String pass)`
  - `static Pipeline fromJmxWithCpu(String hostPort, int count, int sleepMs, String user, String pass)`
  - four `map(...)` overloads (`SingleThreadMapper`, `DumpReducer`, `Filter`, `ThreadInfoComparator`, each `& PipeHandler<ThreadDump,ThreadDump>`)
  - `List<ThreadDump> collect()`, `ThreadDump collectOne()`, `List<ThreadInfo> threads()`
  - `Pipeline add(PipeHandler<ThreadDump,ThreadDump> stage)` — package-private; Tasks 5–7 add convenience methods and factories that call it

- [ ] **Step 1: Write the failing test**

`mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineTest.java`:

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.plugins.filters.JStackFilterFieldContains;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.StackTop;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineTest {

  private static final String FIXTURE = "src/test/res/test.txt";

  @Test
  void fromFileReadsOneDump() {
    List<ThreadDump> dumps = Pipeline.fromFile(FIXTURE).collect();
    assertEquals(1, dumps.size());
    assertFalse(dumps.get(0).getThreadInfos().isEmpty());
  }

  @Test
  void collectOneReturnsTheSingleDump() {
    ThreadDump d = Pipeline.fromFile(FIXTURE).collectOne();
    assertFalse(d.getThreadInfos().isEmpty());
  }

  @Test
  void threadsFlattensDumps() {
    List<ThreadInfo> threads = Pipeline.fromFile(FIXTURE).threads();
    assertEquals(Pipeline.fromFile(FIXTURE).collectOne().getThreadInfos().size(), threads.size());
  }

  @Test
  void mapAcceptsAFilterInstance() {
    List<ThreadInfo> mains = Pipeline.fromFile(FIXTURE)
        .map(new JStackFilterFieldContains(Attr.NAME.getAttrName(), "main"))
        .threads();
    assertFalse(mains.isEmpty());
    assertTrue(mains.stream().allMatch(t -> t.name().contains("main")));
  }

  @Test
  void mapAcceptsASingleThreadMapperInstance() {
    List<ThreadInfo> trimmed = Pipeline.fromFile(FIXTURE)
        .map(new StackTop(1))
        .threads();
    assertFalse(trimmed.isEmpty());
  }

  @Test
  void stagesRunInOrderAndPipelineIsReusable() {
    Pipeline base = Pipeline.fromFile(FIXTURE);
    int all = base.threads().size();
    int filtered = Pipeline.fromFile(FIXTURE)
        .map(new JStackFilterFieldContains(Attr.NAME.getAttrName(), "main"))
        .threads().size();
    assertTrue(filtered < all, "filter must remove threads");
    assertEquals(all, base.threads().size(), "collecting twice must give the same result");
  }

  @Test
  void missingFileThrowsMjprofException() {
    MjprofException e = assertThrows(MjprofException.class,
        () -> Pipeline.fromFile("no/such/file.txt").collect());
    assertTrue(e.getMessage().contains("no/such/file.txt")
        || e.getCause().getMessage().contains("no/such/file.txt"), e.getMessage());
  }

  @Test
  void unreachableJmxThrowsMjprofException() {
    // port 1 is never a live JMX endpoint
    assertThrows(MjprofException.class,
        () -> Pipeline.fromJmx("localhost:1", 1, 0, null, null).collect());
  }
}
```

`stagesRunInOrderAndPipelineIsReusable` asserts a real requirement for a server: calling `threads()` twice on the same `Pipeline` must not consume it. A file source is single-use, so `Pipeline` must construct its source lazily — hold a supplier, not an instance.

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=PipelineTest
```

Expected: compilation failure — `cannot find symbol: class Pipeline`.

- [ ] **Step 3: Write `Pipeline`**

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.ThreadInfoComparator;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.plugin.types.DataSource;
import com.performizeit.mjprof.core.plugin.types.DumpReducer;
import com.performizeit.mjprof.core.plugin.types.Filter;
import com.performizeit.mjprof.core.plugin.types.SingleThreadMapper;
import com.performizeit.mjprof.core.plugins.dataSource.JmxDataSourcePlugin;
import com.performizeit.mjprof.core.plugins.dataSource.JmxDataSourcePluginWithCpu;
import com.performizeit.mjprof.core.plugins.dataSource.PathDataSourcePlugin;
import com.performizeit.mjprof.core.plugins.dataSource.StdinDataSourcePlugin;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;
import com.performizeit.mjprof.core.plumbing.PipeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Typed, thread-free thread-dump analysis pipeline.
 *
 * <p>Stages are plugin instances: the plugin constructor arguments are the typed stage arguments, so
 * the compiler checks them. Every method returns a new Pipeline, so a Pipeline can be shared and
 * collected from more than once — the data source is constructed fresh per run.
 *
 * <p>Collection happens on the calling thread. A source configured to take several samples blocks
 * for its own sleep interval: fromJmx(hostPort, 3, 1000, ...) blocks roughly two seconds.
 */
public final class Pipeline {

  private final Supplier<GeneratorHandler<ThreadDump>> source;
  private final List<PipeHandler<ThreadDump, ThreadDump>> stages;

  private Pipeline(Supplier<GeneratorHandler<ThreadDump>> source,
                   List<PipeHandler<ThreadDump, ThreadDump>> stages) {
    this.source = source;
    this.stages = stages;
  }

  // ---------------------------------------------------------------- sources

  /** Uses the given data source plugin instance. The instance is reused, so this Pipeline is single-use. */
  public static <S extends DataSource & GeneratorHandler<ThreadDump>> Pipeline from(S source) {
    if (source == null) {
      throw new MjprofException("data source must not be null");
    }
    return new Pipeline(() -> source, List.of());
  }

  /** Reads one or more thread dumps from a jstack-format text file. */
  public static Pipeline fromFile(String path) {
    if (path == null) {
      throw new MjprofException("path must not be null");
    }
    return new Pipeline(() -> new PathDataSourcePlugin(path), List.of());
  }

  /** Reads thread dumps from standard input. */
  public static Pipeline fromStdin() {
    return new Pipeline(StdinDataSourcePlugin::new, List.of());
  }

  /**
   * Samples thread dumps over JMX.
   *
   * @param hostPort host:port of the target JMX endpoint
   * @param count    number of dumps to take
   * @param sleepMs  milliseconds to wait between dumps; blocks the calling thread
   * @param user     JMX username, or null
   * @param pass     JMX password, or null
   */
  public static Pipeline fromJmx(String hostPort, int count, int sleepMs, String user, String pass) {
    return new Pipeline(() -> new JmxDataSourcePlugin(hostPort, count, sleepMs, user, pass), List.of());
  }

  /** As fromJmx, additionally collecting per-thread CPU time. */
  public static Pipeline fromJmxWithCpu(String hostPort, int count, int sleepMs, String user, String pass) {
    return new Pipeline(() -> new JmxDataSourcePluginWithCpu(hostPort, count, sleepMs, user, pass), List.of());
  }

  // ------------------------------------------------------------ generic stage

  /** Adds any single-thread mapper plugin instance as a stage. */
  public <P extends SingleThreadMapper & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return add(stage);
  }

  /** Adds any dump reducer plugin instance as a stage. */
  public <P extends DumpReducer & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return add(stage);
  }

  /** Adds any filter plugin instance as a stage. */
  public <P extends Filter & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return add(stage);
  }

  /** Adds any comparator plugin instance as a stage. */
  public <P extends ThreadInfoComparator & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return add(stage);
  }

  Pipeline add(PipeHandler<ThreadDump, ThreadDump> stage) {
    if (stage == null) {
      throw new MjprofException("stage must not be null");
    }
    List<PipeHandler<ThreadDump, ThreadDump>> next = new ArrayList<>(stages);
    next.add(stage);
    return new Pipeline(source, next);
  }

  // ------------------------------------------------------------- terminals

  /** Runs the pipeline and returns one dump per dump the source produced. */
  public List<ThreadDump> collect() {
    return PipelineRunner.run(source.get(), freshStages());
  }

  /** Runs the pipeline and returns its single dump, failing if the source produced any other number. */
  public ThreadDump collectOne() {
    List<ThreadDump> all = collect();
    if (all.size() != 1) {
      throw new MjprofException("expected exactly one thread dump, got " + all.size());
    }
    return all.get(0);
  }

  /** Runs the pipeline and returns every thread of every resulting dump. */
  public List<ThreadInfo> threads() {
    List<ThreadInfo> out = new ArrayList<>();
    for (ThreadDump d : collect()) {
      out.addAll(d.getThreadInfos());
    }
    return out;
  }

  private List<PipeHandler<ThreadDump, ThreadDump>> freshStages() {
    return new ArrayList<>(stages);
  }
}
```

The four `map` overloads have the same erasure on their type parameter but **different** first bounds, which is what makes them distinct methods — this compiles. Verify it in Step 4 rather than trusting the claim; if javac rejects them as clashing, the fallback is distinct names (`mapThread`, `reduce`, `filter`, `sortBy`) and the plan's later tasks must be updated to match.

Known limitation, deliberate and documented in the class Javadoc: `from(S source)` reuses the caller's instance, so a Pipeline built that way is single-use, unlike the `fromX` factories. Stateful stages (`AccumulateDumps`) are likewise reused across `collect()` calls — `freshStages()` copies the list, not the stage objects. Task 7 covers the consequence for `merge`.

- [ ] **Step 4: Run the test to verify it passes**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=PipelineTest
```

Expected: all 8 tests pass. `unreachableJmxThrowsMjprofException` may take a few seconds to time out; if it hangs beyond ~30s, the JMX connect has no timeout — add `@Timeout(30)` from `org.junit.jupiter.api.Timeout` to that test and note the missing connect timeout as a follow-up rather than fixing JMX plumbing here.

If `stagesRunInOrderAndPipelineIsReusable` fails on the second `base.threads()`, the source supplier is being memoized — check that `fromFile` passes a lambda and not a pre-built instance.

- [ ] **Step 5: Commit**

```bash
git add mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineTest.java
git commit -m "feat: typed Pipeline builder with sources, map and terminals"
```

---

### Task 5: Multi-attribute grouping, `merge`, and the `Attr.STACK` guard

**Files:**
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/plugins/mappers/GroupByProp.java`
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java`
- Test: `mjprof-core/src/test/java/com/performizeit/mjprof/core/plugins/mappers/GroupByPropMultiAttrTest.java` (create), `mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineGroupingTest.java` (create)

**Interfaces:**
- Consumes: `Pipeline.add(...)` and `MjprofException` (Tasks 3–4), `Attr` constants (Task 2).
- Produces: `GroupByProp(List<String> props)`; `Pipeline.group(Attr... attrs)`; `Pipeline.merge()`; `Pipeline.merge(Attr attr)`.

- [ ] **Step 1: Write the failing tests**

`mjprof-core/src/test/java/com/performizeit/mjprof/core/plugins/mappers/GroupByPropMultiAttrTest.java`:

```java
package com.performizeit.mjprof.core.plugins.mappers;

import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupByPropMultiAttrTest {

  private static ThreadInfo thread(String name, String state) {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, name);
    ti.setVal(ThreadInfoProps.STATE, state);
    return ti;
  }

  private static ThreadDump dumpOf(ThreadInfo... threads) {
    ThreadDump d = new ThreadDump();
    d.setHeader(new JStackHeader("2026-07-31 10:00:00"));
    d.setThreadInfos(new ArrayList<>(List.of(threads)));
    return d;
  }

  @Test
  void singleAttrGroupsByThatAttrOnly() {
    ThreadDump grouped = new GroupByProp(List.of(ThreadInfoProps.STATE)).map(dumpOf(
        thread("a", "RUNNABLE"),
        thread("b", "RUNNABLE"),
        thread("c", "BLOCKED")));

    assertEquals(2, grouped.getThreadInfos().size());
    assertEquals(3, grouped.getThreadInfos().stream().mapToInt(ThreadInfo::count).sum());
  }

  @Test
  void twoAttrsGroupOnTheCompositeKey() {
    ThreadDump grouped = new GroupByProp(List.of(ThreadInfoProps.STATE, ThreadInfoProps.NAME)).map(dumpOf(
        thread("a", "RUNNABLE"),
        thread("a", "RUNNABLE"),
        thread("b", "RUNNABLE"),
        thread("b", "BLOCKED")));

    assertEquals(3, grouped.getThreadInfos().size(),
        "(RUNNABLE,a) (RUNNABLE,b) (BLOCKED,b)");
    assertEquals(4, grouped.getThreadInfos().stream().mapToInt(ThreadInfo::count).sum());
  }

  @Test
  void stringConstructorStillBehavesAsBefore() {
    ThreadDump grouped = new GroupByProp(ThreadInfoProps.STATE).map(dumpOf(
        thread("a", "RUNNABLE"),
        thread("b", "RUNNABLE")));
    assertEquals(1, grouped.getThreadInfos().size());
  }
}
```

`mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineGroupingTest.java`:

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineGroupingTest {

  private static final String FIXTURE = "src/test/res/test.txt";

  @Test
  void groupByStateCollapsesThreadsAndPreservesTheTotalCount() {
    List<ThreadInfo> ungrouped = Pipeline.fromFile(FIXTURE).threads();
    List<ThreadInfo> grouped = Pipeline.fromFile(FIXTURE).group(Attr.STATE).threads();

    assertTrue(grouped.size() < ungrouped.size(), "grouping must collapse threads");
    assertEquals(ungrouped.size(), grouped.stream().mapToInt(ThreadInfo::count).sum());
  }

  @Test
  void groupWithNoAttrsMergesTheWholeDump() {
    assertEquals(1, Pipeline.fromFile(FIXTURE).group().threads().size());
  }

  @Test
  void groupByStackIsRejected() {
    MjprofException e = assertThrows(MjprofException.class,
        () -> Pipeline.fromFile(FIXTURE).group(Attr.STACK));
    assertTrue(e.getMessage().contains("tree()"), e.getMessage());
  }

  @Test
  void mergeByStackIsRejected() {
    assertThrows(MjprofException.class,
        () -> Pipeline.fromFile(FIXTURE).merge(Attr.STACK));
  }

  @Test
  void mergeOfASingleDumpKeepsEveryThread() {
    // merge keys on tid by default; one dump means nothing to combine
    List<ThreadInfo> merged = Pipeline.fromFile(FIXTURE).merge().threads();
    assertEquals(Pipeline.fromFile(FIXTURE).threads().size(), merged.size());
  }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest='GroupByPropMultiAttrTest+PipelineGroupingTest'
```

Expected: compilation failure — no `GroupByProp(List)` constructor, no `group`/`merge` on `Pipeline`.

- [ ] **Step 3: Add the multi-attribute constructor to `GroupByProp`**

Replace the field and constructor, keeping the `@Plugin` annotation and `map`/`handleMsg`/`handleDone` exactly as they are:

```java
public class GroupByProp implements DumpReducer, PipeHandler<ThreadDump, ThreadDump> {
  private final List<String> props;

  /** Single-attribute grouping; the form the {@code group} monad uses. */
  public GroupByProp(String prop) {
    this.props = List.of(prop);
  }

  /** Groups on a composite key of several attributes. API only — the monad passes one attribute. */
  public GroupByProp(List<String> props) {
    this.props = List.copyOf(props);
  }

  public ThreadDump map(final ThreadDump jsd) {
    ThreadInfoAggregator aggr = new ThreadInfoAggregator(props);
    for (ThreadInfo mss : jsd.getThreadInfos()) {
      aggr.accumulateThreadInfo(mss);
    }
    ThreadDump jsd2 = new ThreadDump();
    jsd2.setHeader(jsd.getHeader());
    jsd2.setThreadInfos(aggr.getAggrInfos());
    jsd2.setJNIglobalReferences(jsd.getJNIglobalReferences());
    return jsd2;
  }
```

Imports change from `java.util.ArrayList` to `java.util.List`. `ThreadInfoAggregator` takes `List<String>` already.

One behavioral subtlety to preserve: the `group` monad's attribute is optional with no default, so `MJProf.buildArgsArray` passes `""` when omitted, and `ThreadInfoAggregator` keys every thread on `getVal("")` = `null` — one group, i.e. "merge the whole dump". `List.of(prop)` keeps that, but `List.of` rejects null, so if a caller ever passes a null prop this now throws where it used to group-everything. `MJProf` passes `""`, never null, so the CLI is unaffected. Task 1's golden check on `04-group` covers this.

- [ ] **Step 4: Add `group` and `merge` to `Pipeline`**

Insert after the `map` overloads, before `add`:

```java
  /**
   * Groups threads within each dump on a composite key of the given attributes, collapsing matching
   * threads into one entry carrying count, summed cpu_ns and a merged stack tree. With no arguments,
   * merges every thread of the dump into one entry.
   *
   * @throws MjprofException if given Attr.STACK — stack profiles have no value equality, so grouping
   *                         on them collapses nothing. Use merge(Attr.TID) followed by tree().
   */
  public Pipeline group(Attr... attrs) {
    for (Attr a : attrs) {
      rejectStack(a, "group");
    }
    if (attrs.length == 0) {
      return add(new GroupByProp(""));
    }
    List<String> props = new ArrayList<>();
    for (Attr a : attrs) {
      props.add(a.getAttrName());
    }
    return add(new GroupByProp(props));
  }

  /** Combines every dump into one, matching threads on their thread id. */
  public Pipeline merge() {
    return merge(Attr.TID);
  }

  /**
   * Combines every dump into one, matching threads on the given attribute. Summed cpu_ns, max
   * wall_ms, recomputed %cpu and merged stack trees make N samples into one sampled profile.
   *
   * @throws MjprofException if given Attr.STACK — see group(Attr...).
   */
  public Pipeline merge(Attr attr) {
    rejectStack(attr, "merge");
    return add(new AccumulateDumps(attr.getAttrName()));
  }

  private static void rejectStack(Attr attr, String method) {
    if (Attr.STACK.getAttrName().equals(attr.getAttrName())) {
      throw new MjprofException(method + "(Attr.STACK) is not supported: stack profiles have no "
          + "value equality, so nothing would be collapsed. Use merge(Attr.TID) then tree().");
    }
  }
```

Add imports: `com.performizeit.mjprof.core.api.Attr`, `com.performizeit.mjprof.core.plugins.combiners.AccumulateDumps`, `com.performizeit.mjprof.core.plugins.mappers.GroupByProp`.

- [ ] **Step 5: Run the tests to verify they pass**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest='GroupByPropMultiAttrTest+PipelineGroupingTest'
```

Expected: all 8 tests pass.

- [ ] **Step 6: Run the whole build — `GroupByProp` is a registered monad**

```bash
mvn clean package
```

Expected: `BUILD SUCCESS`. Then re-check the `group` golden file, because `GroupByProp` changed:

```bash
JAR=mjprof-cli/target/mjprof-cli-1.1.0-jar-with-dependencies.jar
java -cp "$JAR" -Dmacros.configFile=/dev/null com.performizeit.mjprof.cli.MJProf \
  'path/mjprof-core/src/test/res/test.txt/.group/state/' > /tmp/mjprof-golden/04-group.recheck.txt 2>&1
diff /tmp/mjprof-golden/04-group.recheck.txt /tmp/mjprof-golden/04-group.txt && echo "group monad unchanged"
```

Expected: `group monad unchanged`.

- [ ] **Step 7: Commit**

```bash
git add mjprof-core/src/main/java/com/performizeit/mjprof/core/plugins/mappers/GroupByProp.java \
        mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/plugins/mappers/GroupByPropMultiAttrTest.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineGroupingTest.java
git commit -m "feat: multi-attribute group, merge, and Attr.STACK guard"
```

---

### Task 6: Typed convenience methods

**Files:**
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java`
- Test: `mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineConvenienceTest.java` (create)

**Interfaces:**
- Consumes: `Pipeline.add(...)` (Task 4), `Attr` (Task 2).
- Produces: `contains`, `notContains`, `matches`, `stackTop`, `stackBottom`, `trimBelow`, `frameContains`, `frameNotContains`, `sort`, `sortDesc`, `tree`, `mergedCallees`, `withoutProp`, `withoutNameSuffix`, `withoutPackages`, `withoutFileNames`, `withoutAt` — all returning `Pipeline`.

Before writing the implementation, confirm each plugin's constructor signature — they take `String`, not `Attr`:

```bash
cd /Users/lifey/github/mjprof
grep -n "public JStackFilterFieldContains\|public JStackFilterFieldNotContains\|public JStackFilterFieldRegexMatches" \
  mjprof-core/src/main/java/com/performizeit/mjprof/core/plugins/filters/*.java
grep -rn "public StackTop\|public StackBottom\|public TrimBelow\|public StackFrameContains\|public StackFrameNotContains\|public PropEliminator\|public RemoveThreadNameNumericalSuffix" \
  mjprof-core/src/main/java/com/performizeit/mjprof/core/plugins/mappers/
grep -rn "public PropComparator\|public ReversePropComparator" \
  mjprof-core/src/main/java/com/performizeit/mjprof/core/plugins/comparators/
grep -rn "public StackTreeMerger\|public MergedCallees" \
  mjprof-core/src/main/java/com/performizeit/mjprof/core/plugins/mergers/ \
  mjprof-core/src/main/java/com/performizeit/mjprof/core/plugins/mappers/MergedCallees.java
grep -rn "public AtEliminator\|public PackageEliminator\|public FileNameEliminator" \
  mjprof-core/src/main/java/com/performizeit/mjprof/core/plugins/mappers/singlethread/stackframe/
```

Use exactly what these print. `JStackFilterFieldContains(String attrName, String valContained)` is confirmed; the rest must be read, and any zero-arg plugin gets a zero-arg convenience method.

- [ ] **Step 1: Write the failing test**

`mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineConvenienceTest.java`:

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineConvenienceTest {

  private static final String FIXTURE = "src/test/res/test.txt";

  private static Pipeline fixture() {
    return Pipeline.fromFile(FIXTURE);
  }

  @Test
  void containsKeepsOnlyMatchingThreads() {
    List<ThreadInfo> t = fixture().contains(Attr.NAME, "main").threads();
    assertFalse(t.isEmpty());
    assertTrue(t.stream().allMatch(x -> x.name().contains("main")));
  }

  @Test
  void notContainsIsTheComplementOfContains() {
    int all = fixture().threads().size();
    int with = fixture().contains(Attr.NAME, "main").threads().size();
    int without = fixture().notContains(Attr.NAME, "main").threads().size();
    assertEquals(all, with + without);
  }

  @Test
  void matchesAppliesARegex() {
    List<ThreadInfo> t = fixture().matches(Attr.STATE, "RUNNABLE|BLOCKED").threads();
    assertTrue(t.stream().allMatch(x -> x.state().equals("RUNNABLE") || x.state().equals("BLOCKED")));
  }

  @Test
  void sortOrdersByAttributeAndSortDescReversesIt() {
    List<ThreadInfo> asc = fixture().sort(Attr.NAME).threads();
    List<ThreadInfo> desc = fixture().sortDesc(Attr.NAME).threads();
    assertEquals(asc.size(), desc.size());
    assertEquals(asc.get(0).name(), desc.get(desc.size() - 1).name());
  }

  @Test
  void stackTopAndBottomTrimStacksWithoutDroppingThreads() {
    int all = fixture().threads().size();
    assertEquals(all, fixture().stackTop(1).threads().size());
    assertEquals(all, fixture().stackBottom(1).threads().size());
  }

  @Test
  void withoutPropRemovesTheAttribute() {
    List<ThreadInfo> t = fixture().withoutProp(Attr.STATE).threads();
    assertTrue(t.stream().allMatch(x -> x.state() == null));
  }

  @Test
  void treeProducesASingleMergedEntry() {
    assertEquals(1, fixture().tree().threads().size());
  }

  @Test
  void chainingSeveralConvenienceStagesWorks() {
    List<ThreadInfo> t = fixture()
        .contains(Attr.STACK, "at")
        .withoutNameSuffix()
        .group(Attr.NAME)
        .sortDesc(Attr.COUNT)
        .threads();
    assertFalse(t.isEmpty());
  }
}
```

`treeProducesASingleMergedEntry` encodes what `StackTreeMerger` does today — confirm against golden file `06-tree` from Task 1. If `tree` yields a different shape, fix the assertion to match observed behavior; do not change the plugin.

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=PipelineConvenienceTest
```

Expected: compilation failure — `cannot find symbol: method contains(Attr,String)`.

- [ ] **Step 3: Add the convenience methods to `Pipeline`**

Insert after `merge(Attr)`, using the exact constructor signatures confirmed above:

```java
  // ------------------------------------------------- filters

  /** Keeps threads whose attribute contains the given substring. */
  public Pipeline contains(Attr attr, String value) {
    return add(new JStackFilterFieldContains(attr.getAttrName(), value));
  }

  /** Keeps threads whose attribute does not contain the given substring. */
  public Pipeline notContains(Attr attr, String value) {
    return add(new JStackFilterFieldNotContains(attr.getAttrName(), value));
  }

  /** Keeps threads whose attribute matches the given regular expression. */
  public Pipeline matches(Attr attr, String regex) {
    return add(new JStackFilterFieldRegexMatches(attr.getAttrName(), regex));
  }

  // ------------------------------------------------- stack shaping

  /** Keeps only the top n stack frames of each thread. */
  public Pipeline stackTop(int frames) {
    return add(new StackTop(frames));
  }

  /** Keeps only the bottom n stack frames of each thread. */
  public Pipeline stackBottom(int frames) {
    return add(new StackBottom(frames));
  }

  /** Drops stack frames below the first frame containing the given text. */
  public Pipeline trimBelow(String frameText) {
    return add(new TrimBelow(frameText));
  }

  /** Keeps only stack frames containing the given text. */
  public Pipeline frameContains(String text) {
    return add(new StackFrameContains(text));
  }

  /** Drops stack frames containing the given text. */
  public Pipeline frameNotContains(String text) {
    return add(new StackFrameNotContains(text));
  }

  // ------------------------------------------------- ordering

  /** Sorts threads by an attribute, ascending. */
  public Pipeline sort(Attr attr) {
    return add(new PropComparator(attr.getAttrName()));
  }

  /** Sorts threads by an attribute, descending. */
  public Pipeline sortDesc(Attr attr) {
    return add(new ReversePropComparator(attr.getAttrName()));
  }

  // ------------------------------------------------- stack trees

  /** Merges all thread stacks of each dump into one call tree. */
  public Pipeline tree() {
    return add(new StackTreeMerger());
  }

  /** Merges the callees of the given method across all threads. */
  public Pipeline mergedCallees(String methodName) {
    return add(new MergedCallees(methodName));
  }

  // ------------------------------------------------- elimination

  /** Removes an attribute from every thread. */
  public Pipeline withoutProp(Attr attr) {
    return add(new PropEliminator(attr.getAttrName()));
  }

  /** Strips trailing numbers from thread names so pool threads group together. */
  public Pipeline withoutNameSuffix() {
    return add(new RemoveThreadNameNumericalSuffix());
  }

  /** Removes package names from stack frames. */
  public Pipeline withoutPackages() {
    return add(new PackageEliminator());
  }

  /** Removes file names and line numbers from stack frames. */
  public Pipeline withoutFileNames() {
    return add(new FileNameEliminator());
  }

  /** Removes the leading "at " from stack frames. */
  public Pipeline withoutAt() {
    return add(new AtEliminator());
  }
```

Add the matching imports from `core.plugins.filters`, `core.plugins.mappers`, `core.plugins.mappers.singlethread`, `core.plugins.mappers.singlethread.stackframe`, `core.plugins.comparators`, `core.plugins.mergers`.

- [ ] **Step 4: Run the test to verify it passes**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=PipelineConvenienceTest
```

Expected: all 8 tests pass. Any constructor mismatch is a signature you did not read in the pre-step — read it, do not guess.

- [ ] **Step 5: Commit**

```bash
git add mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineConvenienceTest.java
git commit -m "feat: typed convenience stages on Pipeline"
```

---

### Task 7: `DumpsDataSource` — collect your own samples, merge later

**Files:**
- Create: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/DumpsDataSource.java`
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java`
- Test: `mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineFromDumpsTest.java` (create)

**Interfaces:**
- Consumes: `Pipeline` and its `merge`/`group` (Tasks 4–5).
- Produces: `class DumpsDataSource implements DataSource, GeneratorHandler<ThreadDump>` with `DumpsDataSource(Collection<ThreadDump>)`; `static Pipeline Pipeline.fromDumps(Collection<ThreadDump> dumps)`.

- [ ] **Step 1: Write the failing test**

`mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineFromDumpsTest.java`:

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineFromDumpsTest {

  private static ThreadDump sample(String tid, long cpuNs) {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, "worker");
    ti.setVal(ThreadInfoProps.TID, tid);
    ti.setVal(ThreadInfoProps.CPUNS, cpuNs);
    ThreadDump d = new ThreadDump();
    d.setHeader(new JStackHeader("2026-07-31 10:00:00"));
    ArrayList<ThreadInfo> tis = new ArrayList<>();
    tis.add(ti);
    d.setThreadInfos(tis);
    return d;
  }

  @Test
  void fromDumpsEmitsOneResultPerDump() {
    List<ThreadDump> in = List.of(sample("0x1", 10), sample("0x1", 20), sample("0x1", 30));
    assertEquals(3, Pipeline.fromDumps(in).collect().size());
  }

  @Test
  void mergeSumsCpuAcrossSelfCollectedSamples() {
    List<ThreadDump> samples = List.of(sample("0x1", 10), sample("0x1", 20), sample("0x1", 30));

    List<ThreadInfo> merged = Pipeline.fromDumps(samples).merge(Attr.TID).threads();

    assertEquals(1, merged.size(), "same tid across samples folds into one entry");
    assertEquals(60L, merged.get(0).cpuNs(), "cpu_ns is summed across samples");
    assertEquals(3, merged.get(0).count());
  }

  @Test
  void mergeKeepsDistinctThreadsApart() {
    List<ThreadDump> samples = List.of(sample("0x1", 10), sample("0x2", 20));
    assertEquals(2, Pipeline.fromDumps(samples).merge(Attr.TID).threads().size());
  }

  @Test
  void emptyCollectionYieldsNoDumps() {
    assertTrue(Pipeline.fromDumps(List.of()).collect().isEmpty());
  }

  @Test
  void nullCollectionIsRejected() {
    assertThrows(MjprofException.class, () -> Pipeline.fromDumps(null));
  }

  @Test
  void collectingTwiceGivesTheSameResult() {
    Pipeline p = Pipeline.fromDumps(List.of(sample("0x1", 10), sample("0x1", 20)));
    assertEquals(2, p.collect().size());
    assertEquals(2, p.collect().size());
  }
}
```

`collectingTwiceGivesTheSameResult` matters: the source supplier must build a fresh iterator per run, and a `merge` stage must not carry state between runs. If it fails on the second call, that is the stateful-stage limitation noted in Task 4 — fix it by having `Pipeline` hold stage *suppliers* rather than instances, and update the `map` overloads and `add` to match. Do not paper over it with a "call collect only once" comment.

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=PipelineFromDumpsTest
```

Expected: compilation failure — no `fromDumps`.

- [ ] **Step 3: Write `DumpsDataSource`**

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.plugin.types.DataSource;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Feeds thread dumps a caller already holds into a pipeline.
 *
 * <p>Deliberately not annotated with @Plugin: an in-memory source is meaningless from the command
 * line, and it must not appear in supported_monads.txt.
 */
final class DumpsDataSource implements DataSource, GeneratorHandler<ThreadDump> {

  private final Iterator<ThreadDump> remaining;

  DumpsDataSource(Collection<ThreadDump> dumps) {
    this.remaining = List.copyOf(dumps).iterator();
  }

  @Override
  public ThreadDump generate() {
    return remaining.hasNext() ? remaining.next() : null;
  }

  @Override
  public boolean isDone() {
    return !remaining.hasNext();
  }

  @Override
  public void sleepBetweenIteration() {
  }
}
```

`List.copyOf` snapshots the collection, so a caller mutating their list mid-run cannot corrupt the iteration. It also rejects null elements, which would otherwise end the source early — `generate()` returning null is the exhaustion signal.

- [ ] **Step 4: Add `fromDumps` to `Pipeline`**

Insert after `fromStdin()`:

```java
  /**
   * Analyses thread dumps the caller collected itself — sample on your own schedule, keep the dumps,
   * then merge and analyse them later. Combine with merge(Attr.TID) to turn N samples into one
   * sampled profile.
   */
  public static Pipeline fromDumps(Collection<ThreadDump> dumps) {
    if (dumps == null) {
      throw new MjprofException("dumps must not be null");
    }
    List<ThreadDump> snapshot = List.copyOf(dumps);
    return new Pipeline(() -> new DumpsDataSource(snapshot), List.of());
  }
```

Add imports `java.util.Collection` and `java.util.List` (the latter is already present).

- [ ] **Step 5: Run the test to verify it passes**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=PipelineFromDumpsTest
```

Expected: all 6 tests pass.

- [ ] **Step 6: Confirm `DumpsDataSource` stayed out of the plugin index**

```bash
cd /Users/lifey/github/mjprof
mvn -q clean package -DskipTests
grep -c . mjprof-cli/target/classes/supported_monads.txt          # expect 31
grep DumpsDataSource mjprof-cli/target/classes/supported_monads.txt && echo "BUG: it got registered"
```

Expected: 31 lines, no `DumpsDataSource`.

- [ ] **Step 7: Commit**

```bash
git add mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/DumpsDataSource.java \
        mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java \
        mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/PipelineFromDumpsTest.java
git commit -m "feat: fromDumps source for self-collected samples"
```

---

### Task 8: Document the API and pin the text-persistence limits

The spec resolved that text round-tripping is lossy and broken for JMX headers. That must be asserted by a test and stated in the docs, not left for a user to discover.

**Files:**
- Create: `mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/TextRoundTripLimitsTest.java`
- Modify: `README.md`, `CLAUDE.md`
- Modify: `mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java` (Javadoc only)

**Interfaces:**
- Consumes: everything from Tasks 2–7.
- Produces: no new API.

- [ ] **Step 1: Write the test that pins the known losses**

```java
package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadDumpTextualParser;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the fidelity limits of persisting dumps as jstack text, so the loss is a known, tested
 * boundary rather than a surprise. See the spec's open question 2.
 */
class TextRoundTripLimitsTest {

  private static ThreadDump dumpWithCpu() {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, "worker-1");
    ti.setVal(ThreadInfoProps.TID, "0x00007f");
    ti.setVal(ThreadInfoProps.STATE, "RUNNABLE");
    ti.setVal(ThreadInfoProps.CPUNS, 1_234_567L);
    ThreadDump d = new ThreadDump();
    d.setHeader(new JStackHeader("2026-07-31 10:00:00\nFull thread dump"));
    ArrayList<ThreadInfo> tis = new ArrayList<>();
    tis.add(ti);
    d.setThreadInfos(tis);
    return d;
  }

  @Test
  void nameAndStateSurviveARoundTrip() {
    ThreadDump reparsed = ThreadDumpTextualParser.parseStringRepresentation(dumpWithCpu().toString());
    assertEquals(1, reparsed.getThreadInfos().size());
    ThreadInfo t = reparsed.getThreadInfos().get(0);
    assertEquals("worker-1", t.name());
    assertNotNull(t.state());
  }

  @Test
  void cpuNsIsLostOnARoundTrip() {
    ThreadDump reparsed = ThreadDumpTextualParser.parseStringRepresentation(dumpWithCpu().toString());
    assertNull(reparsed.getThreadInfos().get(0).cpuNs(),
        "toJStackFormat writes cpu_ns= but the parser has no case to read it back; "
            + "use Pipeline.fromDumps to keep CPU data in memory instead");
  }
}
```

Run it and adjust only the *assertions* to match observed behavior — if `cpuNs` unexpectedly survives, change the test to assert that and update the docs below accordingly. The point is a truthful record, not a predetermined answer.

- [ ] **Step 2: Run the test**

```bash
cd /Users/lifey/github/mjprof
mvn -pl mjprof-core test -Dtest=TextRoundTripLimitsTest
```

Expected: both pass, documenting the boundary.

- [ ] **Step 3: Add the Javadoc note to `Pipeline`**

Append to the class-level Javadoc:

```java
 * <p>To sample on your own schedule and analyse later, keep the ThreadDump objects and use
 * fromDumps: dumps written out as text and re-read lose count, cpu_ns, wall_ms and %cpu, because
 * the parser has no cases for them, and dumps taken over JMX do not re-split when concatenated
 * because their header is not date-prefixed.
```

- [ ] **Step 4: Add an API section to `README.md`**

Insert a `## Java API` section after the CLI usage section. Keep it short — three examples and the limits:

````markdown
## Java API

`mjprof-core` is usable as a library. Add the dependency:

```xml
<dependency>
    <groupId>com.performizeit</groupId>
    <artifactId>mjprof-core</artifactId>
    <version>1.1.0</version>
</dependency>
```

Stages are plugin instances, so their constructor arguments are typed and checked by the compiler:

```java
List<ThreadInfo> runnable = Pipeline.fromFile("dump.txt")
    .contains(Attr.STATE, "RUNNABLE")
    .sort(Attr.NAME)
    .threads();
```

Anything without a convenience method — including plugins from your own jars — goes through `map`:

```java
Pipeline.fromStdin()
    .map(new MergedCallees("java.net.SocketInputStream.read"))
    .collect();
```

Sample on your own schedule and merge later:

```java
List<ThreadDump> samples = new ArrayList<>();
samples.add(Pipeline.fromJmxWithCpu("localhost:9010", 1, 0, null, null).collectOne());
// ... more samples, on your cadence ...

List<ThreadInfo> profile = Pipeline.fromDumps(samples)
    .merge(Attr.TID)        // sums cpu_ns, merges stack trees
    .withoutNameSuffix()
    .group(Attr.NAME)
    .sortDesc(Attr.CPU_NS)
    .threads();
```

Notes:

- Collection runs on the calling thread. A source configured for several samples blocks for its own
  sleep interval.
- Failures throw `MjprofException` rather than yielding an empty result.
- `group(Attr.STACK)` and `merge(Attr.STACK)` are rejected: stack profiles have no value equality.
  Use `merge(Attr.TID)` then `tree()`.
- Keep dumps in memory to keep every property. Written as text they lose `count`, `cpu_ns`,
  `wall_ms` and `%cpu`, and JMX-sourced dumps do not re-split when concatenated.
- The expression DSL, macros and the `supported_monads.txt` plugin registry live in `mjprof-cli`,
  not in the library.
````

- [ ] **Step 5: Update `CLAUDE.md`**

The architecture section describes a single module and is now wrong in several specifics. Update:

- Commands: `mvn test` and `mvn clean package` still work from the root; single-test runs need `-pl`, e.g. `mvn -pl mjprof-core test -Dtest=StackTreeTest`. Native build is `mvn -Pnative package -pl mjprof-cli -am`.
- Note the two modules and what lives in each, with the new package names.
- State that the pipeline has two runtimes: threaded `Pipe`/`Generator` for the CLI, and the synchronous `PipelineRunner` for the API — and that plugins are shared and must keep working under both.
- Note that `mjprof-core` must stay dependency-free, and that new plugins go in core while anything touching the expression DSL goes in cli.
- Keep the existing warnings about `supported_monads.txt` being the canonical discovery path and about plugin constructor changes breaking the native build; add that codegen now runs in `mjprof-cli`.

- [ ] **Step 6: Commit**

```bash
git add mjprof-core/src/test/java/com/performizeit/mjprof/core/pipeline/TextRoundTripLimitsTest.java \
        mjprof-core/src/main/java/com/performizeit/mjprof/core/pipeline/Pipeline.java \
        README.md CLAUDE.md
git commit -m "docs: Java API usage and text round-trip limits"
```

---

### Task 9: Full verification

**Files:** none modified. This task only runs things and reports.

**Interfaces:**
- Consumes: everything.
- Produces: a verified build.

- [ ] **Step 1: Clean build with all tests**

```bash
cd /Users/lifey/github/mjprof
mvn clean package
```

Expected: `BUILD SUCCESS`, both modules, zero failures. Record the test count.

- [ ] **Step 2: Re-run the golden diff in full**

Re-run the ten expression captures from Task 1 Step 8 against the freshly built jar and diff against `/tmp/mjprof-golden/*.txt`. Expected: `OK` for all ten. This is the final proof that a rename plus a `GroupByProp` change altered no CLI output.

- [ ] **Step 3: Verify the plugin index**

```bash
grep -c . mjprof-cli/target/classes/supported_monads.txt                          # expect 31
grep -c "com.performizeit.mjprof.core.plugins" mjprof-cli/target/classes/supported_monads.txt  # expect 31
unzip -l mjprof-cli/target/mjprof-cli-1.1.0.jar | grep supported_monads           # at jar root
```

- [ ] **Step 4: Verify core has no external dependency**

```bash
mvn -q -pl mjprof-core dependency:tree
```

Expected: only `mjprof-core` itself and test-scope junit artifacts. Any compile-scope third-party entry is a defect — find what pulled it in and remove the import.

- [ ] **Step 5: Native image build**

```bash
mvn -Pnative package -pl mjprof-cli -am -DskipTests
mjprof-cli/target/mjprof-cli 'path/mjprof-core/src/test/res/test.txt/.count'
```

Expected: a binary is produced and prints the same count as golden `10-count`. Requires GraalVM with `native-image` on the path; if unavailable, say so explicitly in the completion report rather than marking this verified.

- [ ] **Step 6: Report**

State plainly: test count, golden diff result for each of the ten expressions, `supported_monads.txt` line count, core dependency tree result, and whether the native build ran or was skipped for lack of GraalVM. No success claim for anything not actually run.

---

## Notes for the implementer

- **The golden capture in Task 1 Step 1 must happen before any file moves.** There is no way to reconstruct it afterwards.
- Task 1 is the only task that cannot be partially completed — a half-renamed tree does not compile. Everything after it is independently revertible.
- If javac rejects the four `map` overloads as having the same erasure, fall back to distinct names (`mapThread`, `reduce`, `filter`, `sortBy`) and update Tasks 5–7 accordingly. Note the change in the commit message; do not silently diverge from the spec.
- Do not "fix" the lossy text round-trip, the missing JMX connect timeout, or `Profile`'s lack of value equality. All three are recorded follow-ups. Fixing them here makes the diff unreviewable.
