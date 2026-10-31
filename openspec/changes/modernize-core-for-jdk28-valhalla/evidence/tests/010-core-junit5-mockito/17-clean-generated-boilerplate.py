from pathlib import Path
import json
import re

ev = Path(__file__).parent
snapshot = json.loads((ev / '01-snapshot.json').read_text())
adapted = json.loads((ev / '06-mock-repair-summary.json').read_text())
changes = []
for name in snapshot['inventory']:
    path = Path(name)
    old = (ev / 'before' / path.relative_to('xacml-core/src/test')).read_text()
    source = path.read_text()
    old_import = re.search(r'^import ', old, re.M)
    current_import = re.search(r'^import ', source, re.M)
    if old_import and current_import:
        source = old[:old_import.start()] + source[current_import.start():]
    if name in adapted:
        body = re.sub(r'^import [^\n]*\n', '', source, flags=re.M)
        for symbol, statement in {
                'ArgumentCaptor': 'import org.mockito.ArgumentCaptor;\n',
                'InOrder': 'import org.mockito.InOrder;\n',
                'Mockito': 'import org.mockito.Mockito;\n',
                'assertThrows': 'import static org.junit.jupiter.api.Assertions.assertThrows;\n'}.items():
            if not re.search(r'\b' + symbol + r'\b', body):
                source = source.replace(statement, '')
        # Mockito inherits ArgumentMatchers; this duplicate wildcard was added by the adapter.
        source = source.replace('import static org.mockito.ArgumentMatchers.*;\n', '')
        if path.name == 'CombinerParameterTest.java':
            for statement in ('import static org.mockito.Mockito.*;\n',
                              'import org.mockito.junit.jupiter.MockitoExtension;\n',
                              'import org.junit.jupiter.api.extension.ExtendWith;\n',
                              '@ExtendWith(MockitoExtension.class)\n'):
                source = source.replace(statement, '')
    if source != path.read_text():
        path.write_text(source)
        changes.append(name)
(ev / '17-boilerplate-cleanup.json').write_text(json.dumps(changes, indent=2) + '\n')
print('Restored exact task-start file prefixes and trimmed only generated unused imports:', len(changes))
