from pathlib import Path
import json
import sys
import xml.etree.ElementTree as ET

ev = Path(__file__).parent
before = ev / 'before/java'
current = Path('xacml-core/src/test/java')
old = {p.relative_to(before): p.read_bytes() for p in before.rglob('*.java')}
now = {p.relative_to(current): p.read_bytes() for p in current.rglob('*.java')}
assert set(old) <= set(now), 'An original Java test source was removed'
modified = sorted(p for p in old if old[p] != now[p])
added = sorted(set(now) - set(old))
assert len(modified) == 105, len(modified)
assert added == [Path('org/xacml4j/v30/policy/ModelFixtures.java')], added
(ev / 'changed-files.txt').write_text('\n'.join(
    ['# Compared with the exact task-start source snapshot.'] +
    ['modified xacml-core/src/test/java/' + p.as_posix() for p in modified] +
    ['added xacml-core/src/test/java/' + p.as_posix() for p in added]) + '\n')

counts = [0, 0, 0, 0]
for report in Path('xacml-core/target/surefire-reports').glob('TEST-*.xml'):
    root = ET.parse(report).getroot()
    for index, field in enumerate(('tests', 'failures', 'errors', 'skipped')):
        counts[index] += int(root.attrib[field])
assert counts == [760, 0, 0, 2], counts
assert json.loads((ev / (sys.argv[1] if len(sys.argv) > 1 else '28-core-final-test-receipt.json')).read_text())['counts'] == counts
for name in ('summary.md', 'command.txt', 'surefire-summary.txt', 'easymock-mapping.md',
             'expectation-ledger.md', 'per-class-counts.md', 'gate-count-note.md'):
    assert (ev / name).is_file() and (ev / name).stat().st_size > 0, name

home = str(Path.home()).encode()
checkout = str(Path.cwd()).encode()
for path in ev.rglob('*'):
    if path.is_file():
        data = path.read_bytes()
        assert home not in data and checkout not in data, path
result = {'modified_existing_sources': len(modified), 'added_sources': len(added),
          'actual_surefire_xml_counts': counts, 'receipt_matches_actual_xml': True,
          'required_evidence_present': True, 'home_and_checkout_paths_absent': True,
          'numeric_acceptance': 'PENDING_COUNT_DECISION'}
(ev / '31-final-artifact-audit.json').write_text(json.dumps(result, indent=2) + '\n')
print(json.dumps(result, indent=2))
