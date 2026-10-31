from pathlib import Path
import re
p=Path('xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl')
for name in ('DurationFunctionIdentifierTest','HigherOrderFunctionIdentifierTest'):
    path=p/(name+'.java')
    s=path.read_text()
    s=s.replace('import static org.mockito.Mockito.mock;','import static org.mockito.Mockito.*;')
    # Invocation helpers assign the actual return value before parity assertions.
    s=re.sub(r'(ValueExp (\w+) = Apply\.[^;]+;)',r'\1\n\t\t\tverify(context, atLeastOnce()).isValidateFuncParamsAtRuntime();\n\t\t\tverifyNoMoreInteractions(context);',s)
    path.write_text(s)
path=p/'HigherOrderVariadicInvocationTest.java'
s=path.read_text()
s=re.sub(r'import static org.mockito.Mockito.[^;]+;\n','',s)
s=re.sub(r'EvaluationContext context = mock\(EvaluationContext.class\);[\s\S]*?return context;', 'EvaluationContext context = org.xacml4j.v30.policy.ModelFixtures.context(validate);\n\t\tassertEquals(validate, context.isValidateFuncParamsAtRuntime());\n\t\treturn context;',s)
path.write_text(s)
path=p/'HigherOrderVariadicFunctionsTest.java'
s=path.read_text()
s=re.sub(r'import static org.mockito.Mockito.[^;]+;\n','',s)
s=s.replace('import org.easymock.Capture;','import org.mockito.ArgumentCaptor;')
s=s.replace('private Capture<Status> status;','private ArgumentCaptor<Status> status;')
s=s.replace('import org.junit.jupiter.api.BeforeEach;','import static org.mockito.Mockito.*;')
s=s.replace('this.runtimeValidation = runtimeValidation;','this.runtimeValidation = runtimeValidation;\n\t\tcontext();')
s=re.sub(r'\t@BeforeEach\n(\tpublic void context)',r'\1',s)
s=re.sub(r'context = mock\(EvaluationContext.class\);[\s\S]*?expectLastCall\(\);',
'''context = mock(EvaluationContext.class, invocation ->
\t\t\t\tinvocation.getMethod().getName().equals("isValidateFuncParamsAtRuntime")
\t\t\t\t\t\t? runtimeValidation : RETURNS_DEFAULTS.answer(invocation));
\t\tstatus = ArgumentCaptor.forClass(Status.class);''',s)
s=s.replace('status.reset();','status = ArgumentCaptor.forClass(Status.class);\n\t\tlong previousStatuses = mockingDetails(context).getInvocations().stream()\n\t\t\t\t.filter(invocation -> invocation.getMethod().getName().equals("setEvaluationStatus")).count();')
s=s.replace('assertTrue(status.hasCaptured(),','verify(context, times((int)previousStatuses + 1)).setEvaluationStatus(status.capture());\n\t\tverify(context, atLeastOnce()).isValidateFuncParamsAtRuntime();\n\t\tassertTrue(!status.getAllValues().isEmpty(),')
# For value paths the optional EasyMock runtime flag now receives an explicit check.
s=s.replace('assertEquals(expected, apply.evaluate(context));','assertEquals(expected, apply.evaluate(context));\n\t\tverify(context, atLeastOnce()).isValidateFuncParamsAtRuntime();')
path.write_text(s)
print('Repaired four parameterized/evaluation helper classes; captures remain asserted and mode is initialized before the context.')

