from collections import Counter
from pathlib import Path
import difflib
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
def read_json(name):
    return json.loads((ev / name).read_text())
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def normalize(value):
    return value.replace(str(repo), '<repo>').replace(str(Path.home()), '<user-home>')
def git(*args):
    result = subprocess.run(['rtk', 'git', *args], capture_output=True, text=True)
    assert result.returncode == 0, normalize(result.stderr)
    return normalize(result.stdout)
def write_json(name, value):
    (ev / name).write_text(normalize(json.dumps(value, indent=2)) + '\n')

before = read_json('01-before.json')
marker = read_json('08-before-full-gate.json')
baseline = read_json('01-retained-baseline-receipt.json')
full = read_json('09-full-reactor-clean-verify-jdk21-receipt.json')
protected = {name: sha for name, sha in before['hashes'].items() if name != 'pom.xml'}
changed = [name for name, sha in protected.items() if not (repo / name).is_file() or digest(repo / name) != sha]
assert not changed, changed
current_sources = set()
for module in repo.glob('xacml-*'):
    if module.is_dir():
        current_sources.update(str(path.relative_to(repo)) for path in (module / 'src').rglob('*') if path.is_file())
assert not current_sources.difference(before['hashes']), sorted(current_sources.difference(before['hashes']))
assert not (repo / before['temporary_probe']).exists()
assert not list((repo / 'xacml-json/target/test-classes').rglob('*DualEngineTransitionProbeTest*'))
assert marker['probe_removed']
assert digest(repo / 'pom.xml') == marker['root_pom_sha256'] == full['root_pom_sha256']
actual_patch = ''.join(difflib.unified_diff(
    (ev / '01-before-pom.xml.txt').read_text().splitlines(keepends=True),
    (repo / 'pom.xml').read_text().splitlines(keepends=True),
    fromfile='a/pom.xml', tofile='b/pom.xml'))
assert actual_patch == (ev / '08-root-pom-fixes.patch').read_text()

root = ET.parse(repo / 'pom.xml').getroot()
def text(node, path):
    return node.findtext(path, namespaces=ns)
props = root.find('m:properties', ns)
assert text(props, 'm:junit.version') == '5.14.4'
assert text(props, 'm:mockito.version') == '5.24.0'
assert text(props, 'm:argLine') == ''
deps = {(text(node, 'm:groupId'), text(node, 'm:artifactId')): node
        for node in root.findall('m:dependencies/m:dependency', ns)}
for key in [('org.junit.jupiter', 'junit-jupiter'), ('org.mockito', 'mockito-core'), ('org.mockito', 'mockito-junit-jupiter')]:
    assert text(deps[key], 'm:scope') == 'test', key
for key in [('org.junit.vintage', 'junit-vintage-engine'), ('org.easymock', 'easymock'), ('org.objenesis', 'objenesis')]:
    assert key in deps, key
managed = {(text(node, 'm:groupId'), text(node, 'm:artifactId')): node
           for node in root.findall('m:dependencyManagement/m:dependencies/m:dependency', ns)}
assert text(managed[('org.junit', 'junit-bom')], 'm:scope') == 'import'
assert text(managed[('org.junit', 'junit-bom')], 'm:type') == 'pom'
plugins = {text(node, 'm:artifactId'): node for node in root.findall('m:build/m:plugins/m:plugin', ns)}
assert text(plugins['maven-surefire-plugin'], 'm:version') == '3.0.0'
assert text(plugins['maven-surefire-plugin'], 'm:configuration/m:argLine') == '@{argLine} -javaagent:${org.mockito:mockito-core:jar}'
assert text(plugins['maven-dependency-plugin'], 'm:version') == '3.11.0'
assert text(plugins['maven-dependency-plugin'], 'm:executions/m:execution/m:goals/m:goal') == 'properties'
old_root = ET.fromstring((ev / '01-before-pom.xml.txt').read_text())
old_plugins = {text(node, 'm:artifactId'): node for node in old_root.findall('m:build/m:plugins/m:plugin', ns)}
for key in ('maven-compiler-plugin', 'license-maven-plugin'):
    assert ET.tostring(plugins[key]) == ET.tostring(old_plugins[key]), key
assert ET.tostring(plugins['maven-surefire-plugin'].find('m:configuration/m:excludes', ns)) == ET.tostring(old_plugins['maven-surefire-plugin'].find('m:configuration/m:excludes', ns))
module_overrides = read_json('01-module-overrides.json')
assert all('maven-surefire-plugin' not in item for items in module_overrides.values() for item in items)

red = read_json('02-missing-dependencies-red-receipt.json')
assert red['root_pom_matches_before'] and not red['tests_executed'] and len(red['compiler_diagnostics']) == 16
assert all(item['file'] == before['temporary_probe'] for item in red['compiler_diagnostics'])
startup_red = read_json('04-startup-agent-red-receipt.json')
assert startup_red['totals'] == [6, 1, 0, 1]
assert len(startup_red['problems']) == 1
assert startup_red['problems'][0]['method'] == 'jupiterMocksFinalClassWithStartupAgent'
assert 'Mockito must be loaded as a startup agent on JDK 21' in startup_red['problems'][0]['message']
assert 'Dynamic loading of agents will be disallowed' in (ev / '04-startup-agent-red.log').read_text()
probe_class = 'org.xacml4j.v30.marshal.json.DualEngineTransitionProbeTest'
green_expected = {'05-dual-engine-green': [6, 0, 0, 1],
                  '06-jupiter-only-green': [1, 0, 0, 0],
                  '07-vintage-only-green': [5, 0, 0, 1]}
for stem, counts in green_expected.items():
    receipt = read_json(stem + '-receipt.json')
    assert receipt['build_success'] and receipt['totals'] == counts
    assert not receipt['compiler_diagnostics'] and receipt['root_pom_sha256'] == full['root_pom_sha256']
    log = (ev / (stem + '.log')).read_text()
    assert 'JUnitPlatformProvider' in log
    assert 'Dynamic loading of agents will be disallowed' not in log
    assert 'Mockito is currently self-attaching' not in log
dual_names = set(read_json('05-dual-engine-green-receipt.json')['reports']['xacml-json'])
jupiter_names = set(read_json('06-jupiter-only-green-receipt.json')['reports']['xacml-json'])
vintage_names = set(read_json('07-vintage-only-green-receipt.json')['reports']['xacml-json'])
assert jupiter_names == {probe_class} and probe_class not in vintage_names
assert dual_names == jupiter_names | vintage_names

assert full['build_success'] and not full['compiler_diagnostics'] and not full['probe_present']
assert full['totals'] == baseline['totals'] == [936, 0, 96, 9]
for module, counts in baseline['module_counts'].items():
    assert full['module_counts'].get(module, [0, 0, 0, 0]) == counts, module
problem_key = lambda p: (p['module'], p['class'], p['method'], p['type'], p['message'])
assert sorted(map(problem_key, full['problems'])) == sorted(map(problem_key, baseline['errors']))
assert len(full['problems']) == 96
assert all(p['kind'] == 'error' and p['module'] == 'xacml-core' and p['easymock_class_proxy']
           and p['type'] == 'java.lang.IllegalArgumentException' and p['message'] == 'Could not create type'
           for p in full['problems'])
assert not any('InaccessibleObjectException' in p['trace'] or 'IllegalAccessError' in p['trace'] for p in full['problems'])
fresh_reports = 0
for module, reports in full['reports'].items():
    for class_name, saved in reports.items():
        path = repo / module / 'target/surefire-reports' / ('TEST-' + class_name + '.xml')
        assert path.stat().st_mtime_ns >= marker['full_gate_start_ns'], str(path.relative_to(repo))
        report = ET.parse(path).getroot()
        assert [int(report.get(key, '0')) for key in ('tests', 'failures', 'errors', 'skipped')] == saved['counts']
        java_version = report.find("./properties/property[@name='java.version']").get('value')
        assert java_version == '21.0.6', (class_name, java_version)
        fresh_reports += 1
    assert set(reports) == {ET.parse(path).getroot().get('name') for path in (repo / module / 'target/surefire-reports').glob('TEST-*.xml')}

conformance = {}
for class_name, entry in full['conformance'].items():
    assert entry['case_ids'] == baseline['conformance_case_ids'][class_name], class_name
    assert not entry['failed_case_ids'] and entry['counts'][1:] == [0, 0, 0]
    conformance[class_name] = {'surefire_counts': entry['counts'],
                             'executions': len(entry['case_ids']),
                             'unique_case_ids': len(set(entry['case_ids'])),
                             'case_ids_match_run_008_in_order': True,
                             'failed_case_ids': [],
                             'case_ids': entry['case_ids']}
for name in ('Xacml20ConformanceTest', 'Xacml30ConformanceTest'):
    row = conformance['org.xacml4j.v20.' + name]
    assert row['executions'] == 347 and row['unique_case_ids'] == 346
    assert Counter(row['case_ids'])['IIIG006'] == 2
assert conformance['org.xacml4j.v20.RSA2008InteropTest']['surefire_counts'] == [17, 0, 0, 0]
assert conformance['org.xacml4j.v20.XacmlExampleTest']['surefire_counts'] == [2, 0, 0, 0]
assert conformance['org.xacml4j.v20.Xacml20ConformanceAttributeResolverTest']['surefire_counts'] == [2, 0, 0, 0]
write_json('conformance-parity.json', conformance)
write_json('easymock-error-parity.json', {'identities_match_run_008': True,
    'count': 96, 'other_error_categories': 0,
    'errors_by_class': dict(sorted(Counter(p['class'] for p in full['problems']).items())),
    'errors': [{'class': p['class'], 'method': p['method'], 'type': p['type'], 'message': p['message'],
                'easymock_class_proxy': True} for p in sorted(full['problems'], key=problem_key)]})

tree_receipt = read_json('10b-test-dependency-tree-receipt.json')
assert tree_receipt['build_success'] and not tree_receipt['tests_executed'] and not tree_receipt['compiler_diagnostics']
tree = (ev / '10b-test-dependency-tree.log').read_text()
for artifact in ('org.junit.jupiter:junit-jupiter:jar:5.14.4:test',
                 'org.junit.jupiter:junit-jupiter-api:jar:5.14.4:test',
                 'org.junit.jupiter:junit-jupiter-engine:jar:5.14.4:test',
                 'org.junit.vintage:junit-vintage-engine:jar:5.14.4:test',
                 'org.mockito:mockito-core:jar:5.24.0:test',
                 'org.mockito:mockito-junit-jupiter:jar:5.24.0:test',
                 'junit:junit:jar:4.13.2:test', 'org.easymock:easymock:jar:5.1.0:test',
                 'org.objenesis:objenesis:jar:3.3:test'):
    assert tree.count(artifact) == 3, artifact

summary_lines = ['Task 2.1 Surefire evidence; count order: Tests / Failures / Errors / Skipped.',
                 'All paths normalized. Full gate used maven.test.failure.ignore=true.',
                 'Per-engine filters are command-line proof controls; no permanent filters were added.', '']
for name in ('02-missing-dependencies-red', '03-startup-agent-red', '04-startup-agent-red',
             '05-dual-engine-green', '06-jupiter-only-green', '07-vintage-only-green',
             '09-full-reactor-clean-verify-jdk21', '10-test-dependency-tree', '10b-test-dependency-tree'):
    summary_lines.extend(['=== ' + name + '.log ==='])
    log = (ev / (name + '.log')).read_text()
    in_results = False
    module = 'root'
    for line in log.splitlines():
        plugin_marker = re.search(r'--- .* @ ([^ ]+) ---', line)
        if plugin_marker:
            module = plugin_marker.group(1)
        if ' Results:' in line:
            summary_lines.append('Module: ' + module)
            in_results = True
        if in_results:
            if line.startswith('[INFO] ---'):
                in_results = False
            else:
                summary_lines.append(line)
        elif ('Tests run:' in line or 'JUnitPlatformProvider' in line or '[INFO] Running ' in line
              or 'BUILD SUCCESS' in line or 'BUILD FAILURE' in line):
            summary_lines.append(line)
    receipt = read_json(name + '-receipt.json')
    summary_lines.append('Parsed per-module counts: ' + json.dumps(receipt['module_counts'], sort_keys=True))
    summary_lines.append('')
summary_lines.extend(['=== Full-gate 96-error classification ===',
    'Every error matches run 008 class/method/type/message and has EasyMock ClassProxyFactory.createProxy plus MocksControl.createMock.',
    'All are java.lang.IllegalArgumentException: Could not create type. No failures or other error categories.'])
summary_lines.extend(p['class'] + '#' + p['method'] for p in sorted(full['problems'], key=problem_key))
summary_lines.extend(['', '=== Conformance cases (distinct from Surefire methods) ===',
    'OASIS 2.0: 347 executions, 346 unique case ids, zero failures.',
    'OASIS 3.0: 347 executions, 346 unique case ids, zero failures.',
    'IIIG006 runs twice in each suite, unchanged from run 008.',
    'RSA 2008: 17 cases; examples: 2; resolver characterization methods: 2; all pass.',
    'Failed case ids: none. Case-id sequence matches run 008; source, fixtures, exclusions and expected results unchanged.'])
(ev / 'surefire-summary.txt').write_text(normalize('\n'.join(summary_lines)) + '\n')

branch = git('branch', '--show-current').strip()
head = git('rev-parse', 'HEAD').strip()
assert branch == 'integration/modernize-core-jdk28-valhalla'
assert head == 'befdd116bd11385ac8d007732fa8e4d347af3143'
assert not git('diff', '--cached', '--name-only').strip()
assert not git('diff', '--check', '--', 'pom.xml').strip()
status = git('status', '--short')
(ev / '14-final-git-status.txt').write_text(status)
write_json('scope-check.json', {'branch': branch, 'head': head, 'root_pom_sha256': digest(repo / 'pom.xml'),
    'persistent_project_files_changed_by_task': ['pom.xml'],
    'protected_files_checked_unchanged': len(protected), 'changed_protected_files': changed,
    'new_source_files': [], 'temporary_probe_removed': True, 'temporary_probe_classes_removed_by_clean': True,
    'module_poms_unchanged': True, 'source_and_fixture_bytes_unchanged': True,
    'prior_runs_003_005_008_unchanged': True, 'fresh_full_gate_reports': fresh_reports,
    'all_full_gate_report_jdks': '21.0.6', 'staged_files': [], 'root_diff_check': 'PASS',
    'baseline_counts_match': True, 'baseline_error_identities_match': True,
    'conformance_case_sequences_match': True})
print(f'PASS: {len(protected)} protected files unchanged; {fresh_reports} fresh JDK 21 reports; 936/0/96/9 baseline parity; all 96 EasyMock identities and conformance case sequences match; probe source/classes absent.')

