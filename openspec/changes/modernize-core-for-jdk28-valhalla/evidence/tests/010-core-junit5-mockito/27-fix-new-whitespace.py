from pathlib import Path
import json
import re
import subprocess

ev = Path(__file__).parent
result = subprocess.run(['rtk','git','diff','--check','--','xacml-core/src/test'],capture_output=True,text=True)
assert result.returncode in (0,2), result.stderr
changes = {}
for name, line_number, reason in re.findall(r'^(xacml-core/src/test/[^\n]+\.java):(\d+): (trailing whitespace|space before tab in indent)\.',result.stdout,re.M):
    changes.setdefault(name, {})[int(line_number)] = reason
for name, lines in changes.items():
    path = Path(name)
    source = path.read_text().splitlines(keepends=True)
    license_start = next(i for i,s in enumerate(source) if '#%L' in s)
    license_end = next(i for i,s in enumerate(source) if '#L%' in s)
    for number, reason in lines.items():
        index = number - 1
        assert not license_start <= index <= license_end, ('license',name,number)
        value = source[index].rstrip('\r\n')
        if reason == 'trailing whitespace':
            value = value.rstrip(' \t')
        else:
            value = re.sub(r'^ +(?=\t)', '', value)
        source[index] = value + '\n'
    path.write_text(''.join(source))
(ev / '27-whitespace-fixes.json').write_text(json.dumps(changes,indent=2)+'\n')
print('Fixed only diff-check-reported added lines in',len(changes),'owned files; license blocks preserved')
