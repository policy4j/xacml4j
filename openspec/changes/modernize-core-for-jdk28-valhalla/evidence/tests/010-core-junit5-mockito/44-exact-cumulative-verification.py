from pathlib import Path
import collections
import json
import re

ev = Path(__file__).parent
support = (ev / '06-manual-mock-adapt.py').read_text()
exec(support[support.index('def mask'):support.index('def convert_calls')])
rows = []
for path in sorted(Path('xacml-core/src/test/java').rglob('*.java')):
    source = path.read_text()
    changes = []
    for method, a, b in methods(source):
        body = source[a:b]
        clean = mask(body)
        events = sorted([(m.start(), 'ordered', m) for m in re.finditer(r'\border\.verify\s*\(', clean)] +
                        [(m.start(), 'boundary', m) for m in re.finditer(r'\b(?:order\.)?verifyNoMoreInteractions\s*\(', clean)])
        totals = collections.OrderedDict()
        pending = False
        for pos, kind, m in events:
            if kind == 'ordered':
                start = clean.find('(', m.start(), m.end())
                end = end_pair(body, start)
                args = body[start + 1:end]
                mock, *mode = args.split(',', 1)
                mock = mock.strip()
                mode = mode[0].strip() if mode else ''
                if mode and not re.fullmatch(r'times\(\d+\)', mode):
                    continue
                count = int(re.fullmatch(r'times\((\d+)\)', mode).group(1)) if mode else 1
                finish = end_statement(body, m.start())
                call = body[end + 1:finish].strip()
                key = (mock, re.sub(r'\s+', ' ', call))
                totals[key] = (totals.get(key, (0, call))[0] + count, call)
                pending = True
            elif pending:
                insert = body.rfind('\n', 0, pos) + 1
                assert not body[insert:pos].strip(), (path, method)
                checks = ['verify(' + mock + ', times(' + str(count) + '))' + call
                          for (mock, _), (count, call) in totals.items()]
                indentation = body[insert:pos]
                comment = '// Count all calls; strict stubs can otherwise mark extra calls as verified.'
                added = indentation + comment + '\n' + ''.join(indentation + check + '\n' for check in checks)
                changes.append((a + insert, a + insert, added))
                rows.append({'file':str(path), 'method':method, 'line_before':source.count('\n',0,a+pos)+1,
                             'checks':checks, 'basis':'Cumulative ordered expectations, never observed invocation history'})
                pending = False
    if changes:
        backup = ev / '44-before-exact-counts' / path.relative_to('xacml-core/src/test')
        backup.parent.mkdir(parents=True, exist_ok=True)
        backup.write_text(source)
        source = edits(source, changes)
        if 'import static org.mockito.Mockito.*;' not in source:
            if 'import static org.mockito.Mockito.verify;' not in source:
                source = source.replace('import static org.mockito.Mockito.verifyNoMoreInteractions;',
                                        'import static org.mockito.Mockito.verify;\nimport static org.mockito.Mockito.verifyNoMoreInteractions;')
            if 'import static org.mockito.Mockito.times;' not in source:
                source = source.replace('import static org.mockito.Mockito.verify;',
                                        'import static org.mockito.Mockito.times;\nimport static org.mockito.Mockito.verify;')
        path.write_text(source)
(ev / '44-exact-count-checks.json').write_text(json.dumps(rows, indent=2) + '\n')
print(json.dumps({'files':len({r['file'] for r in rows}), 'verification_boundaries':len(rows),
                  'explicit_total_count_checks':sum(len(r['checks']) for r in rows)}, indent=2))
