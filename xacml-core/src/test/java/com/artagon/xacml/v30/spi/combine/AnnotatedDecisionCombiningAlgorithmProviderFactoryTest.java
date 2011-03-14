package com.artagon.xacml.v30.spi.combine;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.artagon.xacml.v30.CompositeDecisionRule;
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
	public  Decision test1(EvaluationContext context, List<? super DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@XacmlRuleDecisionCombingingAlgorithm("test2Algo")
	public static Decision test2(EvaluationContext context, List<DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@XacmlRuleDecisionCombingingAlgorithm("test3Algo")
	public static Decision test3(List<DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@XacmlRuleDecisionCombingingAlgorithm("test4Algo")
	public static Decision test4(EvaluationContext context, Collection<DecisionRule> rules)
	{
		return Decision.DENY;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test1()
	{	
		p.createPolicyDecisionCombineAlgorithm(
				getMethod(AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.class, "test1"));
	}
	
	@Test
	public void test2()
	{	
		DecisionCombiningAlgorithm<Rule> a = p.createRuleDecisionCombineAlgorithm(
				getMethod(AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.class, "test2"));
		assertNotNull(a);
		assertEquals("test2Algo", a.getId());
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
