FINAL REPORT (rv002-plan-arch, Agy, Gemini 4 Argon (high))

### 1. Lane, CLI, and Model

- **Lane**: `rv002-plan-arch` (Architecture reviewer)
- **CLI**: Agy (Antigravity)
- **Model**: Gemini 4 Argon (high)
- **Change**: `openspec/changes/add-pdp-jmh-benchmarks` (Task 1.1 — plan review)

---

### 2. Verdict

**APPROVE WITH CHANGES**

The overall architecture of `xacml-jmh`—a standalone leaf module using `ServiceLoader` (`@AutoService`) for `PolicyStore` and `PolicyCorpusProvider`, checking corpus decisions once in `@Setup(Level.Trial)` before measurement, and packaging an uber-JAR with `ServicesResourceTransformer`—is sound and fits the `xacml4j` reactor without modifying existing modules. However, four **major** specification/contract issues and five **minor/nit** issues should be resolved in the OpenSpec artifacts before launching the parallel Group 3 implementation swarm.

---

### 3. Findings (Most Severe First)

#### Finding 1 (Major) — `InMemoryPolicyRepository` unknown-ID `NullPointerException` also occurs in `getPolicySets`/`getPolicySet` when a `Policy` ID is queried on `getPolicySet` or a `PolicySet` ID is queried on `getPolicy`

- **Artifact and section**:
  - [spec.md](openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md#L82-L98) (`Requirement: Snapshot store answers like the in-memory store`)
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L27-L29) (`Context`) and [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L200-L203) (`D7: Three benchmark classes`, `PolicyStoreLookupBenchmark`)
  - [proposal.md](openspec/changes/add-pdp-jmh-benchmarks/proposal.md#L91-L94) (`Known Engine Quirk`)
  - [tasks.md](openspec/changes/add-pdp-jmh-benchmarks/tasks.md#L8)
- **Source evidence**:
  - Checked in [InMemoryPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepository.java#L75-L104) and [InMemoryPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepository.java#L192-L199).
  - Checked in [AbstractPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java#L97-L122).
- **Problem**:
  The plan notes that `InMemoryPolicyRepository.getPolicies` calls `find(null, ...)` and throws `NullPointerException` for an unknown ID (checked in `InMemoryPolicyRepository.java:81-87, 198`). The exact same bug also exists in `InMemoryPolicyRepository.getPolicySets` (`InMemoryPolicyRepository.java:97-103`), which looks up `this.policySets.get(id)` and passes `null` to `find(...)` (`InMemoryPolicyRepository.java:198`). Furthermore, `AbstractPolicyRepository.getPolicy(id, ...)` delegates to `getPolicies(id, ...)` (`AbstractPolicyRepository.java:99-101`) and `AbstractPolicyRepository.getPolicySet(id, ...)` delegates to `getPolicySets(id, ...)` (`AbstractPolicyRepository.java:115-117`).
  Because `InMemoryPolicyRepository` stores `Policy` instances in `this.policies` and `PolicySet` instances in `this.policySets`:
  1. In `rsa2008`, all 9 XML files are `<PolicySet>` documents (checked in `xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008PerformanceTestCase.java:53-62`), so `this.policies` is empty. Calling `inMemory.getPolicy(id, null, null, null)` for any `rsa2008` ID—or for the root `PolicySet` ID of `synthetic:100`—throws `NullPointerException` on `InMemoryPolicyRepository`, while returning `null` on `SnapshotPolicyRepository`.
  2. Conversely, in `synthetic:100`, the 100 leaf items are `<Policy>` documents, so calling `inMemory.getPolicySet(id, null, null, null)` on a leaf policy ID throws `NullPointerException` on `InMemoryPolicyRepository`, while returning `null` on `SnapshotPolicyRepository`.
- **Why it matters**:
  - `SnapshotPolicyRepositoryTest` ([spec.md](openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md#L84-L88): *"WHEN `get`, `getPolicy`, and `getPolicySet` are called for each loaded id and version and for latest-version lookups / THEN both repositories return the same `CompositeDecisionRule` instances"*) will fail with `NullPointerException` on `InMemoryPolicyRepository` if the test calls both `getPolicy` and `getPolicySet` on every loaded ID regardless of whether that ID is a `Policy` or a `PolicySet`.
  - `PolicyStoreLookupBenchmark.getLatest` ([design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L202): *"calls `getPolicy` or `getPolicySet` with null version bounds"*) will throw `NullPointerException` on `store=in-memory` unless it dispatches to `getPolicy` for `Policy` entries and `getPolicySet` for `PolicySet` entries.
- **Proposed change**:
  1. In `spec.md` (`Scenario: Parity on a loaded corpus`, lines 84–88) and `design.md` D7 (`PolicyStoreLookupBenchmark`, line 202), specify that `getPolicy` is called for `Policy` entries and `getPolicySet` is called for `PolicySet` entries (matching `DefaultPolicyReferenceResolver.java:92, 124`, checked in `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java:92, 124`).
  2. In `proposal.md:91-94`, `design.md:27-29`, `spec.md:90-98`, and `tasks.md:8`, document that both `getPolicies`/`getPolicy` and `getPolicySets`/`getPolicySet` in `InMemoryPolicyRepository` throw `NullPointerException` when queried for an ID absent from that map.

---

#### Finding 2 (Major) — Missing class/method contract between parallel Group 3 lanes (`tests` task 3.1 vs `benchmarks` task 3.5) for `PdpDecideBenchmarkTest`

- **Artifact and section**:
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L195-L228) (`D7: Three benchmark classes`, `D8: Shared PDP state and per-thread request cursor`)
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L288) (`Test Strategy`) and [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L337-L356) (`Execution Plan`)
  - [spec.md](openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md#L183-L188) (`Scenario: Unknown request id`)
  - [tasks.md](openspec/changes/add-pdp-jmh-benchmarks/tasks.md#L19-L23) (tasks 3.1 and 3.5)
- **Source evidence**:
  - Checked in `design.md:113-163` (D4 spine signatures) and `design.md:195-228` (D7/D8 prose description).
- **Problem**:
  Group 3 runs four implementation lanes in parallel (`stores`, `corpora`, `benchmarks`, `tests`) against the spine interfaces from Group 2 (`design.md:337-356`). Each lane runs `mvn test-compile` with other lanes' files excluded (`design.md:352-353`), and task 3.1 (`tests`) runs first as the red-test step (`tasks.md:19`).
  However, while D4 defines the exact Java signatures for `SnapshotPolicyRepository`, `Rsa2008CorpusProvider`, `SyntheticCorpusProvider`, and `DirectoryCorpusProvider` (and Group 2 creates stubs for them in task 2.3), D7/D8 does **not** define the Java method/field/state signatures for `PdpDecideBenchmark` (nor is a Group 2 stub planned for `PdpDecideBenchmark`).
  If `PdpDecideBenchmarkTest` in task 3.1 references `PdpDecideBenchmark` directly:
  1. `mvn test` in task 3.1 will fail to compile (`cannot find symbol: class PdpDecideBenchmark`) rather than running as a failing test.
  2. The `tests` lane (`Claude Opus 5.5`) and `benchmarks` lane (`Codex GPT-6.1 Terra`) will independently invent incompatible field names, `@State` inner classes, or `@Setup` method names for `PdpDecideBenchmark`.
- **Why it matters**:
  Guaranteed compile/merge conflict at Group 3 integration (`tasks.md:19, 23`).
- **Proposed change**:
  Either:
  1. **(Preferred)** Put request selection on `PolicyCorpus` (or `PdpFixture`) in D4—for example:
     ```java
     public List<CorpusRequest> selectRequests(String requestSelector)
     ```
     which returns `requests` when `requestSelector` is `"all"`, returns the single matching `CorpusRequest` when found, or throws `IllegalArgumentException` naming the unknown ID—and have `PdpDecideBenchmark` call it in `@Setup(Level.Trial)`. Or,
  2. Add a minimal stub for `PdpDecideBenchmark` in Group 2 (task 2.3) and specify its exact `@Param` field names (`store`, `corpus`, `request`), `@Setup` method signature (`public void setup()`), and `@Benchmark` method signature (`public Result decide(RequestCursor cursor)`) in D7/D8.

---

#### Finding 3 (Major) — API mismatch between `spec.md` (`Scenario: Store argument`) and `design.md` D4 (`PolicyStores.find(String name)`)

- **Artifact and section**:
  - [spec.md](openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md#L22-L25) (`Scenario: Store argument`)
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L114-L123) and [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L138-L156) (`D4: Spine interfaces`)
- **Source evidence**:
  - Checked in `spec.md:22-25` and `design.md:114-156`.
- **Problem**:
  `spec.md:22-25` specifies:
  > **Scenario: Store argument**
  > - **GIVEN** a store named `probe` that records its argument
  > - **WHEN** the harness looks up `probe:a:b`
  > - **THEN** the store receives the argument `a:b`, and `probe` alone gives an empty argument (`PolicyStoresTest`)

  In `design.md` D4, `PolicyCorpora.load(String value)` has the comment `// Selection.parse, then provider.load` and passes `selection.argument()` to `PolicyCorpusProvider.load(Optional<String> argument)`.
  By contrast, `PolicyStores` only has:
  ```java
  public final class PolicyStores {
      public static PolicyStore find(String name);
      public static Set<String> names();
  }
  ```
  `PolicyStore` is a singleton `ServiceLoader` provider whose `argument` is only passed inside `PolicyStoreContext` when `store.create(PolicyStoreContext context)` is called (`design.md:114-120`). Looking up a `PolicyStore` via `PolicyStores.find(...)` cannot pass `a:b` to `PolicyStore` unless `PolicyStores` parses the `store` selector and either creates the repository or returns a bound handle/context.
- **Why it matters**:
  Task 2.3 (spine author) and task 3.1 (`PolicyStoresTest` author) will make conflicting assumptions about whether `PolicyStores.find` accepts `"probe:a:b"` or `"probe"`, and how `probe` receives `Optional.of("a:b")`.
- **Proposed change**:
  In `design.md` D4, add a creation helper on `PolicyStores` (mirroring `PolicyCorpora.load(String value)`), or have `PdpFixture` / `PolicyStores` expose:
  ```java
  public final class PolicyStores {
      public static PolicyStore find(String name);
      public static PolicyRepository create(String value, String repositoryId); // Selection.parse, then store.create(context)
      public static Set<String> names();
  }
  ```
  so `PolicyStores.create("probe:a:b", "test-repo")` (or `PolicyStores.create(String value, String id, FunctionProvider, DecisionCombiningAlgorithmProvider)`) parses `probe:a:b` with `Selection.parse`, looks up `probe`, and passes `Optional.of("a:b")` in `PolicyStoreContext` to `probe.create(...)`.

---

#### Finding 4 (Major) — `synthetic:1` and `synthetic:2` request generation in D9 collides with unique request ID invariant in D4

- **Artifact and section**:
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L134-L136) (`D4: PolicyCorpus`)
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L235-L243) (`D9: Built-in corpora`, `synthetic:<n>`)
  - [spec.md](openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md#L63) and [spec.md](openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md#L134-L138)
- **Source evidence**:
  - Checked in `design.md:136, 241-243` and `spec.md:63, 135-137`.
- **Problem**:
  `design.md` D9 (lines 241–243) states:
  > *"Requests target policies `0`, `n/2`, and `n-1` with a `Permit` and a `Deny` case each, plus one request for an unknown resource (`NotApplicable`)."*
  And `design.md` D4 (line 136) states that `PolicyCorpus` enforces *"non-empty lists, unique names and ids"*.
   Meanwhile, `spec.md:63` (`WHEN a synthetic corpus of 1 policy is loaded...`) and `spec.md:135` (`GIVEN the synthetic corpus with arguments 1, 10, and 100`) explicitly test `synthetic:1`.
  For `n = 1`, the policy indices `0`, `n/2`, and `n-1` are all `0` (`0, 0, 0`). For `n = 2`, they are `0, 1, 1`. If `SyntheticCorpusProvider` names requests by target policy index (e.g., `policy-0-permit`, `policy-0-deny`), `new PolicyCorpus(...)` will throw `IllegalArgumentException` on `synthetic:1` and `synthetic:2` for duplicate request IDs.
- **Why it matters**:
  `SyntheticCorpusTest` and `PdpFixtureTest` (`spec.md:63, 135`) will fail on `synthetic:1` unless D9 defines how `0, n/2, n-1` behave when `n < 3`.
- **Proposed change**:
  Specify in `design.md` D9 that target policy indices are deduplicated in order (`IntStream.of(0, n / 2, n - 1).distinct()`), producing 1 target policy (3 requests total) for `n = 1`, 2 target policies (5 requests total) for `n = 2`, and 3 target policies (7 requests total) for `n >= 3` (or specify slot-based request IDs such as `first-permit`, `middle-permit`, `last-permit`).

---

#### Finding 5 (Minor) — `PolicySource` record has a raw `byte[]` component (reference equality & mutability), and `spec.md:131` mentions "request bytes" when `CorpusRequest` holds `RequestContext`

- **Artifact and section**:
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L130-L135) (`D4: Spine interfaces`)
  - [spec.md](openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md#L129-L133) (`Scenario: Deterministic output`)
- **Source evidence**:
  - Checked in `design.md:130-135`, `spec.md:129-133`, and [RequestContext.java](xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java#L366-L392).
- **Problem**:
  1. `public record PolicySource(String name, MediaType mediaType, byte[] content)` uses default record `equals`/`hashCode`, which on `byte[]` delegates to `Object.equals` (`==`) and `System.identityHashCode`. Two `PolicySource` instances created by two calls to `PolicyCorpora.load("synthetic:50")` with identical byte arrays will evaluate `source1.equals(source2) == false`, and `content()` exposes the backing array to mutation (which also conflicts with the repo's immutability and JEP 401 value-record conventions).
  2. `spec.md:131` says: *"WHEN their policy and request bytes are compared / THEN they are identical (`SyntheticCorpusTest`)"*, whereas `CorpusRequest(String id, RequestContext request, Decision expected)` in D4 holds a parsed `RequestContext`, not `byte[]`. (`RequestContext` implements value-based `equals` and `hashCode`, checked in `RequestContext.java:366-392`.)
- **Why it matters**:
  `SyntheticCorpusTest` comparing two `PolicyCorpus` objects or their `PolicySource` lists will fail if it calls `.equals()`, and the `tests` lane may look for raw request bytes on `CorpusRequest` that do not exist in D4.
- **Proposed change**:
  In `design.md` D4, specify that `PolicySource` defensively clones `content` in its compact constructor and `content()` accessor and overrides `equals`/`hashCode` using `Arrays.equals`/`Arrays.hashCode`. In `spec.md:131`, state *"WHEN their policy sources and requests are compared / THEN they are equal (`SyntheticCorpusTest`)"*.

---

#### Finding 6 (Minor) — `SnapshotPolicyRepository` (D10) must override `get(String id, Version v)` and pass a direct `Executor` to `AbstractPolicyRepository`

- **Artifact and section**:
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L23-L26) (`Context`), [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L200-L205) (`D7`), and [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L249-L257) (`D10: SnapshotPolicyRepository`)
- **Source evidence**:
  - Checked in [AbstractPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java#L66-L84) and [AbstractPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java#L205-L210).
  - Checked in [InMemoryPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepository.java#L107-L114).
  - Checked in [VersionMatch.java](xacml-core/src/main/java/org/xacml4j/v30/VersionMatch.java#L58-L70).
- **Problem**:
  1. `AbstractPolicyRepository.get(String id, Version v)` (`AbstractPolicyRepository.java:205-210`) is implemented as:
     ```java
     VersionMatch m = new VersionMatch(v.getValue());
     CompositeDecisionRule r = getPolicy(id, m, null, null);
     return (r == null) ? getPolicySet(id, m, null, null) : r;
     ```
     Constructing `new VersionMatch(...)` runs 4 `String.replaceAll` calls and `Pattern.compile(pattern)` on every call (`VersionMatch.java:58-70`). `InMemoryPolicyRepository` overrides `get(String id, Version v)` (`InMemoryPolicyRepository.java:107-114`) with a direct map lookup (`policies.get(id)` / `policySets.get(id)`). If `SnapshotPolicyRepository` only implements the abstract methods and forgets to override `get(String id, Version v)`, `PolicyStoreLookupBenchmark.getByIdAndVersion` on `snapshot` will benchmark regex compilation instead of map lookup.
  2. `AbstractPolicyRepository`'s 3-argument constructor (`AbstractPolicyRepository.java:66-72`) calls `Executors.newSingleThreadExecutor()`, which allocates a `ThreadPoolExecutor` wrapped in a `FinalizableDelegatedExecutorService` (registering a JVM finalizer/cleaner) on every repository instantiation (`PolicyStoreLoadBenchmark`), and spawns a non-daemon OS thread that has no `close()` method if a listener notification ever fires. `AbstractPolicyRepository` provides a 4-argument constructor accepting an `Executor` (`AbstractPolicyRepository.java:74-84`).
- **Why it matters**:
  Fairness and allocation accuracy of `PolicyStoreLookupBenchmark.getByIdAndVersion` and `PolicyStoreLoadBenchmark` on `SnapshotPolicyRepository`.
- **Proposed change**:
  In `design.md` D10, specify that `SnapshotPolicyRepository`:
  1. Calls the 4-argument `super(id, functions, decisionAlgorithms, Runnable::run)` constructor.
  2. Overrides `get(String id, Version v)` with a direct lookup in its volatile `policies` and `policySets` maps (matching `InMemoryPolicyRepository.java:107-114`).

---

#### Finding 7 (Minor) — `PdpFixture.parse` (D4/D7) should cache discovered `MarshallingProvider` / `PolicyUnmarshaller` instances

- **Artifact and section**:
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L154-L166) (`D4: PdpFixture`) and [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L204) (`D7: PolicyStoreLoadBenchmark.importXml`)
- **Source evidence**:
  - Checked in [MarshallingProvider.java](xacml-core/src/main/java/org/xacml4j/v30/marshal/MarshallingProvider.java#L72-L77).
  - Checked in [PolicyImportTool.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java#L53-L55).
  - Checked in [BaseJAXBUnmarshaller.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L82-L148) and [XacmlPolicyUnmarshaller.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/XacmlPolicyUnmarshaller.java#L41-L65).
- **Problem**:
  `MarshallingProvider.discoverProviders()` (`MarshallingProvider.java:72-77`, also used by `new PolicyImportTool(...)` at `PolicyImportTool.java:54`) invokes `ServiceLoader.load(MarshallingProvider.class)` on every call. Meanwhile, `XacmlPolicyUnmarshaller` is stateless and thread-safe (`BaseJAXBUnmarshaller.java:86, 140` creates a fresh JAXB `Unmarshaller` and SAX `XMLReader` per `unmarshal` call using the shared static `JAXBContext` in `JAXBUtils.java:46`). If `PdpFixture.parse(PolicySource)` calls `MarshallingProvider.discoverProviders()` per policy, `PolicyStoreLoadBenchmark.importXml` (`design.md:204`) will measure `ServiceLoader` classpath discovery `n` times per benchmark invocation.
- **Why it matters**:
  Keep `PolicyStoreLoadBenchmark.importXml` focused on XML unmarshalling + repository insertion rather than repeated `ServiceLoader` scans.
- **Proposed change**:
  In `design.md` D4, state that `PdpFixture` caches the discovered `MarshallingProvider` / `PolicyUnmarshaller` map once in a `static final` field.

---

#### Finding 8 (Minor) — XACML 3.0 `deny-overrides` short-circuits on `Deny`, so D9's statement that "every decision checks every policy target" is only true for `Permit`, `NotApplicable`, and the `n-1` `Deny` request

- **Artifact and section**:
  - [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L235-L244) (`D9: Built-in corpora`, `synthetic:<n>`)
- **Source evidence**:
  - Checked in [DenyOverrides.java](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L73-L78).
  - Checked against OASIS XACML 3.0 Core Specification Appendix C.2 (`urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides`).
- **Problem**:
  `design.md` D9 (lines 236–238) says:
  > *"The root policy set uses the 3.0 `deny-overrides` policy combining algorithm and holds `n` `PolicyIdReference` elements, so every decision checks every policy target and the cost grows with `n`."*
  Under XACML 3.0 Appendix C.2 and `DenyOverrides.java:76-78`:
  ```java
  if (decision == Decision.DENY) {
      return Decision.DENY;
  }
  ```
  `deny-overrides` returns `Decision.DENY` immediately upon encountering the first `Deny` decision. Therefore:
  - Every `Permit` request (`0`, `n/2`, `n-1`), the `NotApplicable` request, and the `Deny` request for `n-1` evaluate all `n` policy targets (100% linear in `n`).
  - The `Deny` request for policy `0` evaluates only 1 policy (`O(1)`), and the `Deny` request for policy `n/2` evaluates `n/2 + 1` policies.
- **Why it matters**:
  The expected decisions (`Permit`, `Deny`, `NotApplicable`) are 100% conformant with XACML 3.0, and the 7-request round-robin average still scales linearly with `n` (`(5.5 / 7) * n` policies evaluated per call), so the corpus design is valid. However, single-request runs with `-p request=<deny-0-id>` will be `O(1)` across `n=10, 100, 1000`, which would look surprising if D9 claims every decision checks every policy target.
- **Proposed change**:
  Clarify in `design.md` D9 that `Permit`, `NotApplicable`, and last-policy `Deny` requests check all `n` policy targets while early `Deny` requests (`0`, `n/2`) short-circuit at the matching policy per XACML 3.0 Appendix C.2.

---

#### Finding 9 (Nit) — Stale task reference (`task 2.6` vs `task 3.6`), duplicate `-rff` filename in Benchmark Plan, and explicit `<outputFile>` in `maven-shade-plugin`

- **Artifact and section**:
  - [research/README.md](openspec/changes/add-pdp-jmh-benchmarks/research/README.md#L47), [spec.md](openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md#L30), [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L88-L92) (`D2`), [design.md](openspec/changes/add-pdp-jmh-benchmarks/design.md#L306-L308) (`Benchmark Plan`)
- **Source evidence**:
  - Checked in `tasks.md:10-26`, `evidence/README.md:15`, and local Maven repo `maven-shade-plugin-3.6.0.pom`.
- **Problem**:
  1. `research/README.md:47` and `spec.md:30` refer to `task 2.6` for the forked-JVM external-store smoke check, whereas in `tasks.md:24` and `evidence/README.md:15` that task is `task 3.6` (Group 2 only has tasks `2.1`–`2.4`).
  2. In `design.md:306-307` (`Benchmark Plan`), both the main baseline command and the scaling command pass `-rff <run>/results.json` in the same `<run>` directory, which will overwrite the first `results.json` with the second command's output (and both commands run `synthetic:100`).
  3. In `design.md:88-92` (`D2`), `maven-shade-plugin` 3.6.0 writes `target/benchmarks.jar` only when `<outputFile>${project.build.directory}/benchmarks.jar</outputFile>` (or `<build><finalName>benchmarks</finalName></build>`) is configured; `<finalName>benchmarks</finalName>` inside the shade plugin `<configuration>` is only honored when `shadedArtifactAttached=true`. Also, in `design.md:244-247` (`D9`), `DirectoryCorpus` should specify sorting `policies/` and `requests/` entries lexicographically by file name (`Files.list` order is filesystem-dependent).
- **Proposed change**:
  1. Replace `task 2.6` with `task 3.6` in `research/README.md:47` and `spec.md:30`.
  2. In `design.md:306-307`, write the scaling run to `-rff <run>/results-scaling.json` with `-p corpus=synthetic:10,synthetic:1000`.
  3. In `design.md` D2, specify `<outputFile>${project.build.directory}/benchmarks.jar</outputFile>`, and in D9 specify sorting `policies/` and `requests/` by file name.

---

### 4. Items Checked and Found Correct

1. **Maven reactor & annotation processor inheritance (`design.md` D1, D2)**:
   - Checked in [pom.xml](pom.xml#L275-L281): the root POM defines `<annotationProcessorPaths>` with `com.google.auto.service:auto-service:1.0.1`. In Maven 3.9 with `maven-compiler-plugin:3.11.0`, declaring `<annotationProcessorPaths combine.children="append">` in `xacml-jmh/pom.xml` with `org.openjdk.jmh:jmh-generator-annprocess:1.37` preserves `auto-service` and runs both processors under `<release>21</release>`.
   - Checked in local Maven repo (`oss-parent-7.pom`, `jmh-core-1.37.pom`, `jmh-generator-annprocess-1.37.pom`, `maven-shade-plugin-3.6.0.pom`): `<maven.deploy.skip>true</maven.deploy.skip>` is respected by `maven-deploy-plugin`, and all required JMH 1.37 and Shade 3.6.0 artifacts are cached locally.
   - Checked `ServicesResourceTransformer` requirement: `xacml-core`, `xacml-pdp`, `xacml-xml`, and `xacml-json` all publish `META-INF/services/org.xacml4j.v30.marshal.MarshallingProvider`, `FunctionProvider`, and `DecisionCombiningAlgorithmProvider`; merging them with `ServicesResourceTransformer` and stripping `META-INF/*.SF`, `META-INF/*.DSA`, `META-INF/*.RSA` is required and sufficient for `java -jar xacml-jmh/target/benchmarks.jar`.
   - Checked second `<resource>` root `../xacml-conformance-tests/src/test/resources` with `<includes><include>rsa2008-interop/**</include></includes>`: packages only the 43 RSA 2008 files (~380 KB) at `rsa2008-interop/*` on the classpath without copying vendored files or pulling in the other ~13 MB of OASIS conformance resources.
2. **PDP context wiring and reference caching (`design.md` Context, D4, D7)**:
   - Checked in [PolicyDecisionPointContextFactory.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java#L99) and [DefaultPolicyReferenceResolver.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java#L55-L138): `DefaultPolicyDecisionPoint` always wraps the `PolicyRepository` in a `DefaultPolicyReferenceResolver` whose 1024-entry Guava caches memoize resolved `PolicyIDReference` and `PolicySetIDReference` targets after the first evaluation. Pairing `PdpDecideBenchmark` with `PolicyStoreLookupBenchmark` and `PolicyStoreLoadBenchmark` (D7) is the right architectural response since changing `xacml-pdp` is out of scope.
   - Checked in [PolicyDecisionPointBuilder.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java#L66-L72) and [PolicyDecisionPointBuilder.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java#L141-L160): `decisionCache` defaults to `NoCachePolicyDecisionCache` and `decisionAuditor` defaults to `NoAuditPolicyDecisionPointAuditor`.
3. **XACML 2.0 / 3.0 `Indeterminate` decision matching (`spec.md:57-60`, `research/README.md` Q4, `design.md` D5)**:
   - Checked in [DefaultPolicyDecisionPoint.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L184-L193), [Decision.java](xacml-core/src/main/java/org/xacml4j/v30/Decision.java#L30-L48), and [Xacml20ResponseContextUnmarshaller.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20ResponseContextUnmarshaller.java#L90-L116): XACML 3.0 extended indeterminate flavors (`INDETERMINATE_D`, `INDETERMINATE_P`, `INDETERMINATE_DP`, OASIS XACML 3.0 §7.10 / Appendix C) exist only during evaluation and on the in-memory `Result.getDecision()`, while XACML 2.0/3.0 XML and JSON responses only represent `<Decision>Indeterminate</Decision>` (OASIS XACML 3.0 §5.53) and unmarshal to `Decision.INDETERMINATE`. Checking `expected.isIndeterminate() ? actual.isIndeterminate() : actual == expected` in `@Setup(Level.Trial)` is exact and spec-compliant.
4. **RSA 2008 corpus structure and response parsing (`design.md` Context, D5, D9)**:
   - Checked in [RSA2008PerformanceTestCase.java](xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008PerformanceTestCase.java#L53-L99), [conformance-manifest.csv](xacml-conformance-tests/src/test/manifest/conformance-manifest.csv#L863-L879), and all 17 `XacmlResponse-*.xml` files under `xacml-conformance-tests/src/test/resources/rsa2008-interop/`: root policy is `http://www.va.gov/xacml/policyset/hl7-top-level-policyset` v`1.0` (`XacmlPolicySet-01-top-level.xml:10-12`), all 17 manifest rows are `executed`, and all 17 response XML files contain single-`<Result>` XACML 2.0 responses (`Permit` or `Deny`) that unmarshal cleanly via `Xacml20ResponseContextUnmarshaller`.
5. **XACML 3.0 synthetic corpus evaluation semantics (`design.md` D9)**:
   - Checked in [PolicySet.java](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java#L60-L92), [Policy.java](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java#L62-L95), [BaseCompositeDecisionRule.java](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L172-L234), [Rule.java](xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java#L96-L157), and [DenyOverrides.java](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L57-L116):
     - A request matching policy `i`'s `resource-id` with `action-id = "permit"` yields `Match` on policy `i`, `Permit` on rule 1, `NotApplicable` on rule 2, `Permit` from rule `deny-overrides`, `NotApplicable` on all other `n - 1` policies, and `Permit` from policy-set `deny-overrides`.
     - With `action-id = "deny"`, rule 1 is `NotApplicable`, rule 2 is `Deny`, policy `i` returns `Deny`, and policy-set `deny-overrides` returns `Deny`.
     - With an unknown `resource-id`, all `n` policies return `NotApplicable` on target mismatch (`BaseCompositeDecisionRule.java:186-190`), and policy-set `deny-overrides` returns `NotApplicable` (`DenyOverrides.java:108`).
6. **Concurrency model of `PdpDecideBenchmark` (`design.md` D8) and `SnapshotPolicyRepository` (`design.md` D10)**:
   - Checked in [DefaultPolicyDecisionPoint.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L139-L199): `decide(RequestContext)` creates a fresh per-call `EvaluationContext` through `contextFactory.createContext(...)`; the `PolicyDecisionPoint` and `RequestContext` instances are immutable during steady-state evaluation, and the `@State(Scope.Thread)` cursor has zero cross-thread sharing.
   - Checked in `design.md` D10: immutable `ImmutableMap` + `ImmutableNavigableMap` published through a single `volatile` reference with copy-on-write updates under a lock provides safe publication and lock-free reads under the Java Memory Model (JLS §17.4.5).

---

### 5. Residual Risks

1. **Multi-threaded (`-t > 1`) CAS contention on static `java.util.Random` inside `xacml-pdp`**:
   - Checked in [PolicyDecisionPointContextFactory.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java#L68) and [PolicyDecisionPointContextFactory.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java#L111) (`new Random( RANDOM.nextLong())` on every `createContext` call) and [MDCSupport.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/MDCSupport.java#L49-L56) (`Math.abs(RANDOM.nextLong())` on every `decide` call). Because `java.util.Random` uses a shared `AtomicLong` seed, running `PdpDecideBenchmark` with `-t <threads>` (`threads > 1`) will surface contention on those two static `Random` instances. This is real engine behavior (not a harness bug), and `PdpDecideBenchmark` will accurately expose it if multi-threaded runs are performed.
2. **Thread safety of user-supplied `dir:<path>` corpora containing XML `<Content>` + `<AttributeSelector>` under `-t > 1`**:
   - Checked in [XmlContent.java](xacml-core/src/main/java/org/xacml4j/v30/content/XmlContent.java#L75) and [XmlContent.java](xacml-core/src/main/java/org/xacml4j/v30/content/XmlContent.java#L228-L235): `RequestContext` with XML `<Content>` stores a W3C DOM `org.w3c.dom.Document` (Xerces DOM is not thread-safe even for concurrent XPath reads), and `XmlContent.equals` calls `content.normalizeDocument()` (which mutates the DOM). Neither `rsa2008` nor `synthetic:<n>` uses `<Content>` or `<AttributeSelector>` (checked across `xacml-conformance-tests/src/test/resources/rsa2008-interop/`), so built-in corpora are unaffected, but a custom `dir:<path>` corpus using `<Content>` with `-t > 1` could race on the shared DOM `Document`.
3. **`InMemoryPolicyRepository` executor/finalizer allocation in `PolicyStoreLoadBenchmark`**:
   - Checked in [AbstractPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java#L66-L72) and [InMemoryPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepository.java#L63-L73): `InMemoryPolicyRepository` calls the 3-arg `AbstractPolicyRepository` constructor, which allocates `Executors.newSingleThreadExecutor()` (`FinalizableDelegatedExecutorService`) per repository instance. Because no listener is registered during `PolicyStoreLoadBenchmark`, no OS thread is spawned, but GC/Cleaner overhead for the unused executor is part of `InMemoryPolicyRepository`'s construction cost.
