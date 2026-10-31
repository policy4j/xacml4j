# Duplicate registry identifiers (4.26)

## Result

Datatype and function registries now reject identifier claims by distinct
objects, including primary/legacy/short-id collisions. Exceptions name the
colliding identifier and both claimants. Re-registering the same datatype,
provider instance, or `FunctionSpec` object is idempotent.

`TypeFactory.BaseTypeFactory` registers canonical IDs, short IDs, and aliases
into case-insensitive maps and validates claims before exposing the maps
(`TypeFactory.java:50-79`). `BaseFunctionProvider` validates canonical,
legacy, and short function IDs both while providers are combined and while its
lookup maps are built (`BaseFunctionProvider.java:48-91,95-137`). Provider and
spec identity is used for idempotency. `FunctionProviderBuilder` retains the
default provider instance, deduplicates repeated provider instances, and
filters a discovered default when the same builder already has its default
(`FunctionProviderBuilder.java:98-139,174-184`).

The existing disabled registry tests are enabled and assert the new behavior.
Added coverage checks primary function conflicts, legacy alias versus primary
ID conflicts, same-provider and same-spec idempotency, and default plus
explicit/discovered construction (`TypeRegistryTest.java:151-184`,
`FunctionProviderDuplicateIdTest.java:45-119`). Existing test fixtures that
explicitly re-added standard function classes now use the already-registered
standard specs (`ModelFixtures.java:48-50`, `XPathFunctionsTest.java:98-102`).

## Red then green

Before production changes, the focused JDK 21 run reached the tests and failed
with 5 failures, 0 errors: duplicate datatype registration, duplicate
function registration, primary function ID conflict, legacy-alias/primary-ID
conflict, and a default-plus-explicit test setup that was using the
non-defaulting `FunctionProvider.builder()` entry point. The latter setup was
corrected to use `FunctionProviderBuilder.builder()`; production behavior was
not weakened. A preceding attempt had a test-only `FunctionSpecBuilder`
package import typo and failed compilation; that was corrected before the
behavioral red run.

The focused green run passed 69 tests with 0 failures, 0 errors, and 0 skips.
The full JDK 21 `clean verify` passed with exit status 0.

## Construction-path audit

Search: `rtk rg -n "FunctionProviderBuilder\\.builder|FunctionProvider\\.builder|new AggregatingFunctionProvider|extends BaseFunctionProvider|new TypeFactory\\.BaseTypeFactory|TypeFactory\\.BaseTypeFactory" --glob '*.java' xacml-core/src/main xacml-pdp/src/main xacml-xml/src/main xacml-json/src/main`.

Production function-provider construction sites found:

- `xacml-xml/.../XacmlPolicyUnmarshaller.java:67` discovers providers and
  then requests defaults; the builder adopts the discovered default instance.
- `xacml-xml/.../Xacml30PolicyUnmarshaller.java:50`,
  `xacml-core/.../MarshallingProvider.java:47`, and
  `xacml-pdp/.../PolicyRepository.java:204` request defaults directly.
- `FunctionProviderBuilder.java:205` builds the aggregate;
  `XacmlDefaultFunctions` and `AnnotationBasedFunctionProvider` construct
  their registries through `BaseFunctionProvider`.
- `XacmlTypes.SystemTypes` is the production `TypeFactory.BaseTypeFactory`
  construction path (`XacmlTypes.java:248`).

All these paths initialized successfully in the full clean reactor run. The
initial full-run failure exposed test setup code that intentionally supplied
an already-standard function class on top of defaults; those fixtures now use
the standard provider directly.

## Conformance

All conformance tests passed in the full clean reactor run. The run recorded
368 OASIS 2.0, 484 OASIS 3.0, 17 RSA 2008, and 2 example `CONFORMANCE PASS`
receipts. Surefire reported OASIS 2.0: 375 tests / 10 skipped; OASIS 3.0: 488
tests / 7 skipped; RSA 2008: 17 / 0 skipped; examples: 2 / 0 skipped. The
OASIS receipt counts differ from the earlier 347/347 green-gate baseline.
Task 4.26 did not change the manifest or fixtures, so the cause of this
difference is unresolved and should be reconciled by the conductor. The
receipt totals here are raw `CONFORMANCE PASS` log lines, not a claim that
each line maps one to one to a Surefire test method.

## Verification

JDK: Maven 3.9.16 running on GraalVM 21.0.6. Both Maven commands passed
`-Dlicense.skipUpdateLicense=true`. The full raw log is
`full-reactor-clean-verify.log`; focused green run log is
`type-function-duplicates-green.log`. The behavioral red run was captured in
the tool session output, but the requested log path did not exist at the time
of that first run, so no separate raw red log was retained.
