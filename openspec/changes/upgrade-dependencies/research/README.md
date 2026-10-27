# Research

Status: REQUIRED. Version choices and migration steps depend on current
releases and their release notes.

## Questions and answers

### Q1. Latest stable releases

- Answer: from `https://repo1.maven.org/maven2/<group>/<artifact>/maven-metadata.xml`,
  retrieved 2026-10-27, stable versions only (pre-releases skipped):

  | Artifact | Today | Latest stable |
  | --- | --- | --- |
  | `org.apache.logging.log4j:log4j-core` | 2.14.0 | 2.26.1 (3.0.0-beta3 is a pre-release); removed by D1 |
  | `org.slf4j:slf4j-api` | 1.7.30 | 2.0.20 (2.1.0-alpha1 is a pre-release) |
  | `ch.qos.logback:logback-classic` | not used | 1.6.6 (repository updated 2026-10-25) |
  | `com.google.guava:guava` | 31.1-jre | 33.7.2-jre |
  | `com.google.code.gson:gson` | 2.8.9 | 2.15.0 |
  | `com.jayway.jsonpath:json-path` | 2.8.0 | 3.0.0 |
  | `net.minidev:json-smart` (property only) | 2.4.10 | 2.6.0 |
  | `io.dropwizard.metrics:metrics-core` | 4.1.17 (managed, unused) | 4.2.40 |
  | `com.codahale.metrics:metrics-core` (Yammer) | 3.0.2 | replaced by Dropwizard |
  | `com.google.auto.service:auto-service` | 1.0.1 | 1.1.1 |
  | `org.hamcrest:hamcrest` | `hamcrest-all` 1.3 | 3.0 |
  | `com.google.truth:truth` | 1.1.5 | 1.4.5 |
  | `org.junit:junit-bom` | 5.14.4 | 6.1.3 |
  | `org.mockito:mockito-core` | 5.24.0 | 5.24.0 |
  | `org.openjdk.jmh:jmh-core` | 1.37 | 1.37 |
  | `com.google.protobuf:protobuf-java` | 3.22.3 (unused) | 4.36.2; removed |
  | `com.github.seancfoley:ipaddress` | 5.4.0 (unused) | 5.6.2; removed |
  | `jakarta.xml.bind:jakarta.xml.bind-api` | 2.3.3 | 4.0.5 (4.1.0-M1 is a milestone); 2.3.x patch per Q7 |
  | `com.sun.xml.bind:jaxb-impl` | 2.3.8 | 4.0.9; 2.3.x patch per Q7 |
- Sources: Maven Central metadata as above.
- Confidence: high.
- Disagreement / drift risk: releases continue; the final gate repeats the audit.

Security: Apache Log4j security page (https://logging.apache.org/security.html,
retrieved 2026-10-27): CVE-2021-44228, CVSS 10.0, `log4j-core` versions
`[2.13.0, 2.15.0)` among others, fixed in 2.15.0 for Java 8 and later;
CVE-2021-44832, CVSS 6.6, versions before 2.17.0. The project's 2.14.0 is in
both ranges.

### Q2. Logback 1.6.6 configuration

- Answer: pending (Logback manual: `AsyncAppender`, `<include>`, shutdown hook, variable defaults).
- Sources: pending.
- Confidence: pending.
- Disagreement / drift risk: Logback 1.5 changed conditional configuration.

### Q3. SLF4J 2.0 provider lookup

- Answer: slf4j-api 2.0.x finds providers through `ServiceLoader`; Logback
  1.3 and later is such a provider. Bindings for SLF4J 1.7 (static binder) are
  listed and ignored. With no provider, SLF4J warns "No SLF4J providers were
  found" and uses a no-operation logger, so a user who adds no backend gets
  no logs and one warning (the **BREAKING** note of the proposal says this).
- Sources: SLF4J error codes page (https://www.slf4j.org/codes.html, retrieved 2026-10-27).
- Confidence: high.
- Disagreement / drift risk: none known.

### Q4. json-path 3.0.0 against `JsonContent`

- Answer: 3.0.0 follows 2.11.0 and is a "major release with Java 17
  baseline" (release commit); it adds Jackson 3 providers, and the default
  provider stays `JsonSmartJsonProvider` (Spring Boot issue 50786). No removal
  of `Configuration`, `DocumentContext`, `JsonPath`, `Option`, or `TypeRef` is
  reported; the compile and `JsonContent` tests of task 3.2 decide, with
  2.11.0 as the fallback.
- Sources: https://github.com/json-path/JsonPath/commit/470b56661adc7b677f5e225529f80c1fefb4104d;
  https://github.com/spring-projects/spring-boot/issues/50786 (both retrieved 2026-10-27).
- Confidence: medium (no full changelog found; the build is the check).
- Disagreement / drift risk: major version.

### Q5. Guava and Gson behavior changes for the used APIs

- Answer: Gson 2.11.0 added the `Strictness` API (`GsonBuilder#setStrictness`,
  `JsonReader#setStrictness`); the reader's default is `LEGACY_STRICT`, the
  old non-lenient behavior, so `xacml-json` parsing stays as it is; current
  `JsonReader` also has `setNestingLimit` with a default limit (used by
  `improve-xacml-parsing`). Guava 32 to 33: the compile and module tests of
  task 3.1 are the check (no changelog search yet).
- Sources: Gson 2.11.0 release discussion (https://github.com/google/gson/discussions/2677);
  `JsonReader` source on the main branch
  (https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/stream/JsonReader.java),
  both retrieved 2026-10-27.
- Confidence: high for Gson; medium for Guava until the build.
- Disagreement / drift risk: none known.

### Q6. JUnit 6, Truth 1.4, Hamcrest 3.0

- Answer: JUnit 6 needs Java 17 or later and Surefire 3.0 or later (the
  build has 3.6.1), gives platform, Jupiter, and Vintage one version number,
  removes APIs that 5.x deprecated, defaults `@EnabledForJreRange` to
  `JAVA_17`, and deprecates `junit-jupiter-migrationsupport`. The migration is
  the BOM version plus fixes for any removed API the compile finds. Truth and
  Hamcrest: pending.
- Sources: JUnit 6.0.0-M1 and 6.0.3 release notes
  (https://junit.org/junit5/docs/6.0.0-M1/release-notes,
  https://docs.junit.org/6.0.3/release-notes, retrieved 2026-10-27).
- Confidence: high for JUnit; pending for Truth and Hamcrest.
- Disagreement / drift risk: major version.

### Q7. Latest JAXB 2.3.x patches

- Answer: `jakarta.xml.bind:jakarta.xml.bind-api` 2.3.3 is already the last
  2.3.x; `com.sun.xml.bind:jaxb-impl` 2.3.8 moves to 2.3.9 (the last 2.3.x;
  `org.glassfish.jaxb:jaxb-runtime` also ends at 2.3.9).
- Sources: Maven Central `maven-metadata.xml` of the three artifacts, retrieved 2026-10-27.
- Confidence: high.
- Disagreement / drift risk: none known.

### Q8. Vulnerability scan for the gate

- Answer: pending (tools installed locally; whether they run offline).
- Sources: pending.
- Confidence: pending.
- Disagreement / drift risk: databases change daily; record the scan date.

## Lanes

| Lane | Profile / model | Report | Status |
| --- | --- | --- | --- |
| conductor | Claude Code, Claude Opus 5.5 | this file (Q1, security note) | started 2026-10-27 |

## Synthesis

n/a
