package com.artagon.xacml.v30.spi.combine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.Rule;
import com.artagon.xacml.v30.policy.combine.DenyOverrides;

public class AnnotatedDecisionCombiningAlgorithmProviderFactoryTest 
{
	private AnnotatedDecisionCombiningAlgorithmProviderFactory p;
	
	@Before
	public void init(){
		this.p = new AnnotatedDecisionCombiningAlgorithmProviderFactory();
	}
	
	@XacmlPolicyDecisionCombingingAlgorithm("test1Algo")
	public  Decision testNonStaticMethod(EvaluationContext context, List<? super DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@XacmlRuleDecisionCombingingAlgorithm("test2Algo")
	public static Decision testValidMethod(EvaluationContext context, List<DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@XacmlRuleDecisionCombingingAlgorithm("test2Algo")
	public static Decision test2params1(List<DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@XacmlRuleDecisionCombingingAlgorithm("test2algo")
	public static Decision test2params2(EvaluationContext context)
	{
		return Decision.DENY;
	}
	
	@XacmlRuleDecisionCombingingAlgorithm("test4Algo")
	public static Decision test4(EvaluationContext context, Collection<DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void doTest1()
	{	
		p.createPolicyDecisionCombineAlgorithm(
				getMethod(AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.class, "testNonStaticMethod"));
	}
	
	@Test
	public void doTestValidMethod()
	{	
		DecisionCombiningAlgorithm<Rule> a = p.createRuleDecisionCombineAlgorithm(
				getMethod(AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.class, "testValidMethod"));
		assertNotNull(a);
		assertEquals("test2Algo", a.getId());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void doTest2Params1()
	{	
		p.createRuleDecisionCombineAlgorithm(
				getMethod(AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.class, "test2params2"));
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void doTest2Params2()
	{	
		p.createRuleDecisionCombineAlgorithm(
				getMethod(AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.class, "test2params2"));
		
	}
	
	@Test
	public void testParse()
	{
		p.create(DenyOverrides.class);
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
