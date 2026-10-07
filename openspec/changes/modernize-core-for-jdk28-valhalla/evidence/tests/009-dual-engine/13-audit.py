from pathlib import Path
import hashlib
import json
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
scope = json.loads((ev / 'scope-check.json').read_text())
snapshot = json.loads((ev / '01-before.json').read_text())
assert scope['baseline_counts_match'] and scope['baseline_error_identities_match']
assert scope['conformance_case_sequences_match']
assert scope['root_pom_sha256'] == hashlib.sha256((repo / 'pom.xml').read_bytes()).hexdigest()
for name, digest in snapshot['hashes'].items():
    if name != 'pom.xml':
        assert hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
for name in ('summary.md', 'command.txt', 'surefire-summary.txt', 'documentation.md'):
    assert (ev / name).is_file() and (ev / name).stat().st_size > 0, name
offending = []
file_count = 0
for path in ev.rglob('*'):
    if not path.is_file():
        continue
    text = path.read_text(errors='replace')
    for private_path in (str(repo), str(Path.home()), '/' + 'Users/', '/' + 'home/'):
        if private_path in text:
            offending.append(str(path.relative_to(ev)))
            break
    file_count += 1
assert not offending, offending
assert not (repo / snapshot['temporary_probe']).exists()
assert not list((repo / 'xacml-json/target').rglob('*DualEngineTransitionProbeTest*'))
ns = {'m':'http://maven.apache.org/POM/4.0.0'}
root = ET.parse(repo / 'pom.xml').getroot()
lines = (repo / 'pom.xml').read_text().splitlines()
anchors = {}
for number, line in enumerate(lines, 1):
    for key in ('<junit.version>', '<mockito.version>', '<artifactId>junit-jupiter</artifactId>',
                '<artifactId>mockito-core</artifactId>', '<artifactId>mockito-junit-jupiter</artifactId>',
                '<artifactId>junit-bom</artifactId>', '<artifactId>maven-dependency-plugin</artifactId>',
                '<artifactId>maven-surefire-plugin</artifactId>', '<argLine>@{argLine}'):
        if key in line:
            anchors[key] = number
result = {'result':'PASS', 'evidence_files_checked':file_count,
          'no_home_or_checkout_paths':True, 'required_evidence_present':True,
          'protected_files_unchanged':scope['protected_files_checked_unchanged'],
          'root_pom_lines':anchors}
(ev / '13-final-audit.json').write_text(json.dumps(result, indent=2) + '\n')
print(json.dumps(result, indent=2))
