<!-- Re-check RVH2 of cycle H round 2, Codex GPT-6.1 Sol, read-only; report from the NTM pane. -->

FINAL REPORT (RVH2 code reviewer, GPT-6 / Codex)

Reviewed round 2 against HEAD `9855935affb0f479c5102c410e627acb7f30e6b4`. Production code, characterization tests and the lint rule remained unchanged during review.

RVH1 dispositions:

1. **Resolved — custom-type retention.** `BagOfValues` is back to HEAD. Caching now belongs to individual built-in enum constants (`xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java:172`, `:222`). Other types take the uncached path in `BagOfValuesType.java:78`.

2. **Resolved — custom `bagBuilder()` bypass.** `types/ValueType.java:125` again calls `bagBuilder().build()`. `policy/EmptyBagCharacterizationTest.java:126` checks both a rejecting builder and an unshared custom-builder result.

3. **Resolved — missing laziness protection.** `EmptyBagCharacterizationTest.java:140` makes an unwanted fallback invocation throw. The rule at `tools/ast-grep/rules/java-no-eager-empty-bag.yml:14` also covers both function-site forms. Static searches against HEAD matched all three original eager fallbacks; the current production scan passed. The function sites have structural protection rather than new behavioral tests.

**P2 — Shared empty bags expose a non-benign hash-cache race.**

`xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:231` reads the nonvolatile `hashCode` field twice: in the condition and again at return. With concurrent hashing, the first read may observe a nonzero cached hash while the second observes the initial zero. A caller can therefore receive an inconsistent hash, breaking hash-based collection lookup. This matches the pattern fixed in [OpenJDK JDK-8166842](https://bugs.openjdk.org/browse/JDK-8166842).

The hash method predates round 2; enum caching at `XacmlTypes.java:222` makes sharing these bags routine. RVH1’s description of this hash race as benign was incorrect.

**Smallest fix:** read the field once into a local, compute and cache the hash when that local is zero, and return the local. Update the corresponding safety explanation in D14.

**Test:** add a two-actor concurrency stress case over an initially unhashed shared empty bag, forbidding any result different from the independently calculated hash of an equal bag. Runtime reproduction on the project’s JVM is **UNVERIFIED**; the finding follows from the source pattern and the cited OpenJDK defect.

The construction-publication question is separate. The payload references are final (`BagOfValues.java:63`, `:64`), assigned before constructor completion, and the type descriptor also holds its value type in a final field (`BagOfValuesType.java:40`). Those fields receive the guarantees described in [JLS §17.5](https://docs.oracle.com/javase/specs/jls/se25/html/jls-17.html#jls-17.5). That protection does not cover subsequent hash-cache mutations.

No serialization-field change was identified in `BagOfValues`. The added enum cache field does not enter the enum’s serialized representation; enum serialization records its name. [Serialization specification §1.12](https://docs.oracle.com/en/java/javase/25/docs/specs/serialization/serial-arch.html#serialization-of-enum-constants)

Verification performed: source/HEAD comparison, known-positive AST searches, production `ast-grep scan` and scoped `git diff --check`. Ripwire results were **PARTIAL**; quality-delta exited 2 with heuristic findings.

Retained evidence records the intended two failures (`evidence/tests/020-cycleH-round2/red-on-HEAD.txt:51`), 126 passing focused tests (`green.txt:67`), and the successful reactor (`021-reactor-H-harness/output.txt:20`). These were inspected, not rerun. No files, builds, tests or benchmarks were executed or changed in this lane.

**Verdict: APPROVE WITH CHANGES — fix the hash-cache race before accepting shared empty bags.**
