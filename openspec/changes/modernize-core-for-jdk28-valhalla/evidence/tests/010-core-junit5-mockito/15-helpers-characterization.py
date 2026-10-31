from pathlib import Path
import hashlib
import sys

ev = Path(__file__).parent
mode = sys.argv[1]
assert mode in ('before', 'restore')
names = ['TestFunctions.java', 'TestInstanceFunctions.java']
if len(sys.argv) > 2 and sys.argv[2] == 'with-test':
    names.append('BagFunctionsTest.java')
for name in names:
    relative = Path('java/org/xacml4j/v30/policy/function/impl') / name
    owned = Path('xacml-core/src/test') / relative
    saved = ev / '15-jupiter-helpers' / name
    if mode == 'before':
        saved.parent.mkdir(parents=True, exist_ok=True)
        if saved.exists():
            assert owned.read_bytes() == saved.read_bytes()
        else:
            saved.write_bytes(owned.read_bytes())
        owned.write_bytes((ev / 'before' / relative).read_bytes())
    else:
        owned.write_bytes(saved.read_bytes())
        assert hashlib.sha256(owned.read_bytes()).digest() == hashlib.sha256(saved.read_bytes()).digest()
    print(mode, owned.as_posix())
