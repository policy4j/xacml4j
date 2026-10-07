from pathlib import Path
import difflib
import hashlib
import json
import time

repo = Path.cwd()
ev = Path(__file__).parent
snapshot = json.loads((ev / '01-before.json').read_text())
protected = {name:digest for name,digest in snapshot['hashes'].items() if name != 'pom.xml'}
for name,digest in protected.items():
    assert (repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest,name
expected = {'05-dual-engine-green':[6,0,0,1],'06-jupiter-only-green':[1,0,0,0],'07-vintage-only-green':[5,0,0,1]}
for prefix,counts in expected.items():
    receipt = json.loads((ev / (prefix + '-receipt.json')).read_text())
    assert receipt['build_success'] and receipt['totals'] == counts and not receipt['compiler_diagnostics']
    assert receipt['root_pom_sha256'] == hashlib.sha256((repo / 'pom.xml').read_bytes()).hexdigest()
probe = repo / snapshot['temporary_probe']
assert probe.is_file() and not probe.is_symlink() and str(probe.relative_to(repo)) not in snapshot['hashes']
probe_text = probe.read_text()
assert '@ExtendWith(MockitoExtension.class)' in probe_text and 'static final class FinalCollaborator' in probe_text
assert 'Mockito must be loaded as a startup agent on JDK 21' in probe_text
probe_hash = hashlib.sha256(probe.read_bytes()).hexdigest()
(ev / '08-probe-source.java.txt').write_text(probe_text)
probe.unlink()
assert not probe.exists()
old = (ev / '01-before-pom.xml.txt').read_text()
new = (repo / 'pom.xml').read_text()
(ev / '08-root-pom-fixes.patch').write_text(''.join(difflib.unified_diff(old.splitlines(keepends=True),new.splitlines(keepends=True),fromfile='a/pom.xml',tofile='b/pom.xml')))
receipt = {'temporary_probe':snapshot['temporary_probe'],'probe_source_sha256':probe_hash,'probe_removed':True,'protected_files_unchanged':len(protected),'root_pom_sha256':hashlib.sha256((repo / 'pom.xml').read_bytes()).hexdigest(),'full_gate_start_ns':time.time_ns()}
(ev / '08-before-full-gate.json').write_text(json.dumps(receipt,indent=2) + '\n')
print(f'Throwaway source retained as evidence text and deleted; {len(protected)} protected files unchanged; full clean gate ready')
