FINAL REPORT (policy4j_code_review, claude-opus-5-5)

Persona name is inferred (UNVERIFIED). Model ID is harness-stated; config pin and serving variant are UNVERIFIED. This is a supplemental lane: I share a provider family with the Fable writer. Nothing was written.

## Verdict: APPROVE

I found no reachable defect in the 18 scoped files. Findings 1–2 are follow-ups, not blockers.

## Hashes

- 18/18 match `final-scope-hashes.json` v2, first and last.
- Revision acknowledged: `NaNFixtureAdjudicationTest.java` moved from `e68b1179…` (r1, superseded) to `d8f9f2db…` (v2). The other 17 are unchanged.

## Combining and staging (source-verified)

- Arity other than 2 or 3: processing-error, Indeterminate{DP}, no child evaluated (`OnPermitApplySecondPolicyCombiningAlgorithm.java:67-70`).
- First child NotApplicable, Deny or Indeterminate{D}: NotApplicable, or the third child (`:72-79`). Permit: the second child (`:80-82`). Otherwise Indeterminate{DP}, status untouched (`:83-85`).
- This matches the retained CS01 transcription (`builds/032-semantic-decisions-grok/summary.md:345-352`). I did not re-fetch the standard.
- Registration is policy-only (`DefaultXacml30DecisionCombiningAlgorithms.java:58-59`).
- Staging was written by the conductor (`combining/report.md:3`). An enclosed rule buffers child and own responses and publishes only the list matching its Permit or Deny (`BaseCompositeDecisionRule.java:143-166,265-319`).
- The outermost rule is unstaged. That is safe: the PDP reads the root only for Permit or Deny (`DefaultPolicyDecisionPoint.java:177-205`).
- Status passes through the wrapper unbuffered (`DescendantEvaluationContext.java:88-95`).
- `getParentContext()` (`:281-285`) skips the wrapped context. I traced `isEnclosed` (`:202-212`) and both reference-cycle walks: nothing they need is skipped.

## Findings

1. **MEDIUM (traceability; no code defect)** `combining-algorithm-registry/spec.md:1-34` specifies only URN resolution, stability and discovery. A search of the change's `specs/` found no requirement for the CS01 §2.1 table or the §7.18 response flow. `BaseCompositeDecisionRule.java:141-166` changes PEP-visible obligations and advice for every nested policy.
   - Fix: add both requirements with scenarios before verify or archive.

2. **LOW (unrun proof)** The extended-indeterminate wrapper (`BaseCompositeDecisionRule.java:188-192`) and the own-response failure path (`:153-159`) have no dedicated assertion. `OnPermitApplySecondPolicyTest.java:238-301` nests only Permit, Deny and NotApplicable children.
   - Child contexts do not inherit the extended-indeterminate flag (`EvaluationContext.java:54-56`), so rules still emit responses there (`Rule.java:133`). Only this wrapper discards them.
   - By reading, returning `extContext` unwrapped at `:191` fails no scoped test. UNVERIFIED: not executed.

## Unrun proof limits and follow-up tests

None of these dedicated scenarios was executed. I do not claim they pass.

- Enclosing permit-overrides; a child policy with an Indeterminate target and a Permit rule carrying a rule obligation; a Permit sibling. Expect the sibling's responses only.
- An enclosed policy whose own obligation expression fails. Expect Indeterminate, error status kept, nothing published.
- `PolicyIDReference` and `PolicySetIDReference` children with responses under a staged policy set.
- An enclosed policy with a rule obligation whose effect differs from the policy decision.
- Four-level nesting (two chained wrappers).
- The multi-result guard branch (`ConformanceCaseRunner.java:187-193`) cannot be reached without changing vendored bytes.

## Prior dispositions (first report)

1. Zero-argument `type-bag`: open, tracked as `policy4j-the.4.53`, blocks 4.7.
2. Type guards: fixed. Validation-off rows cover full and empty integer bags (`DoubleBagSetEqualityTest.java:327-335`).
3. row43: fixed. Enabled (`FunctionSafetyNet43Test.java:605`); six skips remain.
4. Overflow row: withdrawn; task 5.4 tracking accepted.
5. Fixture binding: fixed. The path is bound (`ConformanceCaseRunner.java:178-182`). The wrong-Permit path is rejected; suite, disposition and case id are negative-controlled; PDP tests compare full marshalled responses (`NaNFixtureAdjudicationTest.java:236-288`).
6. XPath wording: fixed (`function-semantics/spec.md:95-114`).

## Verification

- Hash checks, file reads, `rtk rg`, and `rtk proxy jq` on receipts. No builds, tests, network or writes.
- Reactor receipt, not run by me: exit 0; 7589 tests, 0 failures, 0 errors, 30 skipped. Its frozen hashes equal v2 for all 18 files.
- Scoped suites in it: policy test 12, decision table 289, registry 125, NaN adjudication 10, safety net 109 with 6 skipped, double bag 108.
- The receipt's mtime (13:31:27) is 91 seconds after the last scoped edit (13:29:56). I did not open `reactor-green.log` to confirm the run start.

## Remaining scope

- Gate 4.7: zero-argument bags, n-ary union, a real malformed XPath test, and JMH for the scan and staging paths.
- Task 5.4: integer overflow.
- Whole-engine compliance is not claimed.

DONE_FINAL_OPUS_20261004
