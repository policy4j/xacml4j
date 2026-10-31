from pathlib import Path
import collections
import json
import re

ev = Path(__file__).parent
audit = json.loads((ev / '32-review-artifacts-audit.json').read_text())
(ev / '32-review-artifacts-before.json').write_text(json.dumps(audit, indent=2) + '\n')
support = (ev / '06-manual-mock-adapt.py').read_text()
exec(support[support.index('def mask'):support.index('def convert_calls')])
by_file = collections.defaultdict(lambda: collections.defaultdict(list))
for item in audit['captor_declarations']:
    if item['needs_review']:
        by_file[item['file']][item['method']].append(item['variable'])
receipt = []
for name, groups in by_file.items():
    path = Path(name)
    source = path.read_text()
    (ev / '33-before-oracle-fixes' / path.relative_to('xacml-core/src/test')).parent.mkdir(parents=True, exist_ok=True)
    (ev / '33-before-oracle-fixes' / path.relative_to('xacml-core/src/test')).write_text(source)
    if path.stem == 'PolicyTest':
        source = source.replace('private Policy policy;', 'private Policy policy;\n\tprivate Rule rule;')
        source = source.replace('Rule rule = Rule.builder("testRule", Effect.PERMIT).build();',
                                'this.rule = Rule.builder("testRule", Effect.PERMIT).build();')
    changes = []
    use_same = False
    for method, a, b in methods(source):
        if method not in groups:
            continue
        checks = []
        for var in groups[method]:
            stem = path.stem
            if stem == 'AttributeDesignatorTest':
                value = ('assertEquals(StatusCodeId.MISSING_ATTRIBUTE, status.getValue().get().getStatusCode().getValue());'
                         if var == 'status' else 'assertEquals(builder.build(), c.getValue());')
            elif stem == 'AttributeSelectorTest':
                value = ('assertEquals(org.xacml4j.v30.StatusCodeId.MISSING_ATTRIBUTE, status.getValue().get().getStatusCode().getValue());'
                         if var == 'status' else 'assertEquals(refKey, c.getValue());')
            elif stem in ('ConditionTest', 'MatchTest'):
                assert var == 'ruleCapture'
                value = 'assertSame(rule, ruleCapture.getValue());'
                use_same = True
            elif stem == 'PolicyTest':
                value = ('assertEquals(java.util.Collections.singletonList(rule), ruleCapture.getValue());'
                         if var == 'ruleCapture' else 'assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());\n\t\tassertSame(policy, contextCapture.getValue().getCurrentPolicy());')
            elif stem == 'PolicySetTest':
                value = ('assertEquals(java.util.Collections.emptyList(), ruleCapture.getValue());'
                         if var == 'ruleCapture' else 'assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());\n\t\tassertSame(policy, contextCapture.getValue().getCurrentPolicySet());')
            elif stem == 'TargetTest':
                value = 'assertSame(context, ' + var + '.getValue());'
                use_same = True
            else:
                raise AssertionError((name, method, var))
            checks.append(value)
            receipt.append({'file':name, 'method':method, 'captor':var, 'added_assertion':value})
        insert = source.rfind('\n', a, b) + 1
        assert not source[insert:b].strip(), (name, method)
        changes.append((insert, b, '\t\t' + '\n\t\t'.join(checks) + '\n\t'))
    source = edits(source, changes)
    if use_same and 'import static org.junit.jupiter.api.Assertions.assertSame;' not in source:
        source = source.replace('import static org.junit.jupiter.api.Assertions.assertEquals;',
                                'import static org.junit.jupiter.api.Assertions.assertEquals;\nimport static org.junit.jupiter.api.Assertions.assertSame;')
    path.write_text(source)
(ev / '33-captor-oracle-fixes.json').write_text(json.dumps(receipt, indent=2) + '\n')
print(json.dumps({'files':len(by_file), 'new_captor_checks':len(receipt),
                  'methods_strengthened':sum(len(v) for v in by_file.values())}, indent=2))
