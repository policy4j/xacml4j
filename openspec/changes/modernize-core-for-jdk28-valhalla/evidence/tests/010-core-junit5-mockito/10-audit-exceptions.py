from pathlib import Path
import json
import re

ev = Path(__file__).parent
exec((ev / '06-manual-mock-adapt.py').read_text().split('skipped=')[0])
report = []
for original in sorted((ev / 'before/java').rglob('*.java')):
    old = original.read_text()
    current_path = Path('xacml-core/src/test/java') / original.relative_to(ev / 'before/java')
    current = current_path.read_text()
    current_methods = {name: current[a:b] for name, a, b in methods(current)}
    for name, a, b in methods(old):
        annotation = old[old.rfind('@Test', 0, a):a]
        if '}' in mask(annotation):
            continue
        expected = re.search(r'@Test\s*\(\s*expected\s*=\s*([\w.]+)\.class\s*\)', annotation)
        if not expected:
            continue
        body = current_methods[name]
        checked = bool(re.search(r'assertThrows\(\s*' + re.escape(expected.group(1)) + r'\.class', body))
        item = {'file': current_path.as_posix(), 'method': name, 'expected': expected.group(1), 'checked': checked}
        report.append(item)
        if not checked:
            print(json.dumps(item))
(ev / '10-exception-audit.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps({'expected_exception_methods': len(report), 'missing': sum(not item['checked'] for item in report)}))
