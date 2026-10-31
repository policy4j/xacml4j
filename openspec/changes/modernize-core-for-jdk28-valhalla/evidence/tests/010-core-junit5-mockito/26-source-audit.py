from pathlib import Path
import collections
import hashlib
import json
import re
import subprocess
import sys

ev = Path(__file__).parent
repo = Path.cwd()
snapshot = json.loads((ev / '01-snapshot.json').read_text())
support = (ev / '06-manual-mock-adapt.py').read_text()
exec(support[support.index('def mask'):support.index('def convert_calls')])

def test_methods(source):
    clean = mask(source)
    return [name for name, a, b in methods(source)
            if re.search(r'@(?:Test|ParameterizedTest)\b', clean[max(0, clean.rfind('}', 0, a - 1) + 1):a])]

def headers(source):
    return [block for block in re.findall(r'/\*[\s\S]*?\*/',source) if '#%L' in block and '#L%' in block]

rows = {}
expectations = []
mock_sites = []
for name, item in snapshot['inventory'].items():
    path = Path(name)
    old = (ev / 'before' / path.relative_to('xacml-core/src/test')).read_text()
    current = path.read_text()
    old_names = test_methods(old)
    current_names = test_methods(current)
    assert old_names == current_names, name
    assert headers(old) == headers(current), ('license headers', name)
    before_assertions = collections.Counter(re.findall(r'\b(assert\w*|fail)\s*\(', old))
    after_assertions = collections.Counter(re.findall(r'\b(assert\w*|fail)\s*\(', current))
    assert not before_assertions - after_assertions, (name, before_assertions - after_assertions)
    rows[name] = {'before_methods':len(old_names), 'after_methods':len(current_names),
                  'methods':old_names, 'before_assertions':dict(before_assertions), 'license_blocks':len(headers(old)),
                  'after_assertions':dict(after_assertions), 'license_unchanged':True,
                  'changed_from_task_start':old != current, 'easymock_before':item['easymock'],
                  'legacy_expect':item['expect'], 'legacy_verify':item['verify'],
                  'legacy_captures':item['capture'], 'legacy_strict':item['strict'],
                  'current_verify_sites':len(re.findall(r'\bverify\s*\(',current)),
                  'current_captor_declarations':len(re.findall(r'ArgumentCaptor<',current)),
                  'current_inorder':len(re.findall(r'\binOrder\s*\(',current)),
                  'current_no_more':len(re.findall(r'\bverifyNoMoreInteractions\s*\(',current))}
    clean = mask(old)
    for m in re.finditer(r'\b(?:EasyMock\.)?expect\s*\(', clean):
        start = clean.find('(', m.start(), m.end())
        end = end_pair(old, start)
        finish = end_statement(old, m.start())
        method = next((n for n,a,b in methods(old) if a <= m.start() < b), '<setup>')
        expectations.append({'file':name, 'line':old.count('\n',0,m.start())+1, 'method':method,
                             'expression':old[start+1:end], 'outcome':old[end+1:finish]})
    for m in re.finditer(r'\b(?:createStrictMock|createMock|createNiceMock)\(\s*([\w.]+)\.class',clean):
        mock_sites.append({'file':name, 'line':old.count('\n',0,m.start())+1, 'type':m.group(1)})

protected_changes = []
for name, digest in snapshot['hashes'].items():
    if name.startswith('xacml-core/src/test/'):
        continue
    path = Path(name)
    if not path.is_file() or hashlib.sha256(path.read_bytes()).hexdigest() != digest:
        protected_changes.append(name)
critical_protected_changes = [name for name in protected_changes
                              if name in ('pom.xml', 'xacml-core/pom.xml') or
                              name.startswith(('xacml-core/src/main/',
                                               'xacml-xml-jaxb/src/',
                                               'xacml-conformance-tests/src/test/resources/',
                                               str(ev.parent / '008-baseline-jdk11-jdk21'),
                                               str(ev.parent / '009-dual-engine')))]
assert not critical_protected_changes, critical_protected_changes
allow_shared = '--allow-shared-changes' in sys.argv
assert allow_shared or not protected_changes, protected_changes
if protected_changes:
    (ev / 'shared-tree-changes.json').write_text(json.dumps({
        'coverage':'SHA-256 comparison with task-start; authorship UNATTRIBUTED',
        'changed_paths':protected_changes,
        'critical_protected_changes':critical_protected_changes,
        'lane_writes':'Core test sources and this evidence run only; these outside paths were preserved'}, indent=2) + '\n')

controls = {}
for label, folder in [('task_start', ev / 'before/java'), ('current', Path('xacml-core/src/test/java'))]:
    command = ['ast-grep','scan','--rule',str(ev / '20-legacy-stack-rule.yml'),'--json=compact',str(folder)]
    result = subprocess.run(command, capture_output=True, text=True)
    found = json.loads(result.stdout)
    assert result.returncode == (1 if found else 0), (label,result.returncode,result.stderr)
    controls[label] = {'command':command,'exit':result.returncode,'matches':len(found), 'stderr':result.stderr}
assert controls['task_start']['matches'] > 0 and controls['current']['matches'] == 0

targets = collections.Counter()
declarations = {}
for path in sorted(Path('xacml-core/src/test/java').rglob('*.java')):
    source = path.read_text()
    clean = mask(source)
    for target in re.findall(r'\b(?:mock|spy)\(\s*([\w.]+)\.class',clean):
        simple = target.split('.')[-1]
        targets[simple] += 1
        imported = re.search(r'^import ([\w.]+\.'+re.escape(simple)+r');',source,re.M)
        qualified = imported.group(1) if imported else source.split('package ',1)[1].split(';',1)[0] + '.' + target
        declaration = Path('xacml-core/src/main/java') / Path(qualified.replace('.','/')).with_suffix('.java')
        assert declaration.is_file(), (target, path)
        text = declaration.read_text()
        matched = re.search(r'(?m)^public interface '+re.escape(simple)+r'\b',text)
        assert matched, ('not an interface',target, path)
        declarations[simple] = {'file':str(declaration),'line':text.count('\n',0,matched.start())+1, 'kind':'interface'}
    assert not re.search(r'\b(?:reset|clearInvocations)\s*\(',clean), ('reset',path)
    assert not re.search(r'@Mock\b',clean), ('unaccounted annotation mock',path)

baseline = json.loads((ev / '19-vintage-count-characterization-receipt.json').read_text())
final = json.loads((ev / (sys.argv[1] if len(sys.argv) > 1 else '28-core-final-test-receipt.json')).read_text())
def invocation_counts(receipt):
    out = collections.Counter()
    for cls, report in receipt['reports'].items():
        for method in report['methods']:
            if not method['method']:
                continue
            simple = re.split(r'[\[(]',method['method'],maxsplit=1)[0]
            out[(method.get('class') or cls,simple,method['skipped'])] += 1
    return out
before_invocations = invocation_counts(baseline)
after_invocations = invocation_counts(final)
assert before_invocations == after_invocations, (before_invocations-after_invocations,after_invocations-before_invocations)
assert sum(before_invocations.values()) == 760
historical_source = (ev.parent.parent / 'builds/006-test-stack-inventory/summary.md').read_text()
historical_block = historical_source.split('### EasyMock files: xacml-core',1)[1].split('### EasyMock files: xacml-pdp',1)[0]
historical = re.findall(r'^xacml-core/.*\.java$',historical_block,re.M)
assert len(historical) == 37 and all(rows[name]['easymock_before'] for name in historical)
receipt = {'files':rows,'original_expectations':expectations,'original_mock_sites':mock_sites,
           'historical_inventory_006':historical,'current_easymock_files_before':sum(r['easymock_before'] for r in rows.values()),
           'legacy_ast_control':controls,'current_mock_targets':dict(targets),'interface_declarations':declarations,
           'protected_files_checked':sum(not n.startswith('xacml-core/src/test/') for n in snapshot['hashes']),
           'protected_changes':protected_changes,'test_declarations_before':sum(r['before_methods'] for r in rows.values()),
           'critical_protected_changes':critical_protected_changes,'shared_changes_reported':allow_shared,
           'test_declarations_after':sum(r['after_methods'] for r in rows.values()),
           'named_invocations_before':sum(before_invocations.values()),'named_invocations_after':sum(after_invocations.values()),
           'invocation_parity':True,'license_headers_unchanged':True,'assertion_call_counts_non_decreasing':True,
           'all_current_mock_targets_are_interfaces':True,'reset_or_clear_invocations':False}
assert headers(Path('xacml-core/src/test/java/org/xacml4j/v30/policy/ModelFixtures.java').read_text()) == headers(
    (ev / 'before/java/org/xacml4j/v30/policy/ConditionTest.java').read_text())
serialized = (json.dumps(receipt,indent=2)+'\n').replace(str(repo),'<repo>').replace(str(Path.home()),'<user-home>')
(ev / '26-source-audit.json').write_text(serialized)
print(json.dumps({key:receipt[key] for key in ('current_easymock_files_before','protected_files_checked','protected_changes',
                  'critical_protected_changes','shared_changes_reported',
                  'test_declarations_before','test_declarations_after','named_invocations_before','named_invocations_after',
                  'invocation_parity','license_headers_unchanged','all_current_mock_targets_are_interfaces','reset_or_clear_invocations')},indent=2))
