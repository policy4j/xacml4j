from pathlib import Path
import collections
import json
import re
import subprocess

ev = Path(__file__).parent
support = (ev / '06-manual-mock-adapt.py').read_text()
exec(support[support.index('def mask'):support.index('def convert_calls')])

def compact(s):
    return re.sub(r'\s+', ' ', s).strip()

def statement_matches(folder):
    command = ['ast-grep', 'run', '--lang', 'java', '--pattern',
               '$RECEIVER.$METHOD($$$ARGS);', '--json=compact', str(folder)]
    result = subprocess.run(command, capture_output=True, text=True)
    assert result.returncode in (0, 1) and not result.stderr, (result.returncode, result.stderr)
    return json.loads(result.stdout), command

current_nodes, current_cmd = statement_matches(Path('xacml-core/src/test/java'))
before_nodes, before_cmd = statement_matches(ev / 'before/java')
call_command = ['ast-grep', 'run', '--lang', 'java', '--pattern',
                '$RECEIVER.$METHOD($$$ARGS)', '--json=compact', 'xacml-core/src/test/java']
call_result = subprocess.run(call_command, capture_output=True, text=True)
assert call_result.returncode in (0, 1) and not call_result.stderr
call_nodes = json.loads(call_result.stdout)
call_index = collections.defaultdict(list)
for item in call_nodes:
    call_index[item['file']].append(item)
node_index = collections.defaultdict(list)
for item in current_nodes:
    node_index[item['file']].append(item)

plain_calls = []
unwrapped_mock_calls = []
lambda_verifies = []
duplicate_verifies = []
captors = []
count_checks = []
void_recordings = []
mock_only_tests = []
legacy = json.loads((ev / '26-source-audit.json').read_text())

for path in sorted(Path('xacml-core/src/test/java').rglob('*.java')):
    source = path.read_text()
    clean = mask(source)
    method_ranges = methods(source)
    all_mocks = set(re.findall(r'(?:this\.)?(\w+)\s*=\s*(?:\w+\.)?(?:mock|reference)\(', clean))
    def method_at(pos):
        return next((n for n, a, b in method_ranges if a <= pos < b), '<field>')
    def line_at(pos):
        return source.count('\n', 0, pos) + 1
    stub_spans = [(m.start(), end_pair(source, clean.find('(', m.start(), m.end())) + 1)
                  for m in re.finditer(r'\bwhen\s*\(', clean)]
    for node in call_index[str(path)]:
        meta = node['metaVariables']['single']
        receiver = compact(meta['RECEIVER']['text']).removeprefix('this.')
        pos = len(source.encode()[:node['range']['byteOffset']['start']].decode())
        if receiver in all_mocks and not any(a <= pos < b for a,b in stub_spans):
            unwrapped_mock_calls.append({'file':str(path), 'line':line_at(pos), 'method':method_at(pos),
                                         'call':compact(node['text'])})
    for node in node_index[str(path)]:
        meta = node['metaVariables']['single']
        receiver = compact(meta['RECEIVER']['text'])
        pos = len(source.encode()[:node['range']['byteOffset']['start']].decode())
        if receiver.removeprefix('this.') in all_mocks:
            plain_calls.append({'file':str(path), 'line':line_at(pos), 'method':method_at(pos),
                                'call':compact(node['text'])})
    for m in re.finditer(r'\bassertThrows\s*\(', clean):
        end = end_pair(source, clean.find('(', m.start(), m.end()))
        text = source[m.start():end + 1]
        if re.search(r'\bverify(?:NoMoreInteractions|NoInteractions)?\s*\(', mask(text)):
            lambda_verifies.append({'file':str(path), 'line':line_at(m.start()), 'call':compact(text)})
    for method, a, b in method_ranges:
        body = source[a:b]
        statements = []
        for m in re.finditer(r'(?m)^[ \t]*(?:order\.)?verify\s*\(', mask(body)):
            end = end_statement(body, m.start())
            statements.append((m.start(), end, compact(body[m.start():end])))
        for left, right in zip(statements, statements[1:]):
            if left[2] == right[2] and not mask(body[left[1]:right[0]]).strip():
                duplicate_verifies.append({'file':str(path), 'line':line_at(a + right[0]),
                                           'method':method, 'call':right[2]})
    for m in re.finditer(r'ArgumentCaptor\s*<[^;=]+?>\s+(\w+)\s*(?:=|;)', clean):
        var = m.group(1)
        method = method_at(m.start())
        region = next((source[a:b] for n, a, b in method_ranges if n == method), source)
        if method in ('init','setUp','<field>'):
            region = source
        getters = re.findall(r'\b' + re.escape(var) + r'\.get(?:Value|AllValues)\s*\(', region)
        captors.append({'file':str(path), 'line':line_at(m.start()), 'method':method,
                        'variable':var, 'getter_sites':len(getters), 'needs_review':not getters})

    old_path = ev / 'before/java' / path.relative_to('xacml-core/src/test/java')
    if not old_path.exists():
        continue
    old = old_path.read_text()
    old_clean = mask(old)
    old_mocks = set(re.findall(r'(?:this\.)?(\w+)\s*=\s*(?:\w+\.)?(?:createMock|createStrictMock|createNiceMock)\(', old_clean))
    for method, a, b in methods(old):
        body = old[a:b]
        new_body = next((source[x:y] for n, x, y in method_ranges if n == method), '')
        if ('verify(' in body or '.verify(' in body) and not re.search(r'\bassert\w*\s*\(', mask(body)):
            mock_only_tests.append({'file':str(path), 'method':method,
                                    'current_verify':bool(re.search(r'\bverify\s*\(',new_body)),
                                    'current_no_more': 'verifyNoMoreInteractions(' in new_body})
        for m in re.finditer(r'\bexpect\s*\(', mask(body)):
            start = body.find('(', m.start(), m.end())
            end = end_pair(body, start)
            finish = end_statement(body, m.start())
            expr = body[start + 1:end]
            tail = body[end + 1:finish]
            count = re.search(r'\.(times|once)\(([^)]*)\)', tail)
            if count:
                receiver, call = expr.split('.', 1)
                operation = call.split('(', 1)[0]
                alternatives = []
                for v in re.finditer(r'\b(?:order\.)?verify\s*\(', mask(new_body)):
                    ve = end_statement(new_body, v.start())
                    text = new_body[v.start():ve]
                    if re.search(r'\.' + re.escape(operation) + r'\s*\(', text):
                        alternatives.append(compact(text))
                count_checks.append({'file':str(path), 'method':method,
                                     'old_line':old.count('\n', 0, a + m.start()) + 1,
                                     'expectation':compact(expr), 'old_count':count.group(0),
                                     'current_candidate_checks':alternatives})

for node in before_nodes:
    path = Path(node['file'])
    source = path.read_text()
    pos = len(source.encode()[:node['range']['byteOffset']['start']].decode())
    meta = node['metaVariables']['single']
    receiver = compact(meta['RECEIVER']['text']).removeprefix('this.')
    all_mocks = set(re.findall(r'(?:this\.)?(\w+)\s*=\s*(?:\w+\.)?(?:createMock|createStrictMock|createNiceMock)\(', mask(source)))
    if receiver not in all_mocks:
        continue
    method, a, b = next(((n, x, y) for n, x, y in methods(source) if x <= pos < y), ('<field>',0,len(source)))
    region = mask(source[a:b])
    local = pos - a
    replays = list(re.finditer(r'\b(?:replay\s*\([^;]*\)|\w+\.replay\s*\(\s*\))\s*;', region))
    verifies = list(re.finditer(r'\b(?:verify\s*\([^;]*\)|\w+\.verify\s*\(\s*\))\s*;', region))
    if any(local < r.start() and not any(local < v.start() < r.start() for v in verifies) for r in replays):
        current_path = Path('xacml-core/src/test/java') / path.relative_to(ev / 'before/java')
        migrated = current_path.read_text()
        migrated_body = next((migrated[x:y] for n,x,y in methods(migrated) if n == method), '')
        operation = meta['METHOD']['text']
        matching = [compact(migrated_body[v.start():end_statement(migrated_body, v.start())])
                    for v in re.finditer(r'\b(?:order\.)?verify\s*\(', mask(migrated_body))
                    if re.search(r'\.' + re.escape(operation) + r'\s*\(',
                                 migrated_body[v.start():end_statement(migrated_body, v.start())])]
        mapped_helper = None
        if current_path.stem == 'HigherOrderVariadicFunctionsTest' and method == 'context':
            helper = next(migrated[x:y] for n,x,y in methods(migrated) if n == 'assertInvalid')
            matching = [compact(helper[v.start():end_statement(helper, v.start())])
                        for v in re.finditer(r'\bverify\s*\(', mask(helper))
                        if '.setEvaluationStatus(' in helper[v.start():end_statement(helper, v.start())]]
            mapped_helper = 'assertInvalid; AfterEach verifies no other calls'
        void_recordings.append({'file':str(current_path), 'method':method,
                                'old_line':source.count('\n',0,pos)+1, 'call':compact(node['text']),
                                'current_checks':matching, 'mapped_helper':mapped_helper})

receipt = {'coverage':'SUPPORTED Java statement syntax; receiver identities/count correspondence require source review',
           'ast_commands':[current_cmd, before_cmd, call_command], 'current_statement_matches':len(current_nodes),
           'before_statement_matches':len(before_nodes), 'plain_mock_call_candidates':plain_calls,
           'all_method_invocation_matches':len(call_nodes), 'unwrapped_mock_call_candidates':unwrapped_mock_calls,
           'verify_inside_assertThrows':lambda_verifies, 'duplicate_consecutive_verifies':duplicate_verifies,
           'captor_declarations':captors, 'explicit_original_count_sites':count_checks,
           'original_void_recording_sites':void_recordings, 'original_mock_only_methods':mock_only_tests}
(ev / '32-review-artifacts-audit.json').write_text(json.dumps(receipt, indent=2) + '\n')
print(json.dumps({'plain_mock_call_candidates':plain_calls, 'unwrapped_mock_call_candidates':unwrapped_mock_calls,
                  'verify_inside_assertThrows':lambda_verifies,
                  'duplicate_consecutive_verifies':duplicate_verifies,
                  'unused_captor_candidates':[x for x in captors if x['needs_review']],
                  'original_count_sites':len(count_checks), 'original_void_recording_sites':len(void_recordings),
                  'missing_void_checks':[x for x in void_recordings if not x['current_checks']],
                  'mock_only_methods_without_oracle':[x for x in mock_only_tests if not (x['current_verify'] or x['current_no_more'])]}, indent=2))
