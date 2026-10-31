from pathlib import Path
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
name = sys.argv[1]
path = ev / name
raw = re.sub(r'\x1b\[[0-9;]*[A-Za-z]', '', path.read_text(errors='replace'))
raw = raw.replace(str(repo), '<repo>').replace(str(Path.home()), '<user-home>')
path.write_text(raw)
names = sorted(set(re.findall(r'\[INFO\] Running (org\.xacml4j\.[\w.$]+)(?:\s|$)', raw)))
counts = re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)(?![^\n]* - in )', raw)
reports, problems = {}, []
for cls in names:
    report_path = repo / 'xacml-core/target/surefire-reports' / ('TEST-' + cls + '.xml')
    root = ET.parse(report_path).getroot()
    props = {p.get('name'):p.get('value') for p in root.findall('./properties/property')}
    assert props.get('java.version') == '21.0.6', cls
    row = [int(root.get(k,'0')) for k in ('tests','failures','errors','skipped')]
    methods = []
    for case in root.findall('testcase'):
        item = {'method':case.get('name'), 'class':case.get('classname'), 'skip_reason':case.findtext('skipped'), 'skipped':case.find('skipped') is not None}
        methods.append(item)
        for kind in ('failure','error'):
            node = case.find(kind)
            if node is not None:
                trace = (node.text or '').replace(str(repo),'<repo>').replace(str(Path.home()),'<user-home>')
                problems.append({'class':cls,'method':case.get('name'),'kind':kind,'type':node.get('type'),'message':node.get('message'),'trace':trace})
    reports[cls] = {'counts':row,'methods':methods, 'jdk':props.get('java.runtime.version')}
total = [sum(item['counts'][i] for item in reports.values()) for i in range(4)]
if names:
    assert list(map(int,counts[-1])) == total, (counts[-1],total)
compiler = [dict(zip(('file','line','column','message'), parts)) for parts in sorted(set(re.findall(r'\[ERROR\] <repo>/(.*\.java):\[(\d+),(\d+)\] ([^\n]+)',raw)))]
receipt = {'log':name,'build_success':'BUILD SUCCESS' in raw and 'BUILD FAILURE' not in raw,'counts':total,'tests_executed':bool(names),
           'reports':reports,'problems':problems,'compiler_diagnostics':compiler}
(ev / (path.stem + '-receipt.json')).write_text(json.dumps(receipt,indent=2)+'\n')
print(json.dumps({k:receipt[k] for k in ('log','build_success','counts','tests_executed','compiler_diagnostics')},indent=2))
