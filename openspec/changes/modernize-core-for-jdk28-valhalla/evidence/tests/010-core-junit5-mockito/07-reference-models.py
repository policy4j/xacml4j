from pathlib import Path
import re
import json
ev=Path(__file__).parent
repo=Path.cwd()
definitions=(ev/'06-manual-mock-adapt.py').read_text().split('skipped=')[0]
exec(definitions)
def replace_file(class_name, body, imports):
    p=Path('xacml-core/src/test/java/org/xacml4j/v30/policy/'+class_name+'.java')
    original=(ev/'before'/p.relative_to(Path('xacml-core/src/test'))).read_text()
    prefix=original[:original.index('import ')]
    p.write_text(prefix+imports+'\n@ExtendWith(MockitoExtension.class)\npublic class '+class_name+'\n{\n'+body+'\n}\n')
def original_methods(class_name, names):
    p=ev/'before/java/org/xacml4j/v30/policy'/ (class_name+'.java')
    source=p.read_text()
    result=[]
    for name,a,b in methods(source):
        if name in names:
            start=source.rfind('@Test',0,a)
            method=source[start:b+1]
            method=method.replace('@Test(expected=IllegalArgumentException.class)','@Test')
            result.append('\t'+method)
    return '\n\n'.join(result)

common_imports='''import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xacml4j.v30.CoreException;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
'''
body='''\tprivate EvaluationContext context;
\tprivate Policy refPolicy;
\tprivate DecisionCombiningAlgorithm<Rule> algorithm;

\t@BeforeEach
\tpublic void init()
\t{
\t\tcontext = mock(EvaluationContext.class);
\t\talgorithm = mock(DecisionCombiningAlgorithm.class);
\t\trefPolicy = Policy.builder("testId").version("1.0").combiningAlgorithm(algorithm).build();
\t}
'''
failures=[
('testEvaluateFailedToResolveReference',True,['Decision.INDETERMINATE','Decision.INDETERMINATE','MatchResult.INDETERMINATE']),
('testEvaluateFailedToResolveReferenceAndReturnNull',False,['Decision.INDETERMINATE','Decision.INDETERMINATE','MatchResult.INDETERMINATE']),
('testEvaluateIfApplicableFailedToResolveReferenceViaException',True,['Decision.INDETERMINATE']),
('testEvaluateIfApplicableFailedToResolveReferenceReturnsNull',False,['Decision.INDETERMINATE']),
('testIsApplicableFailedToResolveReference',True,['MatchResult.INDETERMINATE'])]
for name,raises,results in failures:
    body+='\n\t@Test\n\tpublic void '+name+'() throws CoreException\n\t{\n'
    body+='''\t\tPolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
\t\twhen(context.getCurrentPolicyIDReference()).thenReturn(null);
\t\twhen(context.getCurrentPolicy()).thenReturn(null);
'''
    body+='\t\twhen(context.resolve(ref)).'+('thenThrow(new PolicyResolutionException(null))' if raises else 'thenReturn(null)')+';\n'
    body+='''\t\tInOrder order = inOrder(context, algorithm);
\t\tEvaluationContext policyRefContext = ref.createContext(context);
\t\tassertSame(ref, policyRefContext.getCurrentPolicyIDReference());
\t\torder.verify(context).getCurrentPolicyIDReference();
\t\torder.verify(context).getCurrentPolicy();
\t\torder.verify(context).getCurrentPolicyIDReference();
\t\torder.verify(context).resolve(ref);
\t\tverifyNoMoreInteractions(context, algorithm);
'''
    for value in results:
        op='isMatch' if value.startswith('MatchResult.') else 'evaluate'
        body+='\t\tassertEquals('+value+', ref.'+op+'(policyRefContext));\n\t\torder.verify(context).getCurrentPolicy();\n\t\tverifyNoMoreInteractions(context, algorithm);\n'
    body+='\t}\n'
body+='''
\t@Test
\tpublic void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
\t{
\t\tPolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
\t\tassertThrows(IllegalArgumentException.class, () -> ref.evaluate(context));
\t\tverify(context).getCurrentPolicyIDReference();
\t\tverifyNoMoreInteractions(context, algorithm);
\t}

\t@Test
\tpublic void testCreatePolicyIDReferenceEvaluatonConntext() throws EvaluationException
\t{
\t\tPolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
\t\twhen(context.getCurrentPolicyIDReference()).thenReturn(null);
\t\twhen(context.getCurrentPolicy()).thenReturn(null);
\t\twhen(context.resolve(ref)).thenReturn(refPolicy);
\t\tEvaluationContext policyContext = ref.createContext(context);
\t\tassertSame(refPolicy, policyContext.getCurrentPolicy());
\t\tassertSame(ref, policyContext.getParentContext().getCurrentPolicyIDReference());
\t\tassertSame(context, policyContext.getParentContext().getParentContext());
\t\tInOrder order = inOrder(context, algorithm);
\t\torder.verify(context).getCurrentPolicyIDReference();
\t\torder.verify(context).getCurrentPolicy();
\t\torder.verify(context).getCurrentPolicyIDReference();
\t\torder.verify(context).resolve(ref);
\t\torder.verify(context).getCurrentPolicy();
\t\tverifyNoMoreInteractions(context, algorithm);
\t}

\t@Test
\tpublic void testEvaluateIfApplicablePolicyCanBeResolved() throws CoreException
\t{
\t\tPolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
\t\twhen(context.getCurrentPolicyIDReference()).thenReturn(null);
\t\twhen(context.getCurrentPolicy()).thenReturn(null);
\t\twhen(context.resolve(ref)).thenReturn(refPolicy);
\t\twhen(algorithm.combine(any(EvaluationContext.class), eq(Collections.emptyList()))).thenReturn(Decision.PERMIT);
\t\tEvaluationContext ctx = ref.createContext(context);
\t\tassertEquals("testId", refPolicy.getId());
\t\tassertEquals(Version.parse("1.0"), refPolicy.getVersion());
\t\tassertEquals(Decision.PERMIT, ref.evaluate(ctx));
\t\tArgumentCaptor<EvaluationContext> refContext = ArgumentCaptor.forClass(EvaluationContext.class);
\t\tInOrder order = inOrder(context, algorithm);
\t\torder.verify(context).getCurrentPolicyIDReference();
\t\torder.verify(context).getCurrentPolicy();
\t\torder.verify(context).getCurrentPolicyIDReference();
\t\torder.verify(context).resolve(ref);
\t\torder.verify(context).getCurrentPolicy();
\t\torder.verify(algorithm).combine(refContext.capture(), eq(Collections.emptyList()));
\t\tassertSame(ctx, refContext.getValue());
\t\torder.verify(context).isExtendedIndeterminateEval();
\t\torder.verify(context).addAdvices(Decision.PERMIT, Collections.emptyList());
\t\torder.verify(context).addObligations(Decision.PERMIT, Collections.emptyList());
\t\torder.verify(context).addEvaluationResult(refPolicy, Decision.PERMIT);
\t\tverifyNoMoreInteractions(context, algorithm);
\t\tassertSame(ref, ctx.getCurrentPolicyIDReference());
\t\tassertSame(refPolicy, ctx.getCurrentPolicy());
\t}

\t@Test
\tpublic void testIsApplicablePolicyCanBeResolved() throws CoreException
\t{
\t\tPolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
\t\twhen(context.getCurrentPolicyIDReference()).thenReturn(null);
\t\twhen(context.getCurrentPolicy()).thenReturn(null);
\t\twhen(context.resolve(ref)).thenReturn(refPolicy);
\t\tEvaluationContext ctx = ref.createContext(context);
\t\tassertEquals("testId", refPolicy.getId());
\t\tassertEquals(Version.parse("1.0"), refPolicy.getVersion());
\t\tassertEquals(MatchResult.MATCH, ref.isMatch(ctx));
\t\tInOrder order = inOrder(context, algorithm);
\t\torder.verify(context).getCurrentPolicyIDReference();
\t\torder.verify(context).getCurrentPolicy();
\t\torder.verify(context).getCurrentPolicyIDReference();
\t\torder.verify(context).resolve(ref);
\t\torder.verify(context).getCurrentPolicy();
\t\tverifyNoMoreInteractions(context, algorithm);
\t\tassertSame(ref, ctx.getCurrentPolicyIDReference());
\t\tassertSame(refPolicy, ctx.getCurrentPolicy());
\t}

\t@Test
\tpublic void testReferersTo() throws Exception
\t{
\t\tPolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
\t\trefPolicy = Policy.builder("testId").version("1.0.1").combiningAlgorithm(algorithm).build();
\t\tassertEquals("testId", refPolicy.getId());
\t\tassertEquals(Version.parse("1.0.1"), refPolicy.getVersion());
\t\tassertTrue(ref.isReferenceTo(refPolicy));
\t\tverifyNoMoreInteractions(context, algorithm);
\t}

'''
body+=original_methods('PolicyIDReferenceTest',{'testReferenceBuilder','testObjectMethods'})
replace_file('PolicyIDReferenceTest',body,common_imports)

body='''\tprivate EvaluationContext context;
\tprivate PolicySet policySet;
\tprivate PolicySet refPolicySet;
\tprivate DecisionCombiningAlgorithm<org.xacml4j.v30.CompositeDecisionRule> algorithm;

\t@BeforeEach
\tpublic void init()
\t{
\t\tcontext = mock(EvaluationContext.class);
\t\talgorithm = mock(DecisionCombiningAlgorithm.class);
\t\tpolicySet = PolicySet.builder("otherTestId").version("1.0").withCombiningAlgorithm(algorithm).build();
\t\trefPolicySet = PolicySet.builder("testId").version("1.0").withCombiningAlgorithm(algorithm).build();
\t}
'''
for name,raises,match in [('testEvaluateFailToResolveReferenceViaException',True,False),
                          ('testEvaluateFailToResolveReferenceReturnsNull',False,False),
                          ('testIsApplicableFailToResolveReference',True,True)]:
    body+='\n\t@Test\n\tpublic void '+name+'() throws CoreException\n\t{\n'
    body+='''\t\tPolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
\t\twhen(context.getCurrentPolicySetIDReference()).thenReturn(null);
'''
    body+='\t\twhen(context.resolve(ref)).'+('thenThrow(new PolicyResolutionException(null))' if raises else 'thenReturn(null)')+';\n'
    body+='''\t\tInOrder order = inOrder(context, algorithm);
\t\tEvaluationContext policyRefContext = ref.createContext(context);
\t\tassertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
\t\torder.verify(context, times(2)).getCurrentPolicySetIDReference();
\t\torder.verify(context).resolve(ref);
\t\tverifyNoMoreInteractions(context, algorithm);
\t\twhen(context.getCurrentPolicySet()).thenReturn(policySet);
\t\tassertEquals("otherTestId", policySet.getId());
\t\tassertEquals(Version.parse("1.0"), policySet.getVersion());
'''
    body+='\t\tassertEquals('+('MatchResult.INDETERMINATE, ref.isMatch' if match else 'Decision.INDETERMINATE, ref.evaluate')+'(policyRefContext));\n'
    body+='\t\torder.verify(context).getCurrentPolicySet();\n\t\tverifyNoMoreInteractions(context, algorithm);\n\t}\n'
body+='''
\t@Test
\tpublic void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
\t{
\t\tPolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
\t\tassertThrows(IllegalArgumentException.class, () -> ref.evaluate(context));
\t\tverify(context).getCurrentPolicySetIDReference();
\t\tverifyNoMoreInteractions(context, algorithm);
\t}
'''
for name,evaluate in [('testIsApplicablePolicyIDReference',False),('testEvaluateIfApplicable',True)]:
    body+='\n\t@Test\n\tpublic void '+name+'() throws CoreException\n\t{\n'
    body+='''\t\tPolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
\t\twhen(context.getCurrentPolicySetIDReference()).thenReturn(null);
\t\twhen(context.getCurrentPolicySet()).thenReturn(policySet);
\t\twhen(context.getCurrentPolicy()).thenReturn(null);
\t\twhen(context.resolve(ref)).thenReturn(refPolicySet);
'''
    if evaluate:body+='\t\twhen(algorithm.combine(any(EvaluationContext.class), eq(Collections.emptyList()))).thenReturn(Decision.DENY);\n'
    body+='''\t\tEvaluationContext policyRefContext = ref.createContext(context);
\t\tassertEquals("testId", refPolicySet.getId());
\t\tassertEquals(Version.parse("1.0"), refPolicySet.getVersion());
'''
    body+='\t\tassertEquals('+('Decision.DENY, ref.evaluate' if evaluate else 'MatchResult.MATCH, ref.isMatch')+'(policyRefContext));\n'
    body+='''\t\tInOrder order = inOrder(context, algorithm);
\t\torder.verify(context, times(2)).getCurrentPolicySetIDReference();
\t\torder.verify(context).resolve(ref);
\t\torder.verify(context, times(2)).getCurrentPolicySet();
\t\torder.verify(context).getCurrentPolicy();
'''
    if evaluate:body+='''\t\tArgumentCaptor<EvaluationContext> refContext = ArgumentCaptor.forClass(EvaluationContext.class);
\t\torder.verify(algorithm).combine(refContext.capture(), eq(Collections.emptyList()));
\t\tassertSame(policyRefContext, refContext.getValue());
\t\torder.verify(context).isExtendedIndeterminateEval();
\t\torder.verify(context).addAdvices(Decision.DENY, Collections.emptyList());
\t\torder.verify(context).addObligations(Decision.DENY, Collections.emptyList());
\t\torder.verify(context).addEvaluationResult(refPolicySet, Decision.DENY);
'''
    body+='''\t\tverifyNoMoreInteractions(context, algorithm);
\t\tassertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
\t\tassertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
\t}
'''
body+='\n'+original_methods('PolicySetIDReferenceTest',{'testObjectMethods'})
replace_file('PolicySetIDReferenceTest',body,common_imports)
print('Real Policy/PolicySet reference tests preserve all original test methods and outcome/metadata assertions.')

