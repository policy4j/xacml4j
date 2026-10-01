## Why

Branch `feature_xacml_20` carries an unfinished rewrite of the core type
system that cannot build: the working tree has 2,936 compile errors on JDK
28 (2,943 on JDK 21), and the committed HEAD and the staged snapshot do not
compile either. The last fully green commit (`c1859f1e`, 616 tests) is green
only on JDK 11, and 96 tests fail on JDK 17+ because EasyMock cannot proxy
classes there. Read-only review of the rewrite found XACML semantic
regressions mixed into mechanical renames (designators typed as scalars, a
lost category-URI resolution path, changed integer divide/mod, conditions
that turn errors into False). "Fix the compile errors, then run the tests"
would produce a green-looking but semantically wrong engine. The 3.0
conformance harness also silently skips 169 of 861 OASIS cases.

The project is moving to JDK 28 early access with Valhalla (JEP 401 value
classes). That move is only safe from a green, spec-correct baseline with a
test stack that runs on modern JDKs.

## What Changes

- Preserve the current working tree as a donor (nothing is discarded), and
  build on a new integration line from `a8a90d46` (xacml-core green, 494
  tests on JDK 11). Repair its five `xacml-xml` compile errors first so the
  whole reactor is green on JDK 21.
- Restore vendored OASIS/W3C fixtures and schemas damaged by a bulk
  find-and-replace (11 files) and a committed `record.xsd` break.
- Migrate every test from JUnit 4 to JUnit 5 (Jupiter) and from EasyMock to
  Mockito, on the green baseline. Remove `junit-vintage-engine`, EasyMock,
  and Objenesis. Replace class mocks of model/value objects with real
  instances.
- Make conformance coverage complete and explicit: a manifest of every OASIS
  2.0/3.0 and RSA case with its disposition, corrected loop bounds and the
  off-by-one, fail-fast policy import, and a written reason for every skip.
- Add XACML semantic safety-net tests, and fix the spec deviations they
  expose, before any type-system work.
- Re-apply the type-system refactor from the donor tree in ordered slices
  (identifiers and values, datatype registry, function annotations,
  combining algorithms, expression result shape and condition cardinality,
  entity/record construction and attribute resolution, date/time), each
  slice keeping the reactor and conformance green on JDK 21.
- Move the build to JDK 28 early access with `--enable-preview` as a plain
  compiler and toolchain bump (no value classes), including the CI job.
- Convert audited immutable types to JEP 401 value classes/records one type
  family at a time, after an identity-sensitivity audit.
- **BREAKING**: public API renames from the refactor (for example
  `Expression` → `Expr`, `*CombiningAlgorithm` → `*Algorithm`, `BagOfValues`
  → `BagOf`, new `identifier` package).
- **BREAKING**: `xacml-test` support classes expose JUnit 5 (Jupiter)
  instead of JUnit 4.
- **BREAKING**: artifacts compiled with `--enable-preview` for JDK 28 run
  only on a JDK 28 runtime started with `--enable-preview`.

## Capabilities

### New Capabilities

- `attribute-resolution`: attribute designator and selector result shape,
  category-URI resolution, issuer matching, and MustBePresent status
  behavior.
- `datatype-registry`: complete, unique registration of every XACML data
  type and alias; canonical identifier equality.
- `function-semantics`: integer arithmetic, set and bag functions, double
  equality, XPath function error handling, and date/time-duration
  arithmetic.
- `condition-evaluation`: scalar cardinality of condition and single-value
  results.
- `combining-algorithm-registry`: resolution of every combining algorithm
  by URN through the provider.
- `conformance-coverage`: the conformance case manifest, skip policy, and
  vendored-fixture integrity.
- `test-framework`: JUnit 5 + Mockito as the only test stack, mocking rules,
  and test-discovery parity.
- `jdk28-valhalla-build`: JDK 28 preview build and CI, and the rules for
  converting types to value classes.

### Modified Capabilities

None (no specs exist in `openspec/specs/` yet).

## Impact

- **Code**: all modules. xacml-core (types, identifier, model, schemas,
  policy, functions, combining algorithms), xacml-xml, xacml-json,
  xacml-pdp, xacml-test (support API), xacml-conformance-tests (harness).
  `xacml-proto` stays out of the reactor.
- **Dependencies**: remove junit-vintage-engine, EasyMock, Objenesis; add
  junit-jupiter and mockito-core/mockito-junit-jupiter; upgrade
  maven-compiler-plugin, maven-surefire-plugin, license-maven-plugin;
  replace org.jvnet.jaxb2 maven-jaxb2-plugin 0.15.1 with a maintained
  plugin that keeps the `javax.xml.bind` 2.3 namespace; drop the
  `oss-parent:7` parent.
- **Build and CI**: JDK 21 during stabilization, then JDK 28 early access
  with preview for compile and test; CI moves from JDK 8 to the pinned JDK
  28 build.
- **Tooling**: ast-grep and ripwire (tree-sitter) and Serena (JDTLS) cannot
  fully parse `value class` syntax; their results on converted types are
  partial.
- **Git**: preserving the donor tree and creating the integration line are
  git writes that need the maintainer's explicit approval.
- **Docs**: AGENTS.md test and build sections.
- **Tracking**: beads epic `policy4j-the`; phase sub-epics `policy4j-the.1` to
  `policy4j-the.10`; one issue per task, numbered like `tasks.md`
  (task 2.3 is `policy4j-the.2.3`).
