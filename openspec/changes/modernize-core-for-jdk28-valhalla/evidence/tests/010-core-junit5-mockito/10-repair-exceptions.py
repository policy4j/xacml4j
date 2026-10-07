from pathlib import Path
import json
import re

ev = Path(__file__).parent
support = (ev / '06-manual-mock-adapt.py').read_text()
exec(support[support.index('def mask'):support.index('def convert_calls')])
for item in json.loads((ev / '10-exception-audit.json').read_text()):
    if item['checked']:
        continue
    path = Path(item['file'])
    source = path.read_text()
    name, a, b = next(method for method in methods(source) if method[0] == item['method'])
    body = source[a:b]
    expected = item['expected']
    if name == 'testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag':
        body = body.replace('try{\n\t\t\tdesig.evaluate(context);\n\t\t}catch(AttributeReferenceEvaluationException e){',
                            'AttributeReferenceEvaluationException e = assertThrows(\n\t\t\t\tAttributeReferenceEvaluationException.class, () -> desig.evaluate(context));')
        body = body.replace('\n\t\t\tthrow e;\n\t\t}', '')
        body = body.replace('\n\t\t\t', '\n\t\t')
    elif name == 'testNOfFunctionInderterminate':
        start = body.index('assertEquals(')
        end = end_statement(body, start)
        body = edits(body, [(start, end, 'assertThrows(IllegalArgumentException.class, () -> {\n\t\t\t' + body[start:end] + '\n\t\t});')])
    else:
        trigger = ('desig.evaluate(context)' if 'AttributeDesignator' in item['file'] else
                   'ref.evaluate(context)' if 'AttributeSelector' in item['file'] else
                   'exp.evaluate(context)' if 'ObligationExpression' in item['file'] else
                   'f0.invoke(spec, context, p)' if 'DefaultFunctionFunctionInvocation' in item['file'] else
                   'spec.invokeWithList(context, params)')
        assert body.count(trigger + ';') == 1, item
        body = body.replace(trigger + ';', 'assertThrows(' + expected + '.class, () -> ' + trigger + ');')
    path.write_text(source[:a] + body + source[b:])
