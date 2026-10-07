from pathlib import Path
import re

base = Path('xacml-core/src/test/java/org/xacml4j/v30/policy')
for name in ('MatchAllOfTest', 'MatchAnyOfTest', 'TargetTest', 'RuleTest', 'PolicyTest', 'PolicySetTest'):
    path = base / (name + '.java')
    source = path.read_text().replace('import org.xacml4j.v30.policy.AttributeReference;\n', '')
    if 'import org.xacml4j.v30.Expression;' not in source:
        source = source.replace('import org.xacml4j.v30.EvaluationContext;',
                                'import org.xacml4j.v30.EvaluationContext;\nimport org.xacml4j.v30.Expression;')
    source = re.sub(r'\bAttributeReference (m\d+Reference|targetReference)\b', r'Expression \1', source)
    source = re.sub(r'\b(m\d+|target)Reference\b', r'\1Expression', source)
    path.write_text(source)
print('Replaced class mocks with expression-interface boundaries in six composition tests')
