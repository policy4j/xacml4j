from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
assert not (ev / '01-before.json').exists()
probe = 'xacml-json/src/test/java/org/xacml4j/v30/marshal/json/DualEngineTransitionProbeTest.java'
assert not (repo / probe).exists()
paths = {repo / 'pom.xml', repo / 'AGENTS.md', repo / 'docs/ai/TESTING.md', repo / 'docs/ai/OPENSPEC.md'}
for module in repo.glob('xacml-*'):
    if module.is_dir():
        paths.update(path for path in (module / 'src').rglob('*') if path.is_file())
        if (module / 'pom.xml').exists():
            paths.add(module / 'pom.xml')
change = repo / 'openspec/changes/modernize-core-for-jdk28-valhalla'
paths.update(path for path in change.glob('*.md') if path.is_file())
paths.add(change / 'evidence/README.md')
for prior in ('003-xacml-xml-compile-jdk11','005-reactor-green-jdk11','008-baseline-jdk11-jdk21'):
    paths.update(path for path in (ev.parent / prior).rglob('*') if path.is_file())
hashes = {str(path.relative_to(repo)):hashlib.sha256(path.read_bytes()).hexdigest() for path in sorted(paths)}
(ev / '01-before.json').write_text(json.dumps({'allowed':'pom.xml','temporary_probe':probe,'hashes':hashes},indent=2) + '\n')
(ev / '01-before-pom.xml.txt').write_bytes((repo / 'pom.xml').read_bytes())
ns = {'m':'http://maven.apache.org/POM/4.0.0'}
overrides = {}
for module in repo.glob('xacml-*'):
    pom = module / 'pom.xml'
    if not pom.exists():
        continue
    root = ET.parse(pom).getroot()
    relevant = []
    for node in root.findall('.//m:plugin',ns):
        if node.findtext('m:artifactId',namespaces=ns) == 'maven-surefire-plugin':
            relevant.append(ET.tostring(node,encoding='unicode'))
    for node in root.findall('.//m:dependency',ns):
        if node.findtext('m:groupId',namespaces=ns) in ('org.junit.jupiter','org.junit.vintage','org.mockito'):
            relevant.append(ET.tostring(node,encoding='unicode'))
    overrides[module.name] = relevant
(ev / '01-module-overrides.json').write_text(json.dumps(overrides,indent=2) + '\n')
counts, errors, jdks, case_ids = {}, [], {}, {}
for module in repo.glob('xacml-*'):
    if not (module / 'pom.xml').exists():
        continue
    row = [0,0,0,0]
    for path in (module / 'target/surefire-reports').glob('TEST-*.xml'):
        root = ET.parse(path).getroot()
        row = [a+int(root.get(k,'0')) for a,k in zip(row,('tests','failures','errors','skipped'))]
        props = {node.get('name'):node.get('value') for node in root.findall('./properties/property')}
        jdks[module.name] = {key:props.get(key) for key in ('java.version','java.runtime.version','java.vm.name','java.vendor')}
        for case in root.findall('testcase'):
            error = case.find('error')
            if error is not None:
                trace = error.text or ''
                errors.append({'module':module.name,'class':root.get('name'),'method':case.get('name'),'type':error.get('type'),'message':error.get('message'),'easymock_class_proxy':'org.easymock.internal.ClassProxyFactory.createProxy' in trace and 'MocksControl.createMock' in trace,'trace':trace.replace(str(repo),'<repo>')})
        if module.name == 'xacml-conformance-tests':
            case_ids[root.get('name')] = [case_id for case in root.findall('testcase') for case_id in re.findall(r'Executing test="([^"]+)"',case.findtext('system-out',''))]
    counts[module.name] = row
receipt = {'origin':'Retained target reports from run 008; not a fresh task 2.1 execution','module_counts':counts,'totals':[sum(row[i] for row in counts.values()) for i in range(4)],'jdk_properties':jdks,'errors':errors,'conformance_case_ids':case_ids}
assert receipt['totals'] == [936,0,96,9], receipt['totals']
assert len(errors) == 96 and all(error['easymock_class_proxy'] for error in errors)
(ev / '01-retained-baseline-receipt.json').write_text(json.dumps(receipt,indent=2).replace(str(repo),'<repo>') + '\n')
status = subprocess.run(['rtk','git','status','--short'],capture_output=True,text=True)
assert status.returncode == 0
(ev / '01-before-git-status.txt').write_text(status.stdout.replace(str(repo),'<repo>'))
print(f'Snapshot {len(hashes)} files; root POM plus temporary probe only; retained baseline {receipt["totals"]}; {len(errors)} EasyMock class-proxy errors')
