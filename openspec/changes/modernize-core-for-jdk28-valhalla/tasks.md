Beads: epic `policy4j-the`; task X.Y is issue `policy4j-the.X.Y` (dependencies
recorded there; `br ready` shows the next task). Claim with
`br update <id> --status=in_progress`, close with `br close <id>` when the
checkbox below is ticked.

Gate for every task that changes code, unless the task says otherwise:
`JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true verify`
passes (JDK 28 command from group 8 on), `rtk git status` shows no license
header churn, and the conformance manifest (group 3 on) loses no executed
case.

## 1. Preserve the donor and establish the baseline

- [x] 1.1 With the maintainer's approval, save the current working tree without loss: branch `wip/feature_xacml_20-donor` with the staged and unstaged work as two separate commits, plus `git diff HEAD` and `git diff --cached` patch files stored outside the repo; verify the branch diff reproduces the working tree byte for byte
- [x] 1.2 With the maintainer's approval, create the integration branch from `a8a90d46`
- [ ] 1.3 Repair the five `xacml-xml` compile errors (`Xacml20RequestContextUnmarshaller.java:55`, `Xacml20PolicyFromJaxbToObjectModelMapper.java:98`/`:702`, `TypeToXacml30.java:44`) against the `a8a90d46` core API; gate: full reactor compiles and tests pass on JDK 11
- [ ] 1.4 Restore the vendored fixture and schema files damaged after `a8a90d46` (Example_2 `record.xsd`, both `ConformanceTests.html` files, `IIA002Special.txt`, `XacmlPolicySet-04-N-PPS-PRD-004.xml`, `IIA023Policy.xml`, `IIIA301Policy.xml`, `IIIA340Policy.xml`, `IIIG002Policy.xml`, `IIIG002dPolicy.xml`, `xml.xsd`) with `git restore --source=<pre-damage ref>` per path; keep the `policy.json` deletion
- [ ] 1.5 Review the pre-baseline fixture edits (`IIIF002Request.xml`, `IIIF007Policy.xml`, `IIIG006Policy.xml`, `Example_2/Rule_1-3.xml`) against upstream and restore each one the maintainer does not confirm
- [ ] 1.6 Set the compiler to release 21 for the whole reactor (no preview) and record per-module Surefire test counts on JDK 11 and JDK 21 as the migration baseline (JDK 21 expected to show only the 96 EasyMock class-mock errors)

## 2. Migrate tests to JUnit 5 and Mockito (on the baseline)

- [ ] 2.1 Add `junit-jupiter` and `mockito-core`/`mockito-junit-jupiter` (test scope) to the root pom next to the existing stack; confirm Surefire runs both engines during the transition
- [ ] 2.2 Dry-run and then run the OpenRewrite `JUnit4to5Migration` and `EasyMockToMockito` recipes on xacml-core; review the diff
- [ ] 2.3 Convert xacml-core strict mocks, captures, `verify` calls, and `IMocksControl` usage by hand per design D4; replace class mocks of `Match`, `MatchAnyOf`, `MatchAllOf`, `Target`, `Condition`, `Policy`, `PolicySet`, `Rule`, `AttributeAssignmentExpression`, the `AttributeDesignator` record, and sealed `Value` with real instances; gate: xacml-core method count equals the baseline
- [ ] 2.4 Repeat 2.2-2.3 for xacml-pdp
- [ ] 2.5 Migrate `xacml-test` support classes (`XacmlPolicyTestSupport`, `AttributeResolverTestSupport`, `Xacml20TestUtility`, `Xacml30TestUtility`) to Jupiter and make `junit-jupiter-api` a compile-scope dependency of `xacml-test`
- [ ] 2.6 Migrate xacml-xml, xacml-json, and conformance tests (including `RSA2008PerformanceTestCase`, which stays excluded by an explicit Surefire rule with a comment)
- [ ] 2.7 Remove `junit:junit`, `junit-vintage-engine`, EasyMock, the `easymock.version` property, and Objenesis; gate: no JUnit 4 or EasyMock import anywhere, dependency tree clean, per-module method counts at or above baseline, all tests green on JDK 21
- [ ] 2.8 Update the AGENTS.md test section (JUnit 5 + Mockito, mocking rules)

## 3. Make conformance coverage complete and explicit

- [ ] 3.1 Create the conformance manifest listing every case of the 2.0 suite (374 request ids), the 3.0 suite (487), RSA 2008 (17), and the examples, each with disposition `executed`, `special-harness`, or `skipped` plus reason
- [ ] 3.2 Replace hard-coded loop bounds in `Xacml20ConformanceTest` and `Xacml30ConformanceTest` with manifest-driven enumeration; fix the off-by-one that never runs the last case of a group; use the XACML 3.0 media type for 3.0 root policies
- [ ] 3.3 Make policy import failures fail the test instead of being logged
- [ ] 3.4 Write a reason for each of the 21 inherited skips per suite, or execute the case; mark IID029/IID030 as `special-harness` and implement the multi-root-policy harness or record why not
- [ ] 3.5 Add the vendored-fixture integrity check (files compared with the recorded baseline blobs)
- [ ] 3.6 Gate: every `executed` case passes on JDK 21; record the executed-case count as the manifest baseline

## 4. Add the semantic safety net and fix pre-existing deviations

- [ ] 4.1 Add attribute-resolution tests: designator result type is a bag; MustBePresent true/false × missing × resolver failure with status codes; issuer filtering; `attribute-designator` with a category URI and with an entity
- [ ] 4.2 Add condition tests: absent, true, false, Indeterminate, empty result, multi-valued result, non-boolean declared type
- [ ] 4.3 Add function tests: `integer-divide` 5/2, `integer-mod` -5/3, divide by zero, three-argument multiply, NaN equality, union/intersection duplicates, one-and-only with 0 and 2 values, mixed-type set arguments, XPath error cases, `dateTime-subtract-dayTimeDuration` with positive and negative durations
- [ ] 4.4 Add registry tests: every `a8a90d46` data type id and alias resolves and round-trips; duplicate ids fail; every combining algorithm class resolves by URN (including `on-permit-apply-second`); `ServiceLoader` finds the default providers
- [ ] 4.5 Fix `dateTime-subtract-*` sign handling
- [ ] 4.6 Fix `integer-divide` to return a truncated `xs:integer`, NaN equality, and `on-permit-apply-second` registration (each as its own commit, after the open question on `integer-divide` callers is answered)
- [ ] 4.7 Gate: all safety-net tests pass on JDK 21; conformance manifest unchanged or improved

## 5. Re-apply the refactor: identifiers, values, and the datatype registry

- [ ] 5.1 Introduce the `identifier` package from the donor with canonical-only equality and hashing; keep adapters for the old root `CategoryId`, `StatusCodeId`, `Issuer`, and `UrnIdentifier` types; add symmetry, transitivity, hash-map, and case tests
- [ ] 5.2 Re-apply the `Value`/`BagOf` rename (`BagOfValues` → `BagOf`, `ValueExp` → `ValueExpr`, `AnyUri` → `UriVal`, `LongVal` → `Int64`, `ISO8601Duration` → `Duration`) with bag result types and bag multiplicity preserved
- [ ] 5.3 Replace `XacmlTypes`/`ValueTypes`/`ValueTypeBuilder` with the typed `Primitives` facade and `ValueTypeId` registry (design D5) covering every `a8a90d46` type id; fail fast on duplicates
- [ ] 5.4 Fix numeric semantics from the donor before adopting it: one owner of `xs:integer`; non-wrapping arithmetic; strict integer lexing; product identity 1; no null stubs
- [ ] 5.5 Gate: full reactor and conformance green on JDK 21

## 6. Re-apply the refactor: expressions, functions, algorithms, and attribute resolution

- [ ] 6.1 Introduce `ValueExprType` as the declared result type everywhere, `requireExactlyOne()` for single values, and `first()` only where the specification allows it (design D7); update function parameter specifications and return-type resolvers
- [ ] 6.2 Re-apply the function annotation renames (`@FuncSpec`, `@FuncParam*`, `@PolicyFuncProvider`, ...) using the registry
- [ ] 6.3 Re-apply the combining algorithm class renames (`*CombiningAlgorithm` → `*Algorithm`); gate: URN set equals the `a8a90d46` set plus `on-permit-apply-second`
- [ ] 6.4 Rework `ValueStream`/`SeqOf` into non-`Stream` query views (design D9)
- [ ] 6.5 Implement schema-aware, issuer-preserving `Entity.builder(EntityType)` and `Record` (design D8), then re-apply designator/selector changes with the MustBePresent and status behavior from the spec; update JSON and XML adapters
- [ ] 6.6 Re-apply date/time/duration type changes (`Duration`, day-time and year-month types) against the group 4 tests
- [ ] 6.7 Gate: full reactor and conformance green on JDK 21

## 7. Re-apply the refactor: model, schemas, and cleanup

- [ ] 7.1 Re-apply `model` (`AccessModel`, `AttributesSchema`, `EntitySchema`, `EntityTypeQualifier`) with the donor defects fixed (the `AttributesSchema.java:101` construct, `EntitySchema` recursion, required-domain validation, `Entity` equals/hashCode)
- [ ] 7.2 Re-apply `schemas` with one canonical catalog of standard categories (subject 1.0 URIs, resource/action/environment 3.0 URIs) and standard attributes checked against XACML 3.0 Appendix B
- [ ] 7.3 Remove compatibility adapters and old types once scoped searches show no callers; confirm no donor hunk is left unreviewed
- [ ] 7.4 Gate: full reactor and conformance green on JDK 21; record per-module test counts

## 8. Move the build to JDK 28 with preview (no value classes)

- [ ] 8.1 Verify on the pinned JDK 28 build (`28-ea-jdk-fd9698357bb0`): maven-compiler-plugin, Surefire, JUnit 5, Mockito/ByteBuddy (class file 72), the JAXB plugin, license-maven-plugin, auto-service; record the versions that work
- [ ] 8.2 Drop the `oss-parent:7` parent; upgrade the plugins; replace `maven-jaxb2-plugin` 0.15.1 with the chosen `javax.xml.bind` 2.3 compatible plugin; require Maven 3.9 or later
- [ ] 8.3 Set `<release>28</release>` and `--enable-preview` for compilation; add `--enable-preview` and the Mockito `-javaagent` (plus `-Dnet.bytebuddy.experimental=true` if 8.1 requires it) to Surefire `argLine`
- [ ] 8.4 Gate: full reactor and conformance green on JDK 28 with preview; no `value class` or `value record` in any source file
- [ ] 8.5 Replace the JDK 8 CI workflow with a JDK 28 job that downloads the pinned build, verifies its build identity, and runs the gate
- [ ] 8.6 Update AGENTS.md build section (JDK 28, preview, tooling limits for value classes)

## 9. Convert audited types to JEP 401 value classes

- [ ] 9.1 Audit value-class candidates for identity use (reference `==`/`!=`, `synchronized`, `identityHashCode`, identity maps, weak references and Guava interners, Gson/JAXB reflective mutation); record results per type; fix blocking code
- [ ] 9.2 Add JMH benchmarks for the hot paths of the first family (PDP decide on conformance policies, function invocation, value parsing) and record identity-class baselines on JDK 28
- [ ] 9.3 Convert `BooleanVal` and `Int64` to value records; add equality, serialization (JSON/XML), and bag tests; gate plus JMH before/after results
- [ ] 9.4 Convert the remaining eligible families one change at a time (numeric, string/binary values, identifier records), each with its audit, tests, and JMH results
- [ ] 9.5 Confirm `Entity`, `Record`, `BagOf`, registries, builders, and contexts remain identity classes or record the audit that proves them eligible

## 10. Close out

- [ ] 10.1 Decide with the maintainer how the integration branch lands (replace or merge into `feature_xacml_20`) and confirm the donor branch has no unique work left
- [ ] 10.2 Run `openspec validate modernize-core-for-jdk28-valhalla --strict`, sync the specs, and archive the change
- [ ] 10.3 Close the beads epic and record the final per-module test counts and conformance manifest totals
