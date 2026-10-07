from pathlib import Path
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
name = sys.argv[1]
log_path = ev / name
text = re.sub(r'\x1b\[[0-9;]*[A-Za-z]','',log_path.read_text(errors='replace'))
text = text.replace(str(repo),'<repo>').replace(str(Path.home()),'<user-home>')
log_path.write_text(text)
module, counts, classes, diagnostics = 'unknown', {}, {}, {}
for line in text.splitlines():
    marker = re.search(r'--- .* @ ([^ ]+) ---',line)
    if marker:
        module = marker.group(1)
    count = re.search(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)',line)
    if count and ' - in ' not in line:
        counts[module] = [int(value) for value in count.groups()]
    running = re.search(r'\[INFO\] Running ([\w.$]+)',line)
    if running:
        classes.setdefault(module,[]).append(running.group(1))
    error = re.match(r'\[ERROR\] <repo>/(.+\.java):\[(\d+),(\d+)\] (.*)',line)
    if error:
        path, number, column, message = error.groups()
        diagnostics[(path,number,column,message)] = {'file':path,'line':int(number),'column':int(column),'message':message}
reports, jdks, problems, conformance = {}, {}, [], {}
for module, names in classes.items():
    for class_name in sorted(set(names)):
        path = repo / module / 'target/surefire-reports' / ('TEST-' + class_name + '.xml')
        assert path.exists(), path
        root = ET.parse(path).getroot()
        row = [int(root.get(key,'0')) for key in ('tests','failures','errors','skipped')]
        props = {node.get('name'):node.get('value') for node in root.findall('./properties/property')}
        assert props.get('java.version','').startswith('21.'), (class_name,props.get('java.version'))
        jdks[module] = {key:props.get(key) for key in ('java.version','java.runtime.version','java.vm.name','java.vendor')}
        tests = []
        for case in root.findall('testcase'):
            test = {'name':case.get('name'),'skipped':case.find('skipped') is not None,'problems':[]}
            for kind in ('failure','error'):
                for problem in case.findall(kind):
                    trace = (problem.text or '').replace(str(repo),'<repo>').replace(str(Path.home()),'<user-home>')
                    item = {'module':module,'class':class_name,'method':case.get('name'),'kind':kind,'type':problem.get('type'),'message':problem.get('message'),'easymock_class_proxy':'org.easymock.internal.ClassProxyFactory.createProxy' in trace and 'MocksControl.createMock' in trace,'trace':trace}
                    test['problems'].append(item)
                    problems.append(item)
            tests.append(test)
        reports.setdefault(module,{})[class_name] = {'counts':row,'tests':tests}
        if module == 'xacml-conformance-tests':
            conformance[class_name] = {'counts':row,'case_ids':[case_id for case in root.findall('testcase') for case_id in re.findall(r'Executing test="([^"]+)"',case.findtext('system-out',''))],'failed_case_ids':[re.findall(r'Executing test="([^"]+)"',case.findtext('system-out',''))[-1:] for case in root.findall('testcase') if case.find('failure') is not None or case.find('error') is not None]}
for module, by_class in reports.items():
    aggregate = [sum(value['counts'][i] for value in by_class.values()) for i in range(4)]
    assert aggregate == counts[module], (module,aggregate,counts.get(module))
snapshot = json.loads((ev / '01-before.json').read_text())
receipt = {'log':name,'build_success':'BUILD SUCCESS' in text and 'BUILD FAILURE' not in text,'tests_executed':bool(counts),'module_counts':counts,'totals':[sum(row[i] for row in counts.values()) for i in range(4)],'compiler_diagnostics':list(diagnostics.values()),'reports':reports,'jdk_properties':jdks,'problems':problems,'conformance':conformance,'root_pom_sha256':hashlib.sha256((repo / 'pom.xml').read_bytes()).hexdigest(),'root_pom_matches_before':hashlib.sha256((repo / 'pom.xml').read_bytes()).hexdigest() == snapshot['hashes']['pom.xml'],'probe_present':(repo / snapshot['temporary_probe']).exists()}
(ev / (log_path.stem + '-receipt.json')).write_text(json.dumps(receipt,indent=2) + '\n')
print(json.dumps({key:receipt[key] for key in ('log','build_success','tests_executed','module_counts','totals','compiler_diagnostics','root_pom_matches_before','probe_present')},indent=2))
