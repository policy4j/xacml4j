from pathlib import Path
import json
import re

ev = Path(__file__).parent
support = (ev / '06-manual-mock-adapt.py').read_text()
exec(support[support.index('def mask'):support.index('def convert_calls')])
removed_redundant_order = []
for path in sorted(Path('xacml-core/src/test/java').rglob('*.java')):
    source = path.read_text()
    changes = []
    for name, a, b in methods(source):
        body = source[a:b]
        if 'InOrder order =' in body and 'order.verify(' not in body:
            body = re.sub(r'^[ \t]*InOrder order = inOrder\([^;]+\);\n', '', body, flags=re.M)
            body = re.sub(r'^[ \t]*order\.verifyNoMoreInteractions\(\);\n', '', body, flags=re.M)
            changes.append((a, b, body))
            removed_redundant_order.append({'file':path.as_posix(), 'method':name, 'retained':'verifyNoMoreInteractions'})
    source = edits(source, changes)
    if path.name == 'RuleTest.java':
        source = source.replace('when(targetReference.evaluate(ruleContext)).thenThrow(new EvaluationException(b.build(), "Bad"));',
                                'EvaluationException failure = new EvaluationException(b.build(), "Bad");\n\t\twhen(targetReference.evaluate(ruleContext)).thenAnswer(invocation -> {\n\t\t\truleContext.setEvaluationStatus(failure.getStatus());\n\t\t\tthrow failure;\n\t\t});')
    if path.name in ('DurationFunctionIdentifierTest.java', 'HigherOrderFunctionIdentifierTest.java'):
        source = source.replace('\n\t\t}\n\t}\n\n\tprivate static ValueExp evaluate(',
                                '\n\t\t\tverify(context, atLeastOnce()).isValidateFuncParamsAtRuntime();\n\t\t\tverifyNoMoreInteractions(context);\n\t\t}\n\t}\n\n\tprivate static ValueExp evaluate(')
    if source != path.read_text():
        path.write_text(source)
(ev / '13-fixture-adjustment.json').write_text(json.dumps(removed_redundant_order, indent=2) + '\n')
