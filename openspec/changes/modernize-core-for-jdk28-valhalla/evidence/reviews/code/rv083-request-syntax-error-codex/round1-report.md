No confirmed defects in the supplied diff.

1. **Mapping scope:** `ConformanceCaseRunner.java:217–220` requires `special-harness`, case ID `IIA005`, syntax-error status, and the exact missing-AttributeId message. Another case or a different error message/status is rethrown. The mapped response still undergoes comparison.

2. **PDP exceptions:** Yes. `pdp.decide` is inside the `try` at `ConformanceCaseRunner.java:161`, so its `SyntaxException` reaches the same guard. However, both supplied IIA005 requests throw during unmarshalling, before the PDP call. This does not demonstrate a defect for IIA005. Limiting the catch to request parsing would enforce the ruling more precisely.

3. **Tests:** The four parameterized tests cover both suites and pin successful mapping, named-case restriction, special-harness restriction, and continued response comparison. A missing test is: `IIA005` with `special-harness` throws a syntax-error `SyntaxException` with a different message, and the harness propagates it. The wrong-status guard also lacks an independent negative test. These are coverage gaps, not demonstrated failures.

4. **Manifest:** Both rows consistently use `special-harness,true`, give a comma-free reason describing D12, and leave the bug ID empty.

Verification is limited to the supplied diff and facts; the passing test counts are supplied evidence, not independently verified.

VERDICT: APPROVE
codex exit=0
