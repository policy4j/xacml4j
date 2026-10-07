from pathlib import Path
import hashlib
import json
import re

repo = Path.cwd()
ev = Path(__file__).parent
snapshot = json.loads((ev / '01-snapshot.json').read_text())
assert all((repo/name).is_file() and hashlib.sha256((repo/name).read_bytes()).hexdigest()==digest for name,digest in snapshot['hashes'].items())
patch = (repo / 'xacml-core/target/rewrite/rewrite.patch').read_text()
patch = patch.replace(str(repo),'<repo>').replace(str(Path.home()),'<user-home>')
headers = re.findall(r'^\+\+\+ b/(.*)$',patch,re.M)
assert headers and all(p.startswith('xacml-core/src/test/java/') for p in headers),headers
(ev / '03b-rewrite-dry-run.patch').write_text(patch)
stats = {}
for section in patch.split('diff --git ')[1:]:
    match = re.search(r'^\+\+\+ b/(.*)$', section, re.M)
    if not match:
        continue
    name = match.group(1)
    added = [s[1:] for s in section.splitlines() if s.startswith('+') and not s.startswith('+++')]
    removed = [s[1:] for s in section.splitlines() if s.startswith('-') and not s.startswith('---')]
    stats[name] = {'added':len(added),'removed':len(removed),
        'removed_verification_lines':[s.strip() for s in removed if re.search(r'\bverify\s*\(',s)],
        'added_verification_lines':[s.strip() for s in added if re.search(r'\bverify\s*\(',s)],
        'assertions_removed_lexical':sum(len(re.findall(r'\bassert\w*\s*\(',s)) for s in removed),
        'assertions_added_lexical':sum(len(re.findall(r'\bassert\w*\s*\(',s)) for s in added)}
assert len(stats)==len(headers),(len(stats),len(headers))
summary = {'plugin':'org.openrewrite.maven:rewrite-maven-plugin:6.46.1',
    'recipes':'org.openrewrite.recipe:rewrite-testing-frameworks:3.44.0',
    'paths':stats,'files':len(stats),'all_paths_in_scope':True,'working_tree_unchanged':True,
    'added_lines':sum(s['added'] for s in stats.values()),'removed_lines':sum(s['removed'] for s in stats.values()),
    'verification_lines_removed':sum(len(s['removed_verification_lines']) for s in stats.values()),
    'verification_lines_added':sum(len(s['added_verification_lines']) for s in stats.values())}
(ev / '03b-dry-run-summary.json').write_text(json.dumps(summary,indent=2)+'\n')
print(json.dumps({k:v for k,v in summary.items() if k!='paths'},indent=2))

