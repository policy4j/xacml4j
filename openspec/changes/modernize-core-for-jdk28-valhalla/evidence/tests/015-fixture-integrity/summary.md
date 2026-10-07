# Vendored fixture integrity baseline

## Result

The JUnit 5 integrity check passes against **2,911** recorded Git blobs: 2,906 files under `xacml-conformance-tests/src/test/resources/` and 5 under `xacml-xml-jaxb/src/main/resources/xsd/`. It computes SHA-1 over the Git blob header (`blob <byte-length>\0`) plus raw file bytes; no line-ending normalization or Git executable is used by the test. The manifest is `xacml-conformance-tests/src/test/integrity/vendored-fixtures.sha1`.

The JDK 21 gate passed: `VendoredFixtureIntegrityTest` ran 1 test, 0 failures, 0 errors, 0 skips. The full `-pl xacml-conformance-tests -am test` run completed with reactor SUCCESS. Its actual per-case output records OASIS 2.0 **347**, OASIS 3.0 **347**, RSA 2008 **17**, and examples **2**. Surefire reports 10 JUnit methods for each OASIS runner because those methods execute the cases in loops; the per-case counts come from the runner's `Executing test=` output lines. Maven 3.9.16 ran on Java 21.0.6.

## Baseline cross-checks

- The 17 restored fixture paths listed in `evidence/builds/004-vendored-fixtures/summary.md` match the generated manifest IDs: **17/17**, no mismatches.
- Ten sample manifest IDs were independently checked with `rtk git hash-object`: **10/10** matched.
- The generated manifest has 2,911 unique rows. The two current vendored sets contain no symlinks; the implementation nevertheless encodes Git symlink-target bytes if one is introduced.

| Sample path | Manifest ID | `rtk git hash-object` |
|---|---|---|
| `xacml-conformance-tests/src/test/resources/Example_2/record.xsd` | `e960681d8cdbded7093fbcf074a450286e0c82ac` | `e960681d8cdbded7093fbcf074a450286e0c82ac` |
| `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test/ConformanceTests.html` | `bfdefa095574cd2c0760b7a6a07024f2ed5e24e1` | `bfdefa095574cd2c0760b7a6a07024f2ed5e24e1` |
| `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test/IIA002Special.txt` | `d9d28ca63d482bb5c131bad570055a55e0f64212` | `d9d28ca63d482bb5c131bad570055a55e0f64212` |
| `xacml-conformance-tests/src/test/resources/rsa2008-interop/XacmlPolicySet-04-N-PPS-PRD-004.xml` | `b7dc431d1c35baf402e6a9cb4c03e32cc43d4d04` | `b7dc431d1c35baf402e6a9cb4c03e32cc43d4d04` |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/ConformanceTests.html` | `302245391870083ab47ecdade505230f29cdaffc` | `302245391870083ab47ecdade505230f29cdaffc` |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIA023Policy.xml` | `18f5f613f5762882aec8b3c1b72b6d6d18c18a3f` | `18f5f613f5762882aec8b3c1b72b6d6d18c18a3f` |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIIA301Policy.xml` | `287263cd3bcf557a1b562f5b312e11fd3181c614` | `287263cd3bcf557a1b562f5b312e11fd3181c614` |
| `xacml-xml-jaxb/src/main/resources/xsd/xml.xsd` | `20555e269f17b87a41cb360cf8bc225a5debede3` | `20555e269f17b87a41cb360cf8bc225a5debede3` |
| `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test/IIIF002Request.xml` | `73b9fc0614e84f05092292fec1a11134c8acaa72` | `73b9fc0614e84f05092292fec1a11134c8acaa72` |
| `xacml-conformance-tests/src/test/resources/Example_2/Rule_3.xml` | `131ac04b8a99545e1e4a6d80fb777bac08bdf165` | `131ac04b8a99545e1e4a6d80fb777bac08bdf165` |

## Failure proof and fixture preservation

The bite test copied both fixture trees to a temporary directory, changed the first byte of the copy of `Example_2/record.xsd`, and ran the integrity test with `-Dvendored.fixture.root=<scratch-root>`. The test failed with:

```text
CHANGED xacml-conformance-tests/src/test/resources/Example_2/record.xsd expected=e960681d8cdbded7093fbcf074a450286e0c82ac actual=5518169f6a11b6cb4e57a7cd74ddf7e159e5403a
```

The scratch copy was deleted after the probe. Scoped `rtk git status --short` and `rtk git diff --name-only` show the same six pre-existing task 1.5 restoration paths seen in the initial worktree status: `Example_2/Rule_1.xml`, `Rule_2.xml`, `Rule_3.xml`, `oasis-xacml20-compat-test/IIIF002Request.xml`, `IIIF007Policy.xml`, and `IIIG006Policy.xml`. No other vendored path is listed; all six match the 004 recorded blob IDs. This task did not edit any vendored fixture.

## Maintainer refresh command

After a deliberate standards-fixture update, compile test classes and run `org.xacml4j.v20.GenerateVendoredFixtureManifest --write` using the command in `command.txt`, review the manifest diff, and rerun the integrity and conformance gates.

## Documentation pointer

The lane left docs/ai/TESTING.md untouched because another lane was editing it. The conductor added the pointer afterwards (TESTING.md, Writing tests, fixture bullet: VendoredFixtureIntegrityTest and GenerateVendoredFixtureManifest --write).
