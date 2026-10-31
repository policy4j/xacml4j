# Round 4 gate proposal: repository listener completion

Status: **APPROVED / APPLIED / VERIFIED** by the conductor's Round 4 gate
blocker decision. Scope is this test only, extended to all four tests that
verify listener callbacks after asynchronous changes. The retained
required-repository-listener-barrier-fix.patch is the initial two-test proposal;
90-round4-gate-fixes.patch is the approved extended implementation.

One helper forwards all listener methods to the unchanged EasyMock listener
and counts down after completion. Counts are 4, 2, 1 and 4; every c.verify()
is preceded by a five-second await and clear timeout assertion. Original
callback expectations and all repository assertions are preserved. Production
threading, fixtures, expected results, sleeps and exclusions were not changed.

Proof: before source unchanged, 30 runs -> 26 pass / 4 fail (testAddRemove
once, testFindAllPoliciesWithTheSameId three times); after -> 30 pass / 0 fail.
85-/87-repository-*/summary.json retain counts/hashes/JDK and each report/log.
Full reactor gates 88/89 both pass 936/0/0/9, including configured conformance
347+347 OASIS cases, 17 RSA and 2 examples, all passed. No source change
between stability-after and either full gate. 3543 protected files unchanged.

Original red/source analysis is retained below; its PROPOSED/UNVERIFIED
status describes the earlier unapproved packet, and is superseded above.

## Original proposal (historical)

# Round 4 gate blocker proposal: repository listener completion

Status: PROPOSED ONLY. Outside packet 1.3j's LOW-only scope; no PDP source or
test was edited. The patch is UNVERIFIED: not applied, compiled or tested.

Run 78 fails InMemoryPolicyRepositoryTest.testOnePolicyDifferentVersion:143
and testFindAllPoliciesWithTheSameId:219 during c.verify(). Run 80 repeats the
same full gate with unchanged source and again fails the second test; the
first now passes. Both logs/stripped reports are retained. Both rounds' core
762 tests have zero failures/errors; the LOW-focused tests pass in each.
Test-support and conformance are skipped by the reactor dependency chain.

Classification: pre-existing repository/test race, not a map adaptation
regression. The timing cause is INFERRED from source and differing failure
counts, rather than a controlled scheduler trace. AbstractPolicyRepository
and InMemoryPolicyRepositoryTest are byte-identical to a8a90d46, verified by
79-repository-race-proof.json and 80-repository-race-proof.json.
AbstractPolicyRepository.java:71 creates a single-thread executor;
:167-170 enqueue each policyAdded callback. The test's :97-111 and :191-208
expect/enqueue four additions but :143/:219 verify the mock without a
completion barrier. Policies contain no Apply/functions; the FunctionProvider
is a mock, and the function-validating visitor is commented out at :217.
Repository lookup/version assertions pass; the failure is listener verification.

Requested scope: InMemoryPolicyRepositoryTest.java only. The concrete patch
required-repository-listener-barrier-fix.patch forwards every listener call to
the existing mock; after each successful policyAdded callback it counts down
a four-event latch. Only the two observed four-addition tests await completion
with a bounded timeout before their existing c.verify(). All original mock
expectations, version/count assertions and verification calls remain intact.
No production threading change, fixture change, expected-result edit, sleep
or test exclusion is proposed. Other repository tests are not covered by this
specific barrier proposal; broader listener-test synchronization remains a
maintainer choice.

If approved: keep runs 78/80 as red evidence; apply the test-only patch, run
InMemoryPolicyRepositoryTest on JDK 11, then repeat the full reactor verify.
Record any further failure rather than weakening assertions. The configured
conformance gate must execute before this round can claim full-reactor green.
