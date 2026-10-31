from pathlib import Path
import hashlib
import json
import re
import subprocess

repo = Path.cwd()
ev = Path(__file__).parent
assert not (ev / '01-snapshot.json').exists()
owned = repo / 'xacml-core/src/test'
paths = {repo / 'pom.xml', repo / 'AGENTS.md', repo / 'docs/ai/TESTING.md', repo / 'docs/ai/OPENSPEC.md'}
for module in repo.glob('xacml-*'):
    if module.is_dir():
        paths.update(p for p in (module / 'src').rglob('*') if p.is_file())
        if (module / 'pom.xml').exists():
            paths.add(module / 'pom.xml')
change = ev.parent.parent.parent
paths.update(p for p in change.glob('*.md') if p.is_file())
paths.add(change / 'evidence/README.md')
for prior in ('008-baseline-jdk11-jdk21','009-dual-engine'):
    paths.update(p for p in (ev.parent / prior).rglob('*') if p.is_file())
hashes = {str(p.relative_to(repo)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(paths)}
inventory = {}
for p in sorted(owned.rglob('*.java')):
    source = p.read_text()
    name = str(p.relative_to(repo))
    (ev / 'before' / p.relative_to(owned)).parent.mkdir(parents=True,exist_ok=True)
    (ev / 'before' / p.relative_to(owned)).write_bytes(p.read_bytes())
    inventory[name] = {'tests_lexical':len(re.findall(r'@Test\b',source)),
        'junit_imports':[line.strip() for line in source.splitlines() if line.strip().startswith('import') and 'org.junit.' in line],
        'easymock': 'org.easymock' in source,
        'strict':len(re.findall(r'\b(?:createStrictControl|createStrictMock)\s*\(',source)),
        'expect':len(re.findall(r'\bexpect\s*\(',source)),
        'verify':len(re.findall(r'\bverify\s*\(',source)),
        'capture':len(re.findall(r'Capture<',source)),
        'control':len(re.findall(r'IMocksControl',source)),
        'assert_calls':re.findall(r'\b(assert\w*|fail)\s*\(',source),
        'ignore_lines':[line.strip() for line in source.splitlines() if '@Ignore' in line]}
receipt = {'hashes':hashes,'owned':'xacml-core/src/test/','inventory':inventory}
(ev / '01-snapshot.json').write_text(json.dumps(receipt,indent=2)+'\n')
previous = json.loads((ev.parent / '009-dual-engine/09-full-reactor-clean-verify-jdk21-receipt.json').read_text())
core = {key:previous[key] for key in ('jdk_properties','root_pom_sha256')}
core['reports'] = previous['reports']['xacml-core']
core['counts'] = previous['module_counts']['xacml-core']
assert core['counts'] == [762,0,96,4]
(ev / '01-retained-core-baseline.json').write_text(json.dumps(core,indent=2)+'\n')
status = subprocess.run(['rtk','git','status','--short'],capture_output=True,text=True)
assert status.returncode == 0
(ev / '01-before-git-status.txt').write_text(status.stdout)
print(f'Snapshot {len(hashes)} files; {len(inventory)} core Java test sources; {sum(v["tests_lexical"] for v in inventory.values())} tests; {sum(v["easymock"] for v in inventory.values())} EasyMock files; retained core counts {core["counts"]}')

