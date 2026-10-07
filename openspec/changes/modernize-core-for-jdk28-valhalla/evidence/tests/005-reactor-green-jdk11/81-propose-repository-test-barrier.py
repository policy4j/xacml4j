from pathlib import Path
import difflib

repo = Path.cwd()
ev = Path(__file__).parent
name = "xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java"
before = (repo / name).read_text()
after = before.replace("import java.util.Iterator;", "import java.util.Iterator;\nimport java.util.concurrent.CountDownLatch;\nimport java.util.concurrent.TimeUnit;", 1)
after = after.replace("import org.xacml4j.v30.policy.Policy;", "import org.xacml4j.v30.policy.Policy;\nimport org.xacml4j.v30.policy.PolicySet;", 1)
after = after.replace("\tprivate PolicyRepositoryListener l;", "\tprivate PolicyRepositoryListener l;\n\tprivate CountDownLatch addedPolicies;", 1)
registration = """\t\tthis.addedPolicies = new CountDownLatch(4);
\t\tthis.r.addPolicyRepositoryListener(new PolicyRepositoryListener()
\t\t{
\t\t\t@Override
\t\t\tpublic void policyAdded(Policy p){
\t\t\t\tl.policyAdded(p);
\t\t\t\taddedPolicies.countDown();
\t\t\t}

\t\t\t@Override
\t\t\tpublic void policyRemoved(Policy p){
\t\t\t\tl.policyRemoved(p);
\t\t\t}

\t\t\t@Override
\t\t\tpublic void policySetAdded(PolicySet p){
\t\t\t\tl.policySetAdded(p);
\t\t\t}

\t\t\t@Override
\t\t\tpublic void policySetRemoved(PolicySet p){
\t\t\t\tl.policySetRemoved(p);
\t\t\t}
\t\t});"""
assert after.count("\t\tthis.r.addPolicyRepositoryListener(l);") == 1
after = after.replace("\t\tthis.r.addPolicyRepositoryListener(l);", registration, 1)
for method in ("testOnePolicyDifferentVersion", "testFindAllPoliciesWithTheSameId"):
    start = after.index("\tpublic void " + method + "()")
    end = after.index("\n\t}", start)
    body = after[start:end]
    assert body.count("\t\tc.verify();") == 1
    body = body.replace("\t\tc.verify();", '\t\tassertTrue("Four policy-added callbacks must finish before mock verification",\n\t\t\t\taddedPolicies.await(5, TimeUnit.SECONDS));\n\t\tc.verify();', 1)
    after = after[:start] + body + after[end:]
assert before != after
assert after.count("c.verify();") == before.count("c.verify();")
patch = "".join(difflib.unified_diff(before.splitlines(keepends=True), after.splitlines(keepends=True),
                                  fromfile="a/" + name, tofile="b/" + name))
(ev / "required-repository-listener-barrier-fix.patch").write_text(patch)
proposal = """# Round 4 gate blocker proposal: repository listener completion

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
"""
(ev / "round4-gate-blocker-proposal.md").write_text(proposal)
print("Prepared unapplied test-only barrier patch and proposal; PDP source/tests unchanged")
