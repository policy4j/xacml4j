# 034: cycle K, context queries in one loop (15.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

Design D17 as built: pure forwarders walk the parent fields to the first context that is not a
`DescendantEvaluationContext` and call it once; `getCurrentRule`, `getCurrentPolicy`, `getCurrentPolicySet`,
and the two reference queries are answered by the nearest context of the answering class (a type test per
level); all are final; `isEnclosed` walks the parent fields; the handler's request memo keeps the
`Optional` of the request entity.

- Focused (`cycleK-green.log`, `-pl xacml-pdp -am test`): xacml-core 6,582 tests (2 skipped), xacml-pdp 218
  (3 skipped), 0 failures; the red test of 033 passes.
- Reactor (`reactor.txt`, `clean test`): 8,309 tests, 0 failures, 0 errors, 18 skipped; conformance 938
  (12 skipped), unchanged.
