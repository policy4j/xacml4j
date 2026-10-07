## Round 4 gate

The conductor approved required-repository-listener-barrier-fix.patch and
extended the scope to every listener-verifying test in this class. Status:
**APPROVED / APPLIED / VERIFIED**. The original two-test proposal is retained
as historical evidence; 90-round4-gate-fixes.patch is the extended applied delta.

InMemoryPolicyRepositoryTest.java:94 now has one helper that registers a
forwarding listener and returns a per-test CountDownLatch. Each callback
forwards to the existing EasyMock listener, then counts down after it returns.
Each test awaits completion for at most five seconds, with an explicit
callback timeout message, before its original c.verify(). No callback mock
expectation, repository assertion, verification, fixture or exclusion changed.

| Test | Callback count | Original verify -> final await / verify |
| --- | ---: | --- |
| testOnePolicyDifferentVersion | 4 added | :143 -> :174 / :175 |
| testAddRemove | 1 added + 1 removed | :166 -> :199 / :200 |
| testAddPolicyWithTheSameIdAndSameVersion | 1 added | :183 -> :218 / :219 |
| testFindAllPoliciesWithTheSameId | 4 added | :219 -> :256 / :257 |

Tests first: run 85 uses unchanged current source for thirty single-class
JDK 11 Maven runs. **26 pass / 4 fail**, 120 tests / 4 failures / 0 errors /
0 skips. Iterations 20, 21, 22 and 25 fail listener verification;
testAddRemove fails once and testFindAllPoliciesWithTheSameId three times.
Every failure is retained with its stack and original source hash. Earlier
full red gates 78/80 remain unchanged historical evidence. Run 87 repeats
the same thirty invocations after the fix: **30 pass / 0 fail**, 120/0/0/0.
All sixty reports are fresh and attest JDK 11.0.32.1; no compiler diagnostics.
The selected-class command runs four methods without skips. Full discovery
still reports the existing empty Vintage skip in this class; it is unchanged.

Runs 88 and 89 execute the full reactor with identical source, both
**936 tests / 0 failures / 0 errors / 9 existing skips, BUILD SUCCESS**.
Each gate executes OASIS 2.0 347 and OASIS 3.0 347 cases, RSA 17 and examples
2, all passed; the two resolver characterization methods also pass. No failed
configured case IDs. Case order, thirty exclusions per OASIS suite and loop
bounds match run 71. IIIG006 still executes twice in each OASIS suite.
Both gates repeat all 63 higher-order/status checks from the prior LOW fix.
Fresh receipts, per-module JDK properties and stripped repository reports
are retained separately for each gate before the repeat overwrites targets.

Scope proof: 3543 protected source/POM/resource/run003 files unchanged;
only this approved PDP test changed. All four original test bodies are
identical after removing the new barriers and checked-exception declaration.
All four c.verify() calls have barriers, the license header and tabs are kept,
and scoped git diff --check passes. No production threading change, sleep,
fixture/expected-result change, exclusion, commit, br, task tick or closure.

Residual limits: this finite repetition is not a proof against every possible
scheduler. First-error semantics remain DEFERRED as policy4j-the.4.12.
Configured conformance exclusions/bounds and previously recorded additional
IIIE301-303 proposals are unchanged; those additional cases were not rerun.
