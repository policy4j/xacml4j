## Context

`feature_xacml_20` holds three layers of an unfinished core rewrite:
committed HEAD `1b1e67ac` (459 files), 88 staged files, and further
unstaged edits (about 369 files differ from HEAD). None of them compiles.
The evidence below comes from read-only builds of exported snapshots and an
error-tolerant `javac` pass over the working tree, plus four independent
read-only reviews run through CAO (Codex: core design; Kimi: tests,
downstream modules, conformance; Agy: JDK 28 toolchain; Grok: adversarial
semantics review).

| State | Result |
| --- | --- |
| Working tree | 2,936 compile errors on JDK 28 (core main 682, core test 1,317, the rest downstream); 81% are unresolved symbols from renames callers never followed |
| HEAD and staged snapshot | do not compile (`Condition.java:88`, `HexBinary.java:111`) |
| `c1859f1e` | full reactor green on JDK 11 only: 616 tests, 5 skipped |
| `a8a90d46` (HEAD~1) | xacml-core green on JDK 11 (494 tests); five `xacml-xml` compile errors |
| Any commit on JDK 21 | 96 xacml-core test errors: EasyMock 5.1 cannot proxy classes |

The review found semantic regressions hidden inside mechanical renames:
designators typed as scalars, the category-URI branch of
`attribute-designator` lost, an incomplete datatype registry with two
owners of `xs:integer`, integer divide/mod changed, conditions that turn an
empty or multi-valued result into False, and XPath errors turned into `0`
or `false`. It also found pre-existing deviations: `on-permit-apply-second`
not registered by URN, `dateTime-subtract-dayTimeDuration` adding instead
of subtracting, `integer-divide` returning a double, and IEEE-754 NaN
equality. The XACML 3.0 conformance test silently never runs 141 of its 487
cases (it reuses the 2.0 loop bounds), both conformance tests skip the last
case of each group, and policy import errors are logged and ignored.

Stakeholder: the maintainer (sole owner of the uncommitted work).

## Goals / Non-Goals

**Goals:**

- A reactor that compiles and passes all unit and conformance tests, first
  on JDK 21, then on JDK 28 early access with preview.
- XACML 2.0/3.0 behavior that matches the specification in the areas the
  specs of this change cover.
- JUnit 5 + Mockito as the only test stack.
- An explicit, complete conformance manifest.
- Audited JEP 401 value classes for eligible immutable types.
- Keep every hunk of the current working tree available as donor material.

**Non-Goals:**

- New XACML features or profiles beyond what exists at `a8a90d46` plus the
  in-progress refactor (for example no new data types; `Int32`, `UInt32`,
  `UInt64`, and `JsonPath` wait until the registry is complete).
- Making `xacml-proto` a reactor module.
- Keeping JDK 8/11 compatibility.
- Performance work beyond the JMH evidence required for value-class
  conversion.

## Decisions

### D1. Rebuild from `a8a90d46`; keep the working tree as a donor (lane D approach)

Create an integration branch from `a8a90d46` and re-apply the refactor in
reviewed slices, taking code from the current working tree (the donor)
where it is correct.

- Why: the working tree mixes syntax damage, API redesign, mechanical
  renames, null stubs, and semantic regressions. Fixing compile errors in
  place would keep the regressions and produce a green-looking but wrong
  engine. `a8a90d46` compiles, its core tests pass, and it already includes
  the designator issuer fix.
- Alternatives: (a) finish the working tree in place: rejected, highest
  regression risk and roughly 30-50 engineer-days; (b) rebuild from
  `c1859f1e`: the strongest full-reactor receipt, but it would require
  replaying 22 core commits (proto, JSON, test work) with no semantic gain.
- Preservation: before any branch operation, the working tree (staged and
  unstaged) is saved without loss: a `wip/feature_xacml_20-donor` branch
  with the staged and unstaged work as separate commits, plus a patch file
  outside the repo. Nothing is reset or deleted without the maintainer's
  explicit approval.
- Landing (maintainer ruling 2026-10-24): after phase 4, the integration
  line replaces `feature_xacml_20` instead of waiting until the end. The
  landing commit also carries the feature-only agent tooling, the beads
  export, and the `add-desktop-deep-research` change. The old tip
  `a0ec4683` stays as tag `archive/feature_xacml_20-2026-10-24`.
  Phases 5-10 continue on `feature_xacml_20`. The donor rule is unchanged:
  code comes from `wip/feature_xacml_20-donor` in reviewed slices, and task
  7.3 confirms that no donor hunk is left unreviewed. A merge commit was
  rejected because it would make `1b1e67ac` an ancestor and make the 2023
  rewrite look merged although phases 5-7 re-apply it by hand.

### D2. Green slices with a fixed gate

Every slice (one reviewable unit, usually one commit series) SHALL leave the
reactor compiling and passing unit tests and the conformance manifest on
the current target JDK. A slice that cannot keep the gate green is split.

### D3. Order: baseline, tests, safety net, refactor, JDK 28, value classes

1. Baseline: repair the five `xacml-xml` errors; restore vendored fixtures;
   move compilation to release 21.
2. Test stack: migrate to JUnit 5 + Mockito on the compiling baseline.
   This must happen before the refactor, because OpenRewrite needs type
   attribution and the baseline is the only compiling code. It also
   removes the EasyMock JDK 17+ failure, making JDK 21 the working gate.
3. Conformance coverage and the semantic safety-net tests, with fixes for
   the pre-existing deviations.
4. Refactor slices (D5-D9).
5. JDK 28 with preview as a compiler and toolchain bump only.
6. Value classes, one type family per change.

Alternative: migrate tests after the refactor. Rejected: tests would be
rewritten twice and OpenRewrite could not run on a non-compiling tree.

### D4. Test migration: OpenRewrite for mechanics, manual for semantics

Run `org.openrewrite.java.testing.junit5.JUnit4to5Migration` and
`org.openrewrite.java.testing.easymock.EasyMockToMockito`
(rewrite-testing-frameworks) per module on the compiling baseline, in dry
run first. Then convert by hand what the recipes weaken:

- 127 strict mocks: `InOrder` + `verifyNoMoreInteractions` where order is
  tested.
- 512 capture sites: `ArgumentCaptor`, keeping every assertion on captured
  values.
- 305 `verify` calls: explicit `verify(...)` for every recorded expectation.
- 98 `IMocksControl` uses: per-mock lifecycle; recreate mocks in
  `@BeforeEach` instead of `reset`.
- 62 class mocks of policy model classes, 1 record mock, and 4 sealed
  `Value` mocks: real instances.

The recipe version is checked at run time. Gate: per-module test-method
count parity with the pre-migration Surefire reports.

### D5. Datatype registry is a typed facade over canonical identifiers

`Primitives` becomes a final class with typed static fields (including the
public XACML name `Integer`) backed by an immutable map from canonical
`ValueTypeId` and aliases to types, replacing the enum whose constants have
the erased type `Primitives`. Registration fails fast on duplicates. The
registry must cover every `a8a90d46` `XacmlTypes` identifier before any
new numeric type is added.

### D6. Identifier equality is canonical

Identifier records compare by canonical string only (codepoint equality,
XACML 3.0 §7.20). Aliases live only in registry indexes. This must be
settled before identifiers can become value classes, because value-object
substitutability would expose any alias-dependent equality.

### D7. Result shape comes from `ValueExprType`, not the AST node kind

`ValueType` (single value) and `BagOfType` (bag) both implement
`ValueExprType`, which is the declared result type everywhere: designators
and selectors return `valueType().bagType()`; function parameter
specifications compare full result descriptors; the AST node kind is used
only for syntactic checks. `ValueExpr` gets an explicit exactly-one
operation; the find-first operation is renamed `first()` and not used where
the specification needs a single value.

### D8. Entity and Record construction is schema-aware and issuer-preserving

`Entity.builder(EntityType)` is the canonical construction path; a
`builder(CategoryId)` convenience is allowed only if it resolves the
canonical root schema deterministically. `Record` stores attributes keyed
by attribute id and issuer (and data type), preserving bag multiplicity and
the include-in-result flag, instead of `Map<AttributeId, BagOf>`. JSON and
XML adapters change only after this API is stable.

### D9. `ValueStream` stops extending `java.util.stream.Stream`

`SeqOf` and value streams become small query views (`Iterable` plus
explicit `stream()`), following the existing `AttributeStream` pattern, so
that domain values do not impersonate single-use pipelines.

### D10. JDK 28 toolchain

- maven-compiler-plugin 3.13 or later with `<release>28</release>` and
  `<compilerArgs><arg>--enable-preview</arg></compilerArgs>`.
- Surefire 3.5 or later with `--enable-preview` in `argLine`; the Mockito
  inline mock maker attached as `-javaagent` (JEP 451 disallows dynamic
  self-attach by default); `-Dnet.bytebuddy.experimental=true` if the
  bundled ByteBuddy does not yet support class file version 72.
- Replace `org.jvnet.jaxb2.maven2:maven-jaxb2-plugin:0.15.1` with a
  maintained plugin line that keeps the `javax.xml.bind` 2.3 namespace
  (candidate: `org.jvnet.jaxb:jaxb-maven-plugin` 2.x).
- license-maven-plugin upgrade; drop the `oss-parent:7` parent (2012
  plugin pins, HTTP repositories); Maven 3.9 or later.
- CI downloads the same JDK 28 build developers use and checks its build
  identity. The local build is a custom one (`IMPLEMENTOR=Artagon`, build
  `28-ea-jdk-fd9698357bb0`), so a generic `setup-java` early-access JDK is
  not assumed equivalent.

All versions are verified against the pinned JDK at the start of the JDK 28
phase; locally cached versions are not proof of compatibility.

### D11. Value-class candidates and exclusions

First candidates: simple immutable leaves (`BooleanVal`, `Int64`, then the
other numeric, string, and binary values and identifier records). Excluded
until proven eligible: `Entity`, `Record`, `BagOf`, registries, factories,
builders, and evaluation contexts. Each candidate needs an identity audit
(reference `==`, `synchronized`, `identityHashCode`, identity maps, weak
references, Guava interners, reflective mutation by Gson or JAXB) and JMH
before/after results. Under JEP 401, `synchronized` and weak references on
value objects throw `IdentityException`; `==` compares state.

### D12. A data type that is known only at evaluation is the dynamic type

Added 2026-10-09 (bead `policy4j-the.4.61`, evidence run 086).

The bag of the `attribute-designator` and `attribute-selector` functions has
the data type that their third argument names, and that argument can be an
expression (Related and Nested Entities Profile v1.0, section 6.1). Such an
expression has no data type when the policy is loaded.

- `DynamicType` is one more `ValueType`, a singleton: "a data type that is
  known only at evaluation". The shape of D7 stays: a single value is
  `DynamicType.INSTANCE`, a bag is its bag type. It is the form that cel-java
  (`SimpleType.DYN`) and Trino (`UnknownType.UNKNOWN`) use: an instance of
  the ordinary type abstraction, not a null, an `Optional`, or a second
  hierarchy. It is not in the type registry, and no value has it.
- Checks follow gradual typing (Siek and Taha 2006: a consistency relation
  in place of type equality, and a cast where the type of an argument is
  consistent with the parameter type and not equal to it), as Saxon
  (`TypeChecker.staticTypeCheck`) and AuthzForce
  (`BaseFirstOrderFunctionCall`) apply it. When the expression is built, the
  dynamic type is *consistent* with every data type of the same shape; all
  that is known is checked and rejected as before. `Apply` puts a
  `TypeAssertion` around every argument of dynamic type: its value is
  checked against the parameter when it is evaluated.
- An operand that is not evaluated is not checked (XPath 3.1, sections 2.3.3
  and 3.1.5.1; CEL, logical operators; ruling D11 of this change, run 074).
- The sources, with the quoted passages and the checksums of the fetched
  files, are in `evidence/tests/086-dynamic-type/research.md`.
- Rejected alternatives: no dynamic type, so the expression is a type error
  (run 083, rule 2; it forbids a form the profile allows); a check of every
  call at evaluation (cost on the decision path for all calls); `Optional`
  or a sealed hierarchy for the type (about 60 call sites; task 6.1 reworks
  these interfaces).

Task 6.1 carries `DynamicType` over to `ValueExprType`.

### D13. Generic bounds of the value-type API

Added 2026-10-24 (task 5.6, bead `policy4j-the.5.6`, evidence runs 087 and
rv098).

`Value<T>` is a `Supplier<T>`, so its `T` is the payload type. A bound
`<T extends Value<T>>` accepts only self-valued types (`RFC822Name` is a
`SelfValue<RFC822Name>`) and rejects values that wrap a payload
(`BooleanVal` is a `Value<Boolean>`), so the typed call sites did not
compile; raw types hid this. `Value<?>` accepts both kinds.

- `ValueType.ofAny` and `DynamicType.ofAny` take `<T extends Value<?>>`;
  `ValueType` is a `Function<Object, Value<?>>`.
- The capability family is self-typed: `forType`, `discoverCapabilities`,
  `AbstractCapabilityFactory`, and `TypeCapabilityFactory` take
  `T extends TypeCapability<T>`; `TypeToString`, `TypeToGSon`, and
  `TypeToXacml30` take and return `Value<?>`.
- Compatibility: the erasures do not change, so compiled callers keep
  working. Some source callers need a change: `ValueType` was a
  `Function<Object, Value>` with a raw `Value`, and generic arguments are
  invariant, so
  `Function<Object, Value> f = XacmlTypes.STRING;` no longer compiles;
  write `Function<Object, Value<?>>`. Every call site in the reactor
  compiles. Behavior does not change (gate run 087: 8,009 tests, 0
  failures; conformance unchanged).
- Rejected: keeping raw types (they hide wrong pairings of a type and its
  capability), and waiting for task 5.3 (the facade of D5 builds on these
  signatures).

## Risks / Trade-offs

- [Re-applying the refactor loses useful donor work] → Keep the donor branch
  and patch; review each slice against the donor diff for its files.
- [Silent test weakening during migration] → Manual conversion rules in D4,
  per-module test-count parity, reviewer sign-off per converted file.
- [Semantic regressions not covered by unit tests] → Safety-net tests from
  the adversarial review land before the refactor; conformance manifest
  must not lose executed cases.
- [JDK 28 early-access and plugin drift] → Pin the JDK build; JDK 28 bump
  is its own phase with no value classes; verify plugin and Mockito/ByteBuddy
  support on that build before switching.
- [Preview class files run only on JDK 28 with `--enable-preview`] → Document
  it as a breaking change for consumers.
- [Tooling blind spots for `value class` syntax] (tree-sitter-based
  ast-grep/ripwire, JDTLS in Serena) → Label those results PARTIAL; rely on
  compiler and tests for converted types.
- [Staged and unstaged edits interleave on the same files] → Donor branch
  keeps them as separate commits.
- [Effort] → Reviews estimate roughly 32-55 engineer-days in total; the
  largest variance is JAXB/Gson/preview plugin compatibility.

## Migration Plan

1. Preserve the donor (branch + patch) with the maintainer's approval.
2. Create the integration branch from `a8a90d46`; land the baseline,
   test-stack, conformance, and safety-net phases.
3. After phase 4, land the integration line by moving `feature_xacml_20`
   to it (D1, task 10.1). Keep the old tip as tag
   `archive/feature_xacml_20-2026-10-24` and keep
   `wip/feature_xacml_20-donor` until task 7.3 confirms that no donor hunk
   is left unreviewed.
4. Land refactor slices on `feature_xacml_20`, each green.
5. Land the JDK 28 bump, then value-class families on `feature_xacml_20`.

Rollback: every phase is a separate commit series, on the integration
line through phase 4 and on `feature_xacml_20` after the landing; the old
tip stays in `archive/feature_xacml_20-2026-10-24`, the donor branch stays
until task 7.3, and `a8a90d46` stays untouched.

## Open Questions

- Resolved 2026-10-24 by D1: the integration line replaces
  `feature_xacml_20` after phase 4; no merge commit.
- Should `integer-divide` change from `double` to `xs:integer` results as
  the specification requires, given any external callers of the current
  behavior?
- The `entity` data type URN and the `attribute-designator` function come
  from the XACML 3.0 entity profile work; which identifiers are canonical?
- Which pre-baseline fixture edits (`IIIF002Request.xml`,
  `IIIF007Policy.xml`, `IIIG006Policy.xml`, `Example_2/Rule_1-3.xml`) are
  intentional? They are restored unless the maintainer confirms them.
- Exact CI source for the custom JDK 28 build (artifact URL and checksum).
