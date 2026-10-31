from pathlib import Path
import json
import re

repo = Path.cwd()
ev = Path(__file__).parent
owned = repo / 'xacml-core/src/test'
snapshot = json.loads((ev / '01-snapshot.json').read_text())
def mask(source):
    return re.sub(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',
                  lambda m: ''.join('\n' if c=='\n' else ' ' for c in m.group()), source)
def end_pair(source, start, opening='(', closing=')'):
    clean = mask(source)
    depth = 0
    for i in range(start,len(source)):
        if clean[i]==opening: depth += 1
        elif clean[i]==closing:
            depth -= 1
            if depth==0:return i
    raise AssertionError(source[start:start+160])
def end_statement(source,start):
    clean=mask(source); p=b=0
    for i in range(start,len(source)):
        if clean[i]=='(':p+=1
        elif clean[i]==')':p-=1
        elif clean[i]=='{':b+=1
        elif clean[i]=='}':b-=1
        elif clean[i]==';' and p==0 and b==0:return i+1
    raise AssertionError(source[start:start+160])
def edits(source, changes):
    for a,b,value in sorted(changes,reverse=True):
        source=source[:a]+value+source[b:]
    return source
def methods(source):
    clean=mask(source)
    pattern=r'(?m)^[ \t]*(?:(?:public|private|protected|static|final|synchronized)\s+)*(?:[\w.$]+(?:<[^;{}=]+>)?(?:\[\])?)\s+(\w+)\s*\([^;{}=]*\)\s*(?:throws[^{;]+)?\{'
    out=[]; until=-1
    for m in re.finditer(pattern,clean):
        if m.start()<until:continue
        start=clean.find('{',m.start(),m.end())
        end=end_pair(source,start,'{','}')
        out.append((m.group(1),start+1,end))
        until=end+1
    return out
def convert_calls(source):
    source=re.sub(r'\bcapture\((\w+)\)',r'\1.capture()',source)
    source=re.sub(r'\b(?:\w+\.)createMock\s*\(', 'mock(',source)
    source=re.sub(r'\b(?:EasyMock\.)?(?:createStrictMock|createMock|createNiceMock)\s*\(', 'mock(',source)
    source=re.sub(r'\bEasyMock\.', 'Mockito.',source)
    source=re.sub(r'\banyObject\s*\(', 'any(',source)
    return source
def model_inputs(source,name):
    if name in ('MatchAllOfTest','MatchAnyOfTest','TargetTest'):
        source=re.sub(r'(this\.context\s*=\s*)\w+\.createMock\(EvaluationContext.class\)',r'\1ModelFixtures.context()',source)
        factories={'Match':'match','MatchAllOf':'allOf','MatchAnyOf':'anyOf'}
        def local(m):
            typ,var=m.groups()
            return 'AttributeReference '+var+'Reference = ModelFixtures.reference();\n\t\t'+typ+' '+var+' = ModelFixtures.'+factories[typ]+'('+var+'Reference);'
        source=re.sub(r'\b(Match|MatchAllOf|MatchAnyOf) (\w+) = \w+\.createMock\(\1.class\);',local,source)
    if name in ('PolicyTest','PolicySetTest','RuleTest'):
        source=source.replace('private Target target;','private Target target;\n\tprivate AttributeReference targetReference;')
        source=source.replace('private Condition condition;','private Condition condition;\n\tprivate Expression conditionExpression;')
        source=re.sub(r'this\.target = \w+\.createMock\(Target.class\);','this.targetReference = ModelFixtures.reference();\n\t\tthis.target = ModelFixtures.target(targetReference);',source)
        source=re.sub(r'this\.condition = \w+\.createMock\(Condition.class\);','this.conditionExpression = mock(Expression.class);\n\t\tthis.condition = ModelFixtures.condition(conditionExpression);',source)
        # This overwritten setup mock never had a recorded expectation.
        source=re.sub(r'^[ \t]*this\.enclosingPolicy = \w+\.createMock\(Policy.class\);\n','',source,flags=re.M)
    if name in ('ConditionTest','MatchTest'):
        source=re.sub(r'\w+\.createMock\(Rule.class\)', 'Rule.builder("testRule", org.xacml4j.v30.Effect.DENY).build()',source)
    if name in ('ConditionTest','CombinerParameterTest'):
        source=re.sub(r'Value exp1 = \w+\.createMock\(Value.class\);','Value exp1 = org.xacml4j.v30.types.BooleanVal.of(true);',source)
        source=re.sub(r'Value exp2 = \w+\.createMock\(Value.class\);','Value exp2 = org.xacml4j.v30.types.BooleanVal.of(false);',source)
        source=re.sub(r'expect\((exp[12]\.getEvaluatesTo\(\))\)\.andReturn\(XacmlTypes.BOOLEAN\)\.times\(\d+\);',r'assertEquals(XacmlTypes.BOOLEAN, \1);',source)
    if name=='ObligationExpressionTest':
        source=re.sub(r'\w+\.createMock\(AttributeAssignmentExpression.class\)', 'AttributeAssignmentExpression.builder("id").expression(XacmlTypes.STRING.ofAny("value")).build()',source)
        source=source.replace('expect(attrExp.getAttributeId()).andReturn("id");','assertEquals("id", attrExp.getAttributeId());')
    if name=='MatchTest':
        source=re.sub(r'\w+\.createMock\(AttributeDesignator.class\)', 'AttributeDesignator.builder().key(AttributeDesignatorKey.builder("testId").category(CategoryId.RESOURCE).dataType(XacmlTypes.INTEGER).build()).mustBePresent(false).build()',source)
        source=source.replace('expect(ref.getDataType()).andReturn(XacmlTypes.INTEGER);','assertEquals(XacmlTypes.INTEGER, ref.getDataType());')
        pattern=r'expect\(ref.evaluate\(context\)\)\.andReturn\(([\s\S]*?)\);'
        source=re.sub(pattern,lambda m:'expect(context.resolve(ref.getReferenceKey())).andReturn(java.util.Optional.of('+m.group(1)+'));',source)
        source=source.replace('expect(ref.evaluate(context)).andThrow(', 'expect(context.resolve(ref.getReferenceKey())).andThrow(')
    # Move model evaluation expectations to their interface input.
    pattern=r'expect\((target|m\d+)\.match\(([\s\S]*?)\)\)\.andReturn\((MatchResult\.\w+)\);'
    def match_input(m):
        obj,arg,result=m.groups()
        ref='targetReference' if obj=='target' else obj+'Reference'
        value='.andThrow(ModelFixtures.evaluationError())' if result.endswith('INDETERMINATE') else '.andReturn(ModelFixtures.matchValues('+result+'))'
        return 'expect('+ref+'.evaluate('+arg+'))'+value+';'
    source=re.sub(pattern,match_input,source)
    source=source.replace('expect(target.match(', 'expect(targetReference.evaluate(')
    pattern=r'expect\(condition\.evaluate\(([\s\S]*?)\)\)\.andReturn\(ConditionResult\.(TRUE|FALSE|INDETERMINATE)\);'
    source=re.sub(pattern,lambda m:'expect(conditionExpression.evaluate('+m.group(1)+'))'+('.andThrow(ModelFixtures.evaluationError())' if m.group(2)=='INDETERMINATE' else '.andReturn(XacmlTypes.BOOLEAN.ofAny('+str(m.group(2)=='TRUE').lower()+'))')+';',source)
    if 'ModelFixtures' in source and name not in ('ConditionTest','MatchTest','CombinerParameterTest'):
        source=source.replace('import org.xacml4j.v30.EvaluationContext;', 'import org.xacml4j.v30.EvaluationContext;\nimport org.xacml4j.v30.policy.AttributeReference;')
    return source

skipped={'PolicyIDReferenceTest','PolicySetIDReferenceTest','DurationFunctionIdentifierTest',
         'HigherOrderFunctionIdentifierTest','HigherOrderVariadicInvocationTest','HigherOrderVariadicFunctionsTest'}
audit={}
for name,item in snapshot['inventory'].items():
    if not item['easymock']:continue
    path=repo/name
    class_name=path.stem
    if class_name in skipped:continue
    source=(ev/'before'/path.relative_to(owned)).read_text()
    source=model_inputs(source,class_name)
    controls=set(re.findall(r'IMocksControl\s+(\w+)',source))
    source=re.sub(r'^[ \t]*(?:private\s+)?IMocksControl\s+\w+(?:\s*=\s*[^;]+)?;\n','',source,flags=re.M)
    source=re.sub(r'^[ \t]*(?:this\.)?\w+\s*=\s*(?:EasyMock\.)?create(?:Strict)?Control\(\);\n','',source,flags=re.M)
    method_ranges=methods(source)
    field_mocks=[]
    init=next((source[a:b] for n,a,b in method_ranges if n in ('init','setUp')), '')
    for m in re.finditer(r'(?:this\.)?(\w+)\s*=\s*(?:\w+\.)?(?:createStrictMock|createMock|mock|reference)\(',init):
        var=m.group(1)
        later=init[m.end():]
        if not re.search(r'(?:this\.)?'+re.escape(var)+r'\s*=\s*(?:new|ModelFixtures.context)',later):
            field_mocks.append(var)
    field_mocks=list(dict.fromkeys(field_mocks))
    rewritten=[]
    counts={'expectations':0,'explicit_verifications':0,'void_expectations':0}
    for method,a,b in method_ranges:
        body=source[a:b]
        if method in ('init','setUp'):continue
        local_mocks=re.findall(r'\b(\w+)\s*=\s*(?:\w+\.)?(?:createStrictMock|createMock|mock|reference)\(',body)
        mocks=list(dict.fromkeys(field_mocks+local_mocks))
        clean=mask(body)
        changes=[]; expectations=[]
        for m in re.finditer(r'\b(?:EasyMock\.)?expect\s*\(',clean):
            start=clean.find('(',m.start(),m.end())
            end=end_pair(body,start)
            finish=end_statement(body,m.start())
            expression=convert_calls(body[start+1:end])
            tail=body[end+1:finish]
            times=re.search(r'\.times\((\d+)\)',tail)
            quantifier=', times('+times.group(1)+')' if times and times.group(1)!='1' else ''
            if '.anyTimes()' in tail or '.andStub' in tail:quantifier=', atLeast(0)'
            elif '.atLeastOnce()' in tail:quantifier=', atLeastOnce()'
            tail=re.sub(r'\.(?:times\(\d+\)|once\(\)|anyTimes\(\)|atLeastOnce\(\))','',tail)
            tail=tail.replace('.andReturn(','.thenReturn(').replace('.andThrow(','.thenThrow(').replace('.andStubReturn(','.thenReturn(')
            obj,call=expression.split('.',1)
            changes.append((m.start(),finish,'when('+expression+')'+tail))
            expectations.append((m.start(),'order.verify('+obj+quantifier+').'+call+';'))
            counts['expectations']+=1
        replays=list(re.finditer(r'\b(?:(?:EasyMock\.)?replay\s*\([^;]*\)|\w+\.replay\s*\(\s*\))\s*;',clean))
        verifies=list(re.finditer(r'\b(?:(?:EasyMock\.)?verify\s*\([^;]*\)|\w+\.verify\s*\(\s*\))\s*;',clean))
        # Void calls in a recording block are expectations too.
        last_boundary=0
        for replay in replays:
            for m in re.finditer(r'(?m)^[ \t]*(\w+)\.(\w+)\(',body[last_boundary:replay.start()]):
                pos=last_boundary+m.start()
                obj,call=m.groups()
                if obj not in mocks:continue
                start=body.find(obj+'.'+call+'(',pos)
                if any(x<=start<y for x,y,_ in changes):continue
                finish=end_statement(body,start)
                expression=convert_calls(body[start:finish])
                if expression.endswith(';'):
                    changes.append((start,finish,'doNothing().when('+obj+').'+expression.split('.',1)[1]))
                    expectations.append((start,'order.verify('+obj+').'+expression.split('.',1)[1]))
                    counts['void_expectations']+=1
            next_verify=next((v for v in verifies if v.start()>replay.end()),None)
            last_boundary=next_verify.end() if next_verify else replay.end()
        for i,replay in enumerate(replays):
            value=('InOrder order = inOrder('+', '.join(mocks)+');') if i==0 and mocks else ''
            changes.append((replay.start(),replay.end(),value))
        consumed=0
        for i,verification in enumerate(verifies):
            statements=[v for pos,v in sorted(expectations) if consumed<=pos<verification.start()]
            # A catch block and the normal path may each verify the same recording.
            if not statements and i and not any(r.start()>verifies[i-1].start() and r.start()<verification.start() for r in replays):
                statements=[v for pos,v in sorted(expectations) if pos<verification.start()]
            consumed=verification.end()
            statements+=['order.verifyNoMoreInteractions();'] if mocks and replays else []
            if mocks:statements+=['verifyNoMoreInteractions('+', '.join(mocks)+');']
            indentation=re.search(r'(?m)^[ \t]*$',body[:verification.start()].split('\n')[-1])
            indent=body[:verification.start()].split('\n')[-1]
            changes.append((verification.start(),verification.end(),('\n'+indent).join(statements)))
            counts['explicit_verifications']+=len(statements)
        for m in re.finditer(r'\b\w+\.reset\s*\(\s*\)\s*;',clean):
            changes.append((m.start(),m.end(),''))
        # Preserve expected-exception checks, with post-call verification reachable.
        before_method=source[source.rfind('@Test',0,a):a]
        annotation=re.search(r'@Test\s*\(\s*expected\s*=\s*([\w.]+)\.class\s*\)[\s\S]*?\b'+re.escape(method)+r'\s*\([^{}]*\)\s*\{$',before_method)
        if annotation:
            expected=annotation.group(1)
            start=replays[-1].end() if replays else 0
            finish=verifies[0].start() if verifies else len(body)
            region=body[start:finish]
            if re.search(r'\btry\s*\{',mask(region)):
                trigger=start+re.search(r'\btry\s*\{',mask(region)).start()
                # Include the complete try/catch with its existing assertions and rethrow.
                transformed=edits(body,changes)
                rewritten.append((a,b,'\n\t\tassertThrows('+expected+'.class, () -> {'+transformed+'\n\t\t});\n\t'))
                continue
            end=body.rfind(';',start,finish)+1
            assert end>start,(class_name,method)
            previous=max(start,body.rfind(';',start,end-1)+1)
            actual_start=previous
            while actual_start<end and body[actual_start].isspace():actual_start+=1
            expression=body[actual_start:end-1]
            changes.append((actual_start,end,'assertThrows('+expected+'.class, () -> '+expression+');'))
        rewritten.append((a,b,edits(body,changes)))
    source=edits(source,rewritten)
    source=convert_calls(source)
    source=re.sub(r'Capture<([^;\n]+)> (\w+)\s*=\s*Capture.newInstance\(\);',
                  lambda m:'ArgumentCaptor<'+m.group(1)+'> '+m.group(2)+' = ArgumentCaptor.forClass('+m.group(1).split('<')[0]+'.class);',source)
    source=re.sub(r'@Test\s*\(\s*expected\s*=\s*[\w.]+\.class\s*\)','@Test',source)
    source=source.replace('org.junit.Assert.assertThat','org.hamcrest.MatcherAssert.assertThat')
    source=source.replace('org.junit.Assert','org.junit.jupiter.api.Assertions').replace('org.junit.Test','org.junit.jupiter.api.Test')
    for old,new in [('BeforeClass','BeforeAll'),('Before','BeforeEach'),('AfterClass','AfterAll'),('After','AfterEach'),('Ignore','Disabled')]:
        source=re.sub(r'org.junit.'+old+r'\b','org.junit.jupiter.api.'+new,source)
        source=re.sub(r'@'+old+r'\b','@'+new,source)
    source=re.sub(r'^import (?:static )?org.easymock.[^\n]*\n','',source,flags=re.M)
    source=source.replace('import org.mockito.Mockito;\n','')
    imports='import static org.mockito.Mockito.*;\nimport static org.mockito.ArgumentMatchers.*;\nimport static org.junit.jupiter.api.Assertions.assertThrows;\n\nimport org.mockito.ArgumentCaptor;\nimport org.mockito.InOrder;\nimport org.mockito.Mockito;\nimport org.mockito.junit.jupiter.MockitoExtension;\nimport org.junit.jupiter.api.extension.ExtendWith;\n'
    index=source.index('import ')
    source=source[:index]+imports+source[index:]
    source=source.replace('public class '+class_name,'@ExtendWith(MockitoExtension.class)\npublic class '+class_name,1)
    path.write_text(source)
    audit[name]=counts
# Undo only recipe-induced license movement/duplication; exact task-start header bytes.
header_pattern=r'/\*[\s\S]*?#%L[\s\S]*?#L%[\s\S]*?\*/'
for name,item in snapshot['inventory'].items():
    path=repo/name
    original=(ev/'before'/path.relative_to(owned)).read_text()
    source=path.read_text()
    original_headers=re.findall(header_pattern,original)
    if not original_headers:continue
    source=re.sub(header_pattern,'',source)
    package_end=re.search(r'package [^;]+;',source).end()
    source=source[:package_end]+'\n\n'+original_headers[0]+source[package_end:]
    # Remove blank lines left by header relocation, retaining surrounding file indentation.
    source=re.sub(r'\n(?:[ \t]*\n){3,}','\n\n\n',source)
    path.write_text(source)
(ev/'06-mock-repair-summary.json').write_text(json.dumps(audit,indent=2)+'\n')
print('Manually completed recording/verification/capture conversions in',len(audit),'files; exact license headers restored.')
