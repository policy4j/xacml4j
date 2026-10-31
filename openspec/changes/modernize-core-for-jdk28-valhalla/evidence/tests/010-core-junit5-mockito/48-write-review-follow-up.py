from pathlib import Path
import hashlib
import json
import re

ev = Path(__file__).parent
review = json.loads((ev / '32-review-artifacts-audit.json').read_text())
before = json.loads((ev / '32-review-artifacts-before.json').read_text())
counts = json.loads((ev / '44-exact-count-checks.json').read_text())
assert not (ev / '36-active-mutation.json').exists(), 'A mutation is still active'
for key in ('plain_mock_call_candidates', 'unwrapped_mock_call_candidates',
            'verify_inside_assertThrows', 'duplicate_consecutive_verifies'):
    assert not review[key], key
assert all(c['getter_sites'] for c in review['captor_declarations'])
assert all(v['current_checks'] for v in review['original_void_recording_sites'])
assert all(v['current_verify'] or v['current_no_more'] for v in review['original_mock_only_methods'])

mutants = [
    ('order','37-mutant-order-red','InOrder rejects reversed matching children'),
    ('count','38-mutant-count-red','Exact count rejects five calls where four were recorded'),
    ('exception_verify','39-mutant-exception-verify-red','assertThrows succeeds, then verification rejects missing resolve'),
    ('captured_rule','40-mutant-captured-rule-red','New captured Rule identity assertion rejects a different Rule'),
    ('empty_control','42-mutant-empty-control-red','No-more-interactions rejects an unexpected invocation with no expectations'),
    ('cache','46-mutant-cache-exact-count-red','Explicit total rejects a second resolver call after cached evaluation'),
]
lines = ['| Oracle / test | Temporary mutation | Outcome | Restoration |', '| --- | --- | --- | --- |']
proofs = []
for case, prefix, result in mutants:
    receipt = json.loads((ev / (prefix + '-receipt.json')).read_text())
    restored = json.loads((ev / ('36-' + case + '-restoration.json')).read_text())
    if case == 'count':
        restored['intent'] = 'Add a fourth input without removing any expected member; times(4) must reject the fifth flag call.'
    elif case == 'cache':
        restored['intent'] = 'Simulate an unnecessary second resolver call after cached evaluation; the result remains Indeterminate but the one-call resolver budget must reject it.'
    (ev / ('36-' + case + '-restoration.json')).write_text(json.dumps(restored,indent=2) + '\n')
    assert receipt['counts'] == [1,1,0,0] and receipt['tests_executed'] and not receipt['compiler_diagnostics'], prefix
    assert restored['restored_exact_bytes'], case
    original = (ev / restored['backup']).read_bytes()
    assert hashlib.sha256(original).hexdigest() == restored['original_sha256']
    problem = receipt['problems'][0]
    lines.append(f'| `{Path(restored["file"]).stem}.{restored["method"]}` | {restored["intent"]} | {result}; **1 / 1 / 0 / 0** ([receipt]({prefix}-receipt.json)) | Exact bytes restored |')
    proofs.append({'case':case,'run':prefix,'result':receipt['counts'],'failure':problem['type'],
                   'method':restored['method'],'restored_exact_bytes':True,'scope':restored['mutation_boundary']})
surviving = json.loads((ev / '41-mutant-cache-red-receipt.json').read_text())
assert surviving['counts'] == [1,0,0,0]
final = json.loads((ev / '47-core-oracle-final-gate-receipt.json').read_text())
assert final['build_success'] and final['counts'] == [760,0,0,2]

fixes = json.loads((ev / '33-captor-oracle-fixes.json').read_text())
for item in fixes:
    if item['captor'] == 'contextCapture':
        operation = 'getCurrentPolicySet' if Path(item['file']).stem == 'PolicySetTest' else 'getCurrentPolicy'
        item['added_assertion'] = 'assertTrue(contextCapture.getValue().isExtendedIndeterminateEval()); assertSame(policy, contextCapture.getValue().' + operation + '());'
(ev / '33-captor-oracle-fixes.json').write_text(json.dumps(fixes,indent=2) + '\n')

text = '''## Conductor review follow-up: recipe artifacts / oracle strength

Result: final run **47** on JDK 21 is GREEN: **760 / 0 failures / 0 errors / 2 skipped**. Run 45 also passes with the same counts. All temporary mutations are restored; no production file was mutated. The literal 762/four-skips acceptance remains pending as documented above.

| Conductor item | Evidence / disposition |
| --- | --- |
| 1. Plain RECORD-phase calls on Mockito mocks | Java statement and method-invocation AST scans find zero bare mock calls outside stubbing in current sources. The task-start positive control contains ten void recording sites; all ten map to explicit post-stimulus checks (the higher-order context setup maps to assertInvalid plus AfterEach). |
| 2. Verification inside assertThrows | Zero verify calls inside an assertThrows invocation. AttributeDesignatorTest's pre-existing try/catch that could fall through was strengthened to assertThrows; every original status/reference assertion remains after it. |
| 3. Dropped strict / ordered checks | Every expectation remains in the existing ledger; all ten recorded void calls have checks. InOrder sequences remain. Cache mutation initially survived (run 41) because strict-stub handling marked the extra stubbed call verified. Added COUNT_CHECKS explicit cumulative total checks at BOUNDARIES boundaries in COUNT_FILES files, alongside ordered checks, to restore exact frequency enforcement. Totals come from expected calls, never actual invocation history. |
| 4. Duplicate consecutive verification | Zero identical consecutive verification statements. Ordered checks establish sequence; companion global times(N) checks establish total frequency, including interleaved extra stubbed calls. These have distinct purposes. |
| 5. Unasserted captors | Initial review confirmed **40** unasserted captors (pre-existing), across seven files / 34 methods. Added key, lazy status, Rule identity, rule-list and context checks. All 65 current captor declarations now have asserted value reads. Initial run 34: 61 / 2 / 0 / 0, due to two new assertions incorrectly assuming a direct context; corrected those new assertions to check extended mode and policy metadata on the existing Indeterminate wrapper. Run 35: **61 / 0 / 0 / 0**. No original assertion was removed. |
| 6. Lost times(N) / once checks | All 17 explicit count sites are accounted for: 15 remain exact mock-count checks; two Condition object-method sites now use real BooleanVal values with explicit type assertions. Implicit once calls keep explicit checks. Companion cumulative counts also enforce earlier expectations during later cached/scenario calls. |
| 7. control.verify-only or empty controls | All 24 methods found without an assertion call in the old body retain explicit interaction and/or no-more checks; expected-exception annotations are now explicit assertThrows. The empty-control mutation fails at verifyNoMoreInteractions. Real-value-only controls disappear with their model/value assertions preserved. |

SUPPORTED: ast-grep represents Java syntax. Receiver identity inference and count correspondence were also reviewed in source; this is not an IDE reference inventory. [Before review](32-review-artifacts-before.json), [final artifact audit](32-review-artifacts-audit.json), [new capture checks](33-captor-oracle-fixes.json), [exact-count checks](44-exact-count-checks.json). No Mockito strictness was relaxed, no assertion/count was adjusted to absorb a test's own normal invocation, and no reset/clearInvocations was added.

### Temporary mutation proof

Mutations changed owned test stimuli/collaborator answers only, to simulate wrong order, extra or missing calls, and wrong captured data. This proves the test oracles reject those faults; it is **not** a mutation-coverage claim for production code. None was an assertion or verification mutation. Every red below executed its test and failed at the listed oracle, not during compilation. The cache check was repeated after repair; the other original oracle checks were retained while the total checks were added.

MUTATION_TABLE

Run 41's initially surviving cache mutation is retained as [receipt](41-mutant-cache-red-receipt.json) and [initial patch](41-cache-mutation.patch). Run 46 repeats it against the strengthened source and fails with TooManyActualInvocations: wanted one resolver call, observed two. The code returns to exact pre-mutation bytes, verified by SHA-256 restoration receipts; run 47 then executes every core case successfully.

### Follow-up scope / gate

This follow-up changes seven files for capture/exception checks and adds total-count checks in 32 existing core test files (overlapping sets); the full migration still changes 105 existing sources plus ModelFixtures.java. All 550 original declared test methods and all 760 named invocations remain, including the same two actual disabled tests. License blocks remain byte-identical. Root/core POMs, core main code, vendored inputs and runs 008/009 match task-start hashes.

The shared-tree audit now observes 30 changed paths outside this lane (other module migration sources, one module POM and conductor tracking/evidence files). Authorship is UNATTRIBUTED; this lane's write commands/patches were restricted to core test sources and this run. Those external changes were preserved, not restored. The global preservation audit's original strict mode correctly rejected those differences; its explicit --allow-shared-changes mode records them while still rejecting any change to the protected core/POM/vendored/prior-run paths. [Shared-tree differences](shared-tree-changes.json).

Full reactor/conformance and JDK 28 remain UNRUN in this lane. No commit, br command, task tick, issue closure or out-of-scope fix was performed here.
'''
text = text.replace('COUNT_CHECKS',str(sum(len(r['checks']) for r in counts)))
text = text.replace('BOUNDARIES',str(len(counts))).replace('COUNT_FILES',str(len({r['file'] for r in counts})))
text = text.replace('MUTATION_TABLE','\n'.join(lines))
(ev / 'review-artifacts.md').write_text(text)
summary = (ev / 'summary.md').read_text()
if '## Conductor review follow-up: recipe artifacts / oracle strength' in summary:
    summary = summary.split('## Conductor review follow-up: recipe artifacts / oracle strength',1)[0] + '## Remaining acceptance and risk' + summary.split('## Remaining acceptance and risk',1)[1]
summary = summary.replace('## Remaining acceptance and risk',text + '\n## Remaining acceptance and risk')
(ev / 'summary.md').write_text(summary)
(ev / '48-review-follow-up-receipt.json').write_text(json.dumps({
    'final_gate':final['counts'],'initial_unasserted_captors':sum(c['needs_review'] for c in before['captor_declarations']),
    'current_captor_declarations':len(review['captor_declarations']), 'current_unasserted_captors':0,
    'cumulative_count_checks':sum(len(r['checks']) for r in counts),'oracle_mutations':proofs,
    'initial_cache_mutation_survived':True,'repeated_cache_mutation_failed':True,
    'no_active_mutation':True},indent=2) + '\n')
print(json.dumps({'final_gate':final['counts'], 'captors_checked':len(review['captor_declarations']),
                  'cumulative_count_checks':sum(len(r['checks']) for r in counts),
                  'distinct_oracles_proved':len(proofs),'all_mutations_restored':True}, indent=2))
