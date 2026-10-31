READ-ONLY RE-CHECK (a new packet in this pane; same rules: no file writes, no Maven, tests, benchmarks,
git or br writes). Role: the reviewer of RCJ1 and RCJ2. Lane tag RCJ3.

Your RCJ2 finding is addressed in xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java
(FunctionSpecImpl.normalize and the new isIndexable): normalization indexes only Guava ImmutableList, the JDK
List.of classes, and the Arrays.asList views of the varargs path (whose iterator also reads the array
live); any other caller list is copied once with new ArrayList<>(list), that is through the list's own
iteration. Test: FunctionInvocationCharacterizationTest.callerListIsNormalizedAsItsSnapshot (your
transformed CopyOnWriteArrayList example, replacement and removal variants), red on the previous version
(ArrayIndexOutOfBoundsException) and green now: openspec/changes/optimize-pdp-evaluation/evidence/tests/032-rcj2-fix.

Check the fix (does ArrayList's copy constructor read a transformed or copy-on-write view through its
snapshot; are the three class checks right for empty and single-element lists; any remaining difference
from HEAD for null, missing, extra, or interleaved arguments), then look once more at the whole of cycle J
(Target, MatchAnyOf, MatchAllOf, normalize). Report: findings most severe first (P1 to P3) with file:line,
the smallest fix, and the test; then a verdict: APPROVE, APPROVE WITH CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RCJ3 REPORT ===
=== END RCJ3 REPORT ===
