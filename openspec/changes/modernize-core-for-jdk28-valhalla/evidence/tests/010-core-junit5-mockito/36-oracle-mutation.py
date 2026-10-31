from pathlib import Path
import difflib
import hashlib
import json
import re
import sys

ev = Path(__file__).parent
support = (ev / '06-manual-mock-adapt.py').read_text()
exec(support[support.index('def mask'):support.index('def convert_calls')])
root = Path('xacml-core/src/test/java/org/xacml4j/v30/policy')
cases = {
    'order': ('TargetTest.java', 'testAllMatch', 'Reverse two matching children; the result remains MATCH but the original strict order must fail.'),
    'count': ('function/impl/HigherOrderFunctionsTest.java', 'testMapWithValidArguments', 'Add a fourth input without removing any expected member; times(4) must reject the fifth flag call.'),
    'exception_verify': ('AttributeDesignatorTest.java', 'testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag', 'Throw the expected exception without invoking the designator; assertThrows succeeds and the following resolve verification must fail.'),
    'captured_rule': ('ConditionTest.java', 'testExpressionThrowsRuntimeException', 'Return a different current Rule; result/status/count checks remain satisfied but the captured Rule identity assertion must fail.'),
    'cache': ('PolicyIDReferenceTest.java', 'testEvaluateFailedToResolveReferenceAndReturnNull', 'Simulate an unnecessary second resolver call after cached evaluation; the result remains Indeterminate but the one-call resolver budget must reject it.'),
    'empty_control': ('function/FunctionSpecBuilderTest.java', 'testParameterVarArgMinAndMaxEquals', 'Insert an unexpected invocation before the expected builder exception; the replacement for the empty control verification must reject it.'),
}
state = ev / '36-active-mutation.json'
action = sys.argv[1]
if action == 'restore':
    item = json.loads(state.read_text())
    path = Path(item['file'])
    assert hashlib.sha256(path.read_bytes()).hexdigest() == item['mutant_sha256'], 'Source changed while mutation was active; do not overwrite it'
    data = (ev / item['backup']).read_bytes()
    assert hashlib.sha256(data).hexdigest() == item['original_sha256']
    path.write_bytes(data)
    item['restored_exact_bytes'] = path.read_bytes() == data
    (ev / ('36-' + item['case'] + '-restoration.json')).write_text(json.dumps(item, indent=2) + '\n')
    state.unlink()
    print(json.dumps({'case':item['case'], 'restored_exact_bytes':item['restored_exact_bytes']}))
else:
    assert action == 'mutate' and not state.exists(), 'Restore the current mutant first'
    case = sys.argv[2]
    name, method, intent = cases[case]
    path = root / name
    original = path.read_bytes()
    source = original.decode()
    a, b = next((a,b) for n,a,b in methods(source) if n == method)
    body = source[a:b]
    if case == 'order':
        old = 'matches.add(m1);\n\t\tmatches.add(m2);'
        new = 'matches.add(m2);\n\t\tmatches.add(m1);'
    elif case == 'count':
        old = 'v.add(XacmlTypes.INTEGER.ofAny(30));'
        new = old + '\n\t\tv.add(XacmlTypes.INTEGER.ofAny(40));'
    elif case == 'exception_verify':
        old = '() -> desig.evaluate(context)'
        new = '() -> { throw AttributeReferenceEvaluationException.forDesignator(builder.build()); }'
    elif case == 'captured_rule':
        old = 'when(context.getCurrentRule()).thenReturn(rule);'
        new = 'when(context.getCurrentRule()).thenReturn(Rule.builder("wrong-rule", org.xacml4j.v30.Effect.DENY).build());'
    elif case == 'cache':
        old = 'assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));'
        new = old + '\n\t\tcontext.resolve(ref);'
    else:
        old = 'assertThrows(IllegalArgumentException.class, () -> b.varArg(XacmlTypes.INTEGER, 3, 3).param(XacmlTypes.INTEGER));'
        new = 'impl.invoke(specSameTypeArgs, null, new Expression[0]);\n\t\t' + old
    assert old in body, (case, old)
    mutant = source[:a] + body.replace(old, new, 1) + source[b:]
    if case == 'empty_control':
        mutant = mutant.replace('public void ' + method + '()', 'public void ' + method + '() throws Exception')
    backup = Path('36-mutation-backups') / name
    (ev / backup).parent.mkdir(parents=True, exist_ok=True)
    (ev / backup).write_bytes(original)
    path.write_text(mutant)
    diff = ''.join(difflib.unified_diff(source.splitlines(True), mutant.splitlines(True),
                                       fromfile='before/' + str(path), tofile='mutant/' + str(path)))
    (ev / ('36-' + case + '.patch')).write_text(diff)
    item = {'case':case, 'file':str(path), 'method':method, 'intent':intent,
            'mutation_boundary':'Owned test stimulus/collaborator only; no production source was mutated',
            'backup':backup.as_posix(), 'original_sha256':hashlib.sha256(original).hexdigest(),
            'mutant_sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
    state.write_text(json.dumps(item, indent=2) + '\n')
    print(json.dumps(item, indent=2))
