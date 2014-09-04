package org.xacml4j.v30.spi.combine;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.policy.combine.DenyOverrides;


public class AnnotatedDecisionCombiningAlgorithmProviderFactoryTest
{
	private AnnotatedDecisionCombiningAlgorithmProviderFactory p;

	@Before
	public void init(){
		this.p = new AnnotatedDecisionCombiningAlgorithmProviderFactory();
	}

	@XacmlPolicyDecisionCombiningAlgorithm("test1Algo")
	public  Decision testNonStaticMethod(DecisionRuleEvaluationContext context, List<? super DecisionRule> rules)
	{
		return Decision.DENY;
	}

	@XacmlRuleDecisionCombiningAlgorithm("test2Algo")
	public static Decision testValidMethod(DecisionRuleEvaluationContext context, List<DecisionRule> rules)
	{
		return Decision.DENY;
	}

	@XacmlRuleDecisionCombiningAlgorithm("test2Algo")
	public static Decision test2params1(List<DecisionRule> rules)
	{
		return Decision.DENY;
	}

	@XacmlRuleDecisionCombiningAlgorithm("test2algo")
	public static Decision test2params2(DecisionRuleEvaluationContext context)
	{
		return Decision.DENY;
	}

	@XacmlRuleDecisionCombiningAlgorithm("test4Algo")
	public static Decision test4(DecisionRuleEvaluationContext context, Collection<DecisionRule> rules)
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
