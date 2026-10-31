from pathlib import Path
import hashlib
import json
import sys

ev = Path(__file__).parent
mode = sys.argv[1]
assert mode in ('before', 'restore')
snapshot = json.loads((ev / '01-snapshot.json').read_text())
for name in snapshot['inventory']:
    owned = Path(name)
    relative = owned.relative_to('xacml-core/src/test')
    saved = ev / '19-jupiter-state' / relative
    if mode == 'before':
        assert not saved.exists(), saved
        saved.parent.mkdir(parents=True, exist_ok=True)
        saved.write_bytes(owned.read_bytes())
        owned.write_bytes((ev / 'before' / relative).read_bytes())
    else:
        owned.write_bytes(saved.read_bytes())
        assert hashlib.sha256(owned.read_bytes()).digest() == hashlib.sha256(saved.read_bytes()).digest()
print(mode, len(snapshot['inventory']), 'owned Java test sources; all other files preserved')
