# D49 StringVal equality benchmark

Lane `cod_1`, task 42.18, bead `policy4j-cwtu.42.18`; base
`759d90a6c75510d55127d8e22870f9edd985c991`.

Added `xacml-jmh/src/main/java/org/xacml4j/jmh/value/StringValEqualsBenchmark.java`
(SHA-256 `12eaa29c276140b7f50985092f191d15fe2e84397b29a625a2101583caa633f8`).
It uses `StringVal.of` and covers lengths 16, 32 and 57 crossed with the
identical-instance, equal-distinct, first-character mismatch and last-character
mismatch cases. Both benchmark methods return `StringVal.equals` and read
non-final state. Trial setup precomputes the underlying string hashes for
`cachedHash`. It also checks the cached inputs' identity and expected equality.

`firstUse` constructs fresh strings from character arrays in invocation setup;
the generated average-time harness places this setup before its per-invocation
start timestamp, outside the reported method time. The separate timestamp per
operation, allocation effects and possible GC still perturb this very short
operation. JMH 1.37's `Level.Invocation` documentation says this mode is for
operations taking more than a millisecond; therefore these first-use results
are exploratory and require validation before they can support a timing claim.
They are not directly comparable to the normally timed `cachedHash` results.

Verification used the shared runner from the packet, with `${WORKTREE}` set to
the `cod_1` checkout and `${PACKET}` to the packet directory:

```sh
rtk proxy python3 ${PACKET}/run-build.py --worktree ${WORKTREE} \
  --log ${WORKTREE}/openspec/changes/optimize-pdp-evaluation/evidence/tests/208-d49-benchmark-terra/01-package.txt \
  --maven /opt/homebrew/bin/mvn -- -o -Dlicense.skipUpdateLicense=true \
  -pl xacml-jmh -am -DskipTests clean package
```

The seven-module offline reactor packaged successfully; tests were skipped as
requested. The class compiled against this checkout's pre-D49 `StringVal`;
the D49 version was not separately compiled, and the benchmark uses only the
unchanged factory and equality API. The shaded jar's `META-INF/BenchmarkList`
was inspected and contains both benchmark entries (`02-jar-metadata.txt`).
The jar's `-l` command was not run: the shared runner invokes Maven, not the
jar directly. No JMH run was performed, and no performance conclusion is
claimed.

The three protected D47 source/test files retain their incoming SHA-256 values.
