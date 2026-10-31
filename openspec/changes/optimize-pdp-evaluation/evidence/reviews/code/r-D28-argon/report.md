FINAL REPORT (agyx_1, Gemini 4 Argon)

### Verdict: APPROVE WITH CHANGES

D28's structure, lifecycle handling, full-response oracle, and additive `PdpFixture.open(..., pip)` overloads are clean and well-integrated with `xacml-jmh`. No thread leaks or JMH iteration hangs exist in `PipWorkload` (`executor.invokeAll` cancels unfinished virtual-thread tasks on interruption and awaits completion otherwise; `slots` always releases in `finally`; `PdpFixture` and `PdpDecideBenchmark` close the workload on every setup failure and in `tearDown` after `recheck()`).

One **P1** measurement gap in `PipCorpus` / `PipWorkload` for D27 (`forDecision()` whole-result-set memo) and D26 (per-attribute `buildParallel()`), one **P2** gap in `pipCache=on` verification, and one **P3** argument-parsing edge case should be addressed.

---

### Findings

#### [P1] `PipCorpus` and `PipWorkload` declare only single-attribute resolvers, so D28 cannot measure D27's `forDecision()` whole-result-set memo (PR11 rank 1) or map directly onto D26's per-attribute `buildParallel()`
- **File**: [`PipCorpus.java:142-146`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipCorpus.java`), [`PipCorpus.java:248-259`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipCorpus.java`), [`PipWorkload.java:125-138`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`)
- **Failure / measurement scenario**:
  1. **D27 (`PolicyInformationPoint.forDecision()`, PR11 rank 1, [`design.md:907-911`](`openspec/changes/optimize-pdp-evaluation/design.md`))**: `design.md` specifies that the decision-local whole-result-set memo is *"implemented after D28 can measure it"*. That memo avoids re-invoking a cacheable `AttributeResolverDescriptor` within one decision when evaluation demands a second distinct attribute provided by the same resolver (`ResolverCacheKey`). In [`PipWorkload.java:121-157`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`), every resolver declares only **one** attribute (`CASE`, `BULK`, `TENANT`, `ROLE`, `SHARED`, `MISSING`, `FAILURE`, `FALLBACK`, `CONTENT`). In [`PipCorpus.java:144-146`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipCorpus.java`), the three `"shared"` rules all reference the identical `AttributeDesignatorKey` (`PipCorpus.SHARED`), which is already satisfied on the second and third rules by `DefaultEvaluationContextHandler.designatorCache` ([`DefaultEvaluationContextHandler.java:206-210`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`)) without reaching `PolicyInformationPoint.resolve`. As a result, `forDecision()` has **zero hits** across every request in `pip:N`.
  2. **D26 (`AttributeResolverDescriptor.Builder.attribute(id, type, fn)` + `buildParallel()`, [`design.md:851-856`](`openspec/changes/optimize-pdp-evaluation/design.md`))**: D26 registers one `Function<ResolverContext, BagOfValues>` per attribute ID in `AttributeResolverDescriptor.Builder` (`attributesById` is keyed by attribute ID, [`AttributeResolverDescriptor.java:56`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptor.java`)). In [`PipWorkload.java:125-132`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`), `"bulk"` declares a single attribute (`PipCorpus.BULK`) with a `count`-element bag instead of `count` distinct attributes on the descriptor.
- **Smallest fix**:
  1. In `"shared"`, have the `"shared"` resolver declare two attributes (`SHARED` and `SHARED_PEER = PREFIX + "shared-peer"`), both populated from one backend read in its map, and have the third `"shared"` rule match both `SHARED` (so rules 1–3 still exercise same-key `designatorCache` reuse) and `SHARED_PEER` (so rule 3 demands a second attribute from the `"shared"` resolver, which costs a second resolver call today when `pipCache=off` and becomes a decision-memo hit under D27's `forDecision()`). Include `SHARED_PEER` in `PipCorpus.expected("shared", count)`.
  2. In `"bulk"`, either register `count` attributes `bulk(0)` .. `bulk(count - 1)` on the `"bulk"` descriptor (with `independent` matching `bulk(count - 1)` = `"v" + (count - 1)`, which maps 1-to-1 onto D26's per-attribute `attribute(id, type, fn)` + `buildParallel()` AND verifies via `PipCorpus.check` that the `count - 1` undemanded attributes in `"bulk"`'s `AttributeSet` are not published into `Result.resolvedAttributes`), or keep `BULK` and add the `count` per-attribute definitions when D26 switches `"bulk"` to `buildParallel()`.
- **Test that catches it**: In `PipCorpusTest`, assert that evaluating `"shared"` invokes the `"shared"` resolver twice before `forDecision()` (when `pipCache=off`) and once when `pipCache=on` (and once with `forDecision()` once landed), and returns both `SHARED` and `SHARED_PEER` in `Result.getResolvedAttributes()`.

---

#### [P2] `PipWorkload.verifyCache` only enforces `backendAvailable` on `"bulk"`, `"content"` (`selector`) is hardcoded `.noCache()`, and `PipCorpusTest` has no `pipCache="on"` test
- **File**: [`PipWorkload.java:108-119`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`), [`PipWorkload.java:146-147`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`), [`PipWorkload.java:207-225`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`), [`PipCorpusTest.java:49-66`](`xacml-jmh/src/test/java/org/xacml4j/jmh/corpus/PipCorpusTest.java`)
- **Failure scenario**:
  1. `readValue(String)` ([`PipWorkload.java:212-215`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`)) passes `pause(false)` (`needsBackend = false`), so `backendAvailable = false` in `verifyCache` is ignored by `"tenant"`, `"role"`, `"shared"`, and `"fallback"`.
  2. `"content"` ([`PipWorkload.java:146-147`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`)) uses `.noCache()` instead of `.cache(3600)`, so `-p request=selector -p pipCache=on` never caches the selector's entity in `CacheProvider`.
  3. Now that `policy4j-3huw` (`ResolverCacheKey.hashCode` by content) is merged in the main checkout, `PipCorpusTest` still only tests `cache = "off"` and never exercises `cache = "on"` or `workload.verifyCache(fixture)`.
- **Smallest fix**:
  - Change `readValue` to call `pause(true)`, change `"content"` to `.cache(3600)` with `pause(true)`, and in `verifyCache(PdpFixture)` check `"independent"`, `"dependent"`, `"shared"`, and `"selector"` while `backendAvailable = false`.
  - Add `cache = "on"` + `workload.verifyCache(fixture)` coverage to `PipCorpusTest`.
- **Test that catches it**: A test in `PipCorpusTest` that opens `PipWorkload` with `cache = "on"`, opens `PdpFixture`, and calls `workload.verifyCache(fixture)`.

---

#### [P3] `PipCorpus.count("pip:pip")` silently accepts `"pip:pip"` as `8`
- **File**: [`PipCorpus.java:113-116`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipCorpus.java`)
- **Failure scenario**:
  ```java
  String value = argument.startsWith(NAME + ":") ? argument.substring(NAME.length() + 1) : argument;
  if(value.equals(NAME)) value = "8";
  ```
  Because `if (value.equals(NAME))` runs after stripping `"pip:"`, passing `"pip:pip"` to `PolicyCorpusProvider.corpus("pip:pip")` or `PipCorpus.count("pip:pip")` strips `"pip:"` to `"pip"`, rewrites it to `"8"`, and returns `8` instead of throwing `IllegalArgumentException`.
- **Smallest fix**:
  Check `if (argument.equals(NAME)) return 8;` before stripping `NAME + ":"`:
  ```java
  public static int count(String argument){
      if(argument.equals(NAME)) return 8;
      String value = argument.startsWith(NAME + ":") ? argument.substring(NAME.length() + 1) : argument;
  ```
- **Test that catches it**: `assertThrows(IllegalArgumentException.class, () -> PolicyCorpusProvider.corpus("pip:pip"));` in `PipCorpusTest.corpusNameAndFanOutAreValidated`.

---

### Verification

- Inspected [`PipCorpus.java`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipCorpus.java`), [`PipWorkload.java`](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java`), [`PdpFixture.java`](`xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java`), [`PdpDecideBenchmark.java`](`xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java`), [`README.md`](`xacml-jmh/README.md`), [`PipCorpusTest.java`](`xacml-jmh/src/test/java/org/xacml4j/jmh/corpus/PipCorpusTest.java`), and [`BenchmarkSmokeTest.java`](`xacml-jmh/src/test/java/org/xacml4j/jmh/pdp/BenchmarkSmokeTest.java`) in the main checkout against [`design.md`](`openspec/changes/optimize-pdp-evaluation/design.md`), [`DefaultPolicyInformationPoint.java`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`), and [`DefaultEvaluationContextHandler.java`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) (`SUPPORTED`).
- Ran `ast-grep scan xacml-jmh`: 0 findings (`SUPPORTED`).

### Residual Risk

- Sub-millisecond `Thread.sleep(latencyMicros / 1000, nanos)` timing resolution depends on OS scheduler granularity on macOS/Linux when `0 < pipLatencyMicros < 1000`.
