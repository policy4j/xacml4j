from pathlib import Path
import collections
import json
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ev = Path(__file__).parent
repo = Path.cwd()
audit = json.loads((ev / '26-source-audit.json').read_text())
baseline = json.loads((ev / '19-vintage-count-characterization-receipt.json').read_text())
final = json.loads((ev / (sys.argv[1] if len(sys.argv) > 1 else '28-core-final-test-receipt.json')).read_text())
dry = json.loads((ev / '03b-dry-run-summary.json').read_text())

def compact(value):
    return re.sub(r'\s+', ' ', value).strip().replace('|','\\|').replace('`','\\`')

notes = {
 'CombinerParameterTest':'Two real BooleanVal values; all object-method assertions retained; the obsolete empty control has no mocks to verify.',
 'ConditionTest':'Real Rule and BooleanVal values; expression/context interfaces retain ordered checks and status captors.',
 'MatchTest':'Real AttributeDesignator and Rule; resolve() and invocation interfaces are verified, including failure status/message capture.',
 'MatchAllOfTest':'Real Match children, real boolean-equal function and concrete reference fixtures; expression evaluations verified in order, including short circuit.',
 'MatchAnyOfTest':'Real MatchAllOf children; expression evaluations verified in order and unused children checked with no-more-interactions.',
 'TargetTest':'Real MatchAnyOf children; expression captors and ordered evaluations retain original composition outcomes.',
 'RuleTest':'Real Target/Condition with expression boundaries; real contexts, advice/obligation values and every original status/result assertion retained.',
 'PolicyTest':'Real Target/Condition; expression and combiner interfaces retain order, captured context metadata and obligations/advice assertions.',
 'PolicySetTest':'Real Target/Condition; expression and combiner interfaces retain order, captured context metadata and obligations/advice assertions.',
 'PolicyIDReferenceTest':'Real Policy, real descendant contexts and immutable metadata assertions; resolver/combiner checks replace synthetic model calls. No reset.',
 'PolicySetIDReferenceTest':'Real PolicySet values and contexts; metadata/result/identity assertions and resolver/combiner checks. No reset.',
 'ObligationExpressionTest':'Real AttributeAssignmentExpression; id assertion replaces its getter expectation; expression failure still asserted and verified.',
 'DurationFunctionIdentifierTest':'MethodSource preserves 72 cases; fresh context per mode; every alias/result assertion plus runtime-flag verification and no-more-interactions.',
 'HigherOrderFunctionIdentifierTest':'MethodSource preserves 22 cases; fresh context per mode, all lookup/evaluation assertions and explicit runtime-flag verification.',
 'HigherOrderVariadicFunctionsTest':'MethodSource preserves 54 cases; fresh BeforeEach mock with mode read after parameter assignment, ArgumentCaptor statuses, explicit checks and AfterEach no-more-interactions.',
 'HigherOrderVariadicInvocationTest':'Real RootEvaluationContext for both runtime modes; boolean mode assertion replaces the optional flag stub; all three tests retained.',
 'ISO8601DurationReviewTest':'Fresh context per mode; all fraction/round-trip assertions retained, plus exact verification of 16 Apply flag calls per mode.',
 'DefaultFunctionSpecTest':'Invocation/context interfaces verified even after assertThrows. Invalid variadic input rejects before invocation; the formerly unreachable expectation is verified with never().',
 'LogicalFunctionsTest':'All six test methods and the checks from 21 legacy group-verify sites retained; expressions/context verified explicitly; five obsolete resets removed without clearing verified history.',
 'XPathFunctionsTest':'Context calls remain ordered; two same-category content resolutions use sequential answers and times(2), preserving both contents.',
 'FunctionSpecBuilderTest':'Invocation interface and no-more-interactions retained; expected exception covers setup and call where setup can throw; unannotated helper restored.',
}

mapping = ['| Core file (relative to src/test/java/org/xacml4j/) | Inventory | Old expect / verify / capture / strict sites | Current verify / InOrder / no-more sites | Adaptation |',
           '| --- | --- | ---: | ---: | --- |']
historical = set(audit['historical_inventory_006'])
for name, row in audit['files'].items():
    if not row['easymock_before']:
        continue
    short = name.split('src/test/java/org/xacml4j/',1)[1]
    stem = Path(name).stem
    note = notes.get(stem,'Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks.')
    old = ' / '.join(str(row[key]) for key in ('legacy_expect','legacy_verify','legacy_captures','legacy_strict'))
    new = ' / '.join(str(row[key]) for key in ('current_verify_sites','current_inorder','current_no_more'))
    mapping.append(f'| `{short}` | {"006" if name in historical else "newer review test"} | {old} | {new} | {note} |')
(ev / 'easymock-mapping.md').write_text('# EasyMock mapping\n\nAll 37 inventory-006 files and five newer files are accounted for below. Counts are lexical source sites, not runtime invocation counts. Real immutable getter checks are assertions, rather than Mockito interaction counts.\n\n'+'\n'.join(mapping)+'\n')

def site_fix(item):
    stem = Path(item['file']).stem
    expr = compact(item['expression'])
    if stem in ('PolicyIDReferenceTest','PolicySetIDReferenceTest'):
        if any(token in expr for token in ('.getId(', '.getVersion(')):
            return 'Real model id/version assertions and unchanged reference/result assertions.'
        if '.createContext(' in expr:
            return 'Real context creation, parent/current-reference identity assertions; evaluation additionally captures the combiner context.'
        if re.search(r'(refPolicy|refPolicySet)\.(evaluate|isMatch)\(',expr):
            return 'Real model evaluation/match and original result assertion; evaluate verifies the combiner interface.'
    if stem == 'ConditionTest' and re.search(r'exp[12]\.getEvaluatesTo',expr):
        return 'Real BooleanVal type assertions, plus unchanged object equality/hash/string assertions.'
    if stem == 'MatchTest' and expr.startswith('ref.getDataType('):
        return 'Real AttributeDesignator datatype assertion and Match constructor validation.'
    if stem == 'MatchTest' and expr.startswith('ref.evaluate('):
        return 'Ordered context.resolve(ref.getReferenceKey()) verification through the real designator.'
    if stem == 'ObligationExpressionTest' and expr.startswith('attrExp.getAttributeId('):
        return 'Real AttributeAssignmentExpression id assertion.'
    if re.match(r'(m\d+|target)\.match\(',expr):
        target = expr.split('.',1)[0] + 'Expression'
        return 'Ordered '+target+'.evaluate(...) verification through real model composition; no-more checks include unused children.'
    if stem in ('PolicyTest','PolicySetTest','RuleTest') and expr.startswith('condition.evaluate('):
        return 'Ordered conditionExpression.evaluate(...) verification through the real Condition.'
    if stem == 'HigherOrderVariadicInvocationTest':
        return 'Real context runtime-mode property assertion; resolver interfaces fail on unexpected external resolution.'
    if stem in ('DurationFunctionIdentifierTest','HigherOrderFunctionIdentifierTest','HigherOrderVariadicFunctionsTest','ISO8601DurationReviewTest'):
        return 'Explicit runtime-flag verify(...); all evaluation/status assertions retained and no-more-interactions checks added.'
    if stem == 'DefaultFunctionSpecTest' and item['method'] == 'testOptionalParameterWithDefaultValueAndVarArg' and expr.startswith('invocation.invoke('):
        return 'verify(invocation, never()).invoke(...): validation throws before this old, unreachable expectation; assertThrows retains the same exception type.'
    obj, call = expr.split('.',1)
    method = call.split('(',1)[0]
    return 'Explicit verify('+obj+').'+method+'(...), with original count/order; capture(...) becomes ArgumentCaptor.capture().'

site_lines = ['# Recorded expectation ledger','',
              'All 480 task-start `expect(...)` sites are listed. Each row points at the task-start source snapshot. Standard interactions keep explicit Mockito verification; eliminated model mocks use real model assertions and verified collaborator calls. Void recording sites remain explicit ordered verifications in the migrated methods. The file-level table accounts for every original group verification and capture site.','',
              '| Task-start site | Method | Recorded expectation and outcome | Migrated check |',
              '| --- | --- | --- | --- |']
for item in audit['original_expectations']:
    relative = item['file'].split('xacml-core/src/test/',1)[1]
    label = Path(item['file']).stem + ':' + str(item['line'])
    site_lines.append(f'| [{label}](before/{relative}#L{item["line"]}) | `{item["method"]}` | `{compact(item["expression"] + item["outcome"])}` | {site_fix(item)} |')
(ev / 'expectation-ledger.md').write_text('\n'.join(site_lines)+'\n')

def actual_case_counts(receipt):
    values = collections.defaultdict(lambda: [0,0])
    for cls, report in receipt['reports'].items():
        for case in report['methods']:
            actual = case.get('class') or cls
            values[actual][0] += 1
            values[actual][1] += int(case['skipped'])
    return values

before_cases, after_cases = actual_case_counts(baseline), actual_case_counts(final)
count_lines = ['| Core class | Declared methods before / after | XML cases before / after | Skips before / after |',
               '| --- | ---: | ---: | ---: |']
for name,row in audit['files'].items():
    cls = name.split('src/test/java/',1)[1][:-5].replace('/','.')
    short = cls.removeprefix('org.xacml4j.')
    left, right = before_cases[cls], after_cases[cls]
    count_lines.append(f'| `{short}` | {row["before_methods"]} / {row["after_methods"]} | {left[0]} / {right[0]} | {left[1]} / {right[1]} |')
count_lines.append('| **Total** | **550 / 550** | **762 / 760** | **4 / 2** |')
(ev / 'per-class-counts.md').write_text('# Per-class counts\n\nXML case counts use each testcase\'s actual classname. Vintage attributed the empty TestFunctions skip to the BagFunctionsTest report and the empty TestInstanceFunctions skip to the DefaultFunctionSpecTest report. Named test invocation counts, including the two actual disabled methods, are 760 before and after. There are 758 executable cases before and after. No test was added or removed.\n\n'+'\n'.join(count_lines)+'\n')

properties = ET.parse('xacml-core/target/surefire-reports/TEST-org.xacml4j.v30.policy.MatchTest.xml').getroot().findall('./properties/property')
props = {p.get('name'):p.get('value') for p in properties}
jdk = f'Java {props["java.version"]}, runtime {props["java.runtime.version"]}, {props["java.vendor"]}, {props["java.vm.name"]}'

count_note = '''# Discovery count decision

Status: PENDING conductor acceptance of the engine count difference. The literal packet threshold of at least 762 reported tests with four skips is not met by Jupiter's 760 reported cases and two skips. The test run itself is green.

`19-vintage-count-characterization-receipt.json` records the exact task-start sources replayed on JDK 21: 762 tests, 0 failures, 96 errors, 4 skips. Its testcase classnames identify both extra entries:

| Vintage report | Actual testcase class | Method name | Reason |
| --- | --- | --- | --- |
| BagFunctionsTest | TestFunctions | empty | original class-level @Ignore; no test methods |
| DefaultFunctionSpecTest | TestInstanceFunctions | empty | original class-level @Ignore; no test methods |

Both helper classes retain the equivalent bare `@Disabled` annotation. Jupiter does not discover them as test classes because they contain no test methods. The two actual disabled methods, ContentTest.testXmlBase64 and Xacml20XPathTo30TransformerTest.testXPathTransformationWithFunctions, remain disabled. All four original @Ignore annotations had no supplied reason string; that absence is preserved. The latter method still carries its existing FIXME about implementing transformation.

Source audit: 550 declarations, same method names/order per class; runtime audit: the same 760 named invocations, with the same parameterized cardinality and skip state. Both suites contain 758 executable cases. No assertion or declared test was deleted to change the count. No dummy test, artificial skip, fixture edit or exclusion was added.

Focused observations: with only the two ignored empty helpers selected, Vintage reports zero cases (run 15). With BagFunctionsTest plus the helpers selected, its report has six named tests plus one empty helper skip (run 16). The full replay (run 19) provides both actual classnames. This is reporting/discovery bookkeeping, not lost test coverage.

The async conductor question requests acceptance of the documented 760 total. No answer is assumed from elapsed time. Once accepted, run 28 is the task-2.3 test gate; until then the numeric acceptance condition remains pending.
'''
count_note = count_note.replace('run 28', 'run ' + final['log'].split('-',1)[0])
(ev / 'gate-count-note.md').write_text(count_note)

readme = '''# Core JUnit 5 / Mockito migration — tasks 2.2 and 2.3

Result: the final JDK 21 core test run is GREEN: **760 tests, 0 failures, 0 errors, 2 skipped**. The literal 762/four-skips count threshold needs conductor acceptance of two empty Vintage helper entries disappearing; see [gate-count-note.md](gate-count-note.md). **No declared test or assertion was removed.**

Toolchain: JDK_LINE. Surefire 3.0.0 uses JUnitPlatformProvider. The task-2.1 root configuration (Jupiter/Vintage 5.14.4, Mockito 5.24.0 startup agent) is unchanged.

Scope: 105 of the 106 existing core Java test sources changed, plus the new ModelFixtures.java helper. All changes are under xacml-core/src/test/java and this run directory. SHA-256 audit confirms all 3,451 protected paths, including every POM, every main source, other module tests/resources, task/design/index files and prior runs 008/009, are unchanged from task start. The shared tree's pre-existing dirty files were preserved. Existing license blocks are byte-for-byte unchanged; sources already lacking a block retain their original state. No commit, staging, branch change, br command, task tick or issue closure occurred.

## Recipe dry run and application

- Plugin: `org.openrewrite.maven:rewrite-maven-plugin:6.46.1`.
- Recipe artifact: `org.openrewrite.recipe:rewrite-testing-frameworks:3.44.0`.
- Recipes: `org.openrewrite.java.testing.junit5.JUnit4to5Migration` and `org.openrewrite.java.testing.easymock.EasyMockToMockito`.
- Versions were checked against official published Maven metadata (2026-08-12 update) and successfully resolved/executed. The plugin reference shows a newer example version; the published resolvable version was pinned for this run. Context7 was unavailable in the exposed tools, so native DuckDuckGo fallback located the official OpenRewrite docs.
- Dry run 03b succeeded without modifying any source or protected file: **105 files, 2,225 added lines, 2,382 removed lines**, all in core test Java sources. Its diff removed **210 verification lines** and added 43; that loss was explicitly rejected during manual completion. [Dry-run diff](03b-rewrite-dry-run.patch), [machine summary](03b-dry-run-summary.json).
- The recipes leave unsupported controls, invalid captures/factories, ambiguous Jupiter overloads, license movement/duplication and incomplete exception scopes. They also initialize a parameterized context before assigning its runtime mode. The manual completion repairs these rather than accepting the generated code as finished.
- Run 04b applies the exact pinned recipes. Exclusions cover every POM, src/main and test resources; runPerSubmodule limits execution to the selected core module, skipMavenParsing avoids POM migration, and the plugin is invoked only by command-line coordinates. No plugin or dependency was added to a POM. Environmental cache write denials in 03 and 04 were retried with approved cache access in 03b and 04b; neither failed attempt edited source.

Primary documentation: [plugin reference](https://docs.openrewrite.org/reference/rewrite-maven-plugin), [JUnit4to5Migration](https://docs.openrewrite.org/recipes/java/testing/junit5/junit4to5migration), [EasyMockToMockito](https://docs.openrewrite.org/recipes/java/testing/easymock/easymocktomockito), [plugin metadata](https://repo.maven.apache.org/maven2/org/openrewrite/maven/rewrite-maven-plugin/maven-metadata.xml), [recipe metadata](https://repo.maven.apache.org/maven2/org/openrewrite/recipe/rewrite-testing-frameworks/maven-metadata.xml).

## Manual completion / test intent

- All 37 inventory-006 files and five newer review tests are mapped below. The task-start snapshot contains 480 expect sites, 249 group-verify sites, 68 capture declarations and 90 strict-mock/control creation sites. [Expectation ledger](expectation-ledger.md) accounts for every expect site, including real-model replacements and the formerly unreachable variadic invocation expectation.
- Ordered interactions use InOrder and no-more-interactions checks. Mockito mocks are recreated in BeforeEach; inline per-mode mocks remain fresh as before. No reset or clearInvocations call remains. Empty controls disappear when all their values are real immutable objects; all original object/value assertions remain.
- All 66 listed model/value mock sites become real models and values. The concrete reference fixture delegates to an expression interface, retaining per-child order/capture assertions through real Match/MatchAllOf/MatchAnyOf/Target composition. Only interfaces are mocked; the audit resolves all 11 target types to their main-source declarations.
- Policy/PolicySet references now prove actual context creation and identity, id/version metadata and real model evaluation via a verified combiner, rather than forcing model methods to return canned results. Five original context captors in these two files had no captured-value assertion; the replacement uses real context identity assertions and combiner context captors on the evaluation paths.
- The Rule target-failure fixture now injects failure at the expression boundary inside a real target and records the supplied status on its evaluation context, as a resolver can do. This no longer forces a checked exception directly out of a mocked Target. The original Indeterminate decision and both full status/message assertions remain. Match's resolution-failure test uses a real mustBePresent designator, so the same missing-attribute exception reaches the real Match.
- All 48 expected-exception methods use assertThrows with the original type; existing catch/status/message assertions stay. The optional/variadic rejection test verifies the old invocation expectation with never(), because parameter validation throws before invocation and the original verify was unreachable. The unannotated builder helper remains unannotated and retains its original isVariadic assertion.
- Same-category XPath content calls use sequential Mockito answers and times(2), retaining both recorded contents. Five parameterized classes use MethodSource with unchanged invocation counts. The variadic context's default answer reads the runtime mode after assignment, and a fresh mock/captor is created in BeforeEach.
- All four bare Ignore annotations become bare Disabled annotations. The two real skipped methods remain; the two empty provider-class entries disappear from Jupiter reporting, as proved by the full baseline replay.

## Red / green evidence

| Run | Outcome | Cause / disposition |
| --- | --- | --- |
| 02 baseline | 762 / 0 failures / 96 errors / 4 skipped | Original EasyMock class/value proxy failures in 11 classes; recorded before recipe edits. |
| 03b dryRun / 04b run | SUCCESS | Both pinned recipes executed; all paths in scope. |
| 05 after recipes | testCompile RED, 100 reported diagnostics (compiler cap) | Unsupported controls/captures and remaining migration errors; no tests executed. |
| 09 manual migration | testCompile RED, 12 diagnostics | Missing helper imports/builder operation, split andThrow and generic Jupiter assertEquals overloads; fixed without changing asserted values. |
| 11 | testCompile RED, 1 diagnostic | FunctionInvocation has two abstract overloads, so a lambda is invalid; use the real default boolean-equal provider. |
| 12 first behavior run | 760 / 10 failures / 1 error / 2 skipped | Constructor interactions versus empty InOrder checks (five cases); required designator behavior; real target status propagation; two unnecessary parent-flag expectations; builder exception scope; unreachable variadic invocation expectation. |
| 14 and 18 | 760 / 0 / 0 / 2, SUCCESS | All above repaired; explicit runtime flag checks and lifecycle added. |
| 15 / 16 / 19 | count characterization | Temporary exact task-start source replay; migrated bytes restored afterward. Run 19 reproduces 762 / 0 / 96 / 4 and identifies both unnamed helper skips. |
| 22 clean test | 760 / 0 / 0 / 2, SUCCESS | Core-only clean rebuild on JDK 21. |
| 24 interface boundary completion | testCompile RED, 1 diagnostic | Concrete reference fixture requires accept(ExpressionVisitor); added a fail-fast visitor guard for this evaluation-only fixture. |
| 25 and 28 | 760 / 0 / 0 / 2, SUCCESS | Interface-only mocks, no resets; run 28 is the final source gate. |

Counts are tests / failures / errors / skipped. Receipts record every compiler diagnostic and failure trace. The compiler-error repairs affect tests/helper code only. The underlying core API, function semantics and all conformance fixtures are unchanged.

## Scope and preservation checks

`26-source-audit.json`: **550 declared methods before/after, identical names/order per file; 760 named invocations before/after with identical per-method parameterized cardinality and skip state; 1,596 assertion calls before, 1,671 after**, with no per-file decrease in any original assertion-call kind. These counts support the manual source review; counts alone do not prove semantic equivalence. The original captured-value assertions are retained in the mapped sources.

SUPPORTED syntactic proof: rtk rg finds zero legacy imports (exit 1 = no match), while the ast-grep import-declaration rule matches 544 imports in the task-start positive control and zero in current core tests. Every current Mockito target is an interface by its opened main-source declaration; no model/value mock, reset, clearInvocations or unaccounted @Mock field remains. Git diff --check passes after fixing only newly added whitespace lines, outside license blocks. [Source audit](26-source-audit.json), [exception audit](10-exception-audit.json), [commands](command.txt), [Surefire output](surefire-summary.txt).

## EasyMock-to-Mockito / real-model mapping

MAPPING_TABLE

## Per-class method and case counts

Cases below use the actual XML testcase classname. The two empty provider skips are shown under their own classes; Vintage's textual Maven reports attributed them to BagFunctionsTest and DefaultFunctionSpecTest. [Standalone counts](per-class-counts.md) and raw Results/per-report counts in surefire-summary.txt retain that distinction.

COUNT_TABLE

## Remaining acceptance and risk

Task 2.2 is implemented and evidenced. Task 2.3's test run is green and declared-test/named-invocation parity is proved; the literal 762/four-skip numeric condition remains PENDING conductor decision. No decision is inferred from silence. The task/evidence index and beads status are left to the conductor.

Full reactor/conformance and JDK 28 are UNRUN in this lane, per the packet's core-only scope. Real-fixture substitutions require review for the documented proof changes above. The main sources and dependency/plugin configuration are unchanged.
'''
before_assertions = sum(sum(v['before_assertions'].values()) for v in audit['files'].values())
after_assertions = sum(sum(v['after_assertions'].values()) for v in audit['files'].values())
readme = readme.replace('JDK_LINE',jdk).replace('MAPPING_TABLE','\n'.join(mapping)).replace('COUNT_TABLE','\n'.join(count_lines))
readme = readme.replace('1,596 assertion calls before, 1,671 after', f'{before_assertions:,} assertion calls before, {after_assertions:,} after')
if audit['protected_changes']:
    readme = readme.replace('All changes are under xacml-core/src/test/java and this run directory.',
                            "This lane's changes are under xacml-core/src/test/java and this run directory.")
    readme = readme.replace("SHA-256 audit confirms all 3,451 protected paths, including every POM, every main source, other module tests/resources, task/design/index files and prior runs 008/009, are unchanged from task start. The shared tree's pre-existing dirty files were preserved.",
                            f"SHA-256 audit checks all {audit['protected_files_checked']:,} protected paths. Root/core POMs, core main sources, JAXB/vendored inputs and prior runs 008/009 still match task start. {len(audit['protected_changes'])} other protected paths changed in the shared tree; authorship is UNATTRIBUTED, and this lane did not write or restore those paths. See [shared-tree-changes.json](shared-tree-changes.json).")
readme = readme.replace('No commit, staging, branch change, br command, task tick or issue closure occurred.',
                        'This lane performed no commit, staging, branch change, br command, task tick or issue closure.')
readme = readme.replace('run 28 is the final source gate.', 'run 28 is the pre-review migration checkpoint; the later final gate is recorded below.')
(ev / 'summary.md').write_text(readme)

summary_lines = []
logs = ['02-core-baseline-red.log','12-first-behavior-red.log','14-refined-behavior.log','18-core-final-gate.log',
             '15-vintage-empty-helpers.log','16-vintage-skip-bookkeeping.log','19-vintage-count-characterization.log',
             '22-core-clean-gate.log','25-core-migration-gate.log','28-core-final-test.log']
logs.extend(p.name for p in sorted(ev.glob('*.log')) if p.name.split('-',1)[0].isdigit() and int(p.name.split('-',1)[0]) >= 34)
for name in logs:
    raw = (ev / name).read_text()
    selected = [line for line in raw.splitlines() if re.search(r'Tests run: \d+, Failures:',line)]
    results = re.search(r'^\[INFO\] Results:[\s\S]*?(?=^\[INFO\] -{10,})',raw,re.M)
    summary_lines.extend([name,''])
    summary_lines.extend(line for line in selected if ' - in ' in line)
    if results:
        summary_lines.extend(['',results.group(0).rstrip()])
    summary_lines.extend(['',next((line for line in raw.splitlines() if 'BUILD SUCCESS' in line or 'BUILD FAILURE' in line),'BUILD RESULT NOT FOUND'),''])
(ev / 'surefire-summary.txt').write_text('\n'.join(summary_lines)+'\n')

status = subprocess.run(['rtk','git','status','--short'],capture_output=True,text=True)
assert status.returncode == 0
(ev / '30-final-git-status.txt').write_text(status.stdout)
for path in ev.rglob('*'):
    if not path.is_file() or path.suffix not in ('.log','.json','.txt','.md','.patch'):
        continue
    text = path.read_text()
    path.write_text(text.replace(str(repo),'<repo>').replace(str(Path.home()),'<user-home>'))
print(json.dumps({'result':final['counts'],'jdk':jdk,'mapping_files':len(mapping)-2,'expectation_sites':len(audit['original_expectations']),
                  'source_methods':audit['test_declarations_after'],'protected_files':audit['protected_files_checked'],
                  'numeric_gate':'PENDING_COUNT_DECISION'},indent=2))
