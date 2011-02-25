package com.artagon.xacml.v30.spi.combine;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.policy.combine.DenyOverrides;

public class AnnotationBasedDecisionCombiningAlgorithmProviderTest 
{
	private AnnotatonBasedDecisionCombiningAlgorithmProvider p;
	
	@Before
	public void init(){
		this.p = new AnnotatonBasedDecisionCombiningAlgorithmProvider();
	}
	
	@XacmlPolicyDecisionCombingingAlgorithm("test1Algo")
	public Decision test1(EvaluationContext context, List<? super DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@XacmlRuleDecisionCombingingAlgorithm("test2Algo")
	public Decision test2(EvaluationContext context, List<DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@Test
	public void test1()
	{	
		DecisionCombiningAlgorithm<CompositeDecisionRule> a = p.createPolicyDecisionCombineAlgorithm(getMethod(AnnotationBasedDecisionCombiningAlgorithmProviderTest.class, "test1"));
		assertNotNull(a);
		assertEquals("test1Algo", a.getId());
	}
	
	@Test
	public void testParse()
	{
		p.parse(DenyOverrides.class);
	}
	
	private static Method getMethod(Class<?> clazz, String name)
	{
		for(Method m : clazz.getDeclaredMethods()){
			if(m.getName().equals(name)){
				return m;
			}
		}
		return null;
	}
}
