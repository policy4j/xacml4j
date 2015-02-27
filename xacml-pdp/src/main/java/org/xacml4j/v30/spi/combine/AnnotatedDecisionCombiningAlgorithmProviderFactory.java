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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.util.CglibInvocationFactory;
import org.xacml4j.util.Invocation;
import org.xacml4j.util.InvocationFactory;
import org.xacml4j.util.Reflections;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

class AnnotatedDecisionCombiningAlgorithmProviderFactory
{
	private final static Logger log = LoggerFactory.getLogger(AnnotatedDecisionCombiningAlgorithmProviderFactory.class);

	private final InvocationFactory invocationFactory;

	public AnnotatedDecisionCombiningAlgorithmProviderFactory(
			InvocationFactory invocationFactory){
		Preconditions.checkNotNull(invocationFactory);
		this.invocationFactory = invocationFactory;
	}

	public AnnotatedDecisionCombiningAlgorithmProviderFactory(){
		this(new CglibInvocationFactory());
	}

	/**
	 * Creates {@link DecisionCombiningAlgorithmProvider} from a given
	 * class
	 *
	 * @param clazz a class providing decision
	 * combining algorithm implementations
	 * @return {@link DecisionCombiningAlgorithmProvider}
	 */
	public DecisionCombiningAlgorithmProvider create(Class<?> clazz)
	{
		final List<DecisionCombiningAlgorithm> ruleAlgorithms = new LinkedList<DecisionCombiningAlgorithm>();
		for(Method m : Reflections.getAnnotatedMethods(clazz,
				XacmlRuleDecisionCombiningAlgorithm.class)){
			ruleAlgorithms.add(createRuleDecisionCombineAlgorithm(m));
		}
		final List<DecisionCombiningAlgorithm> policyAlgorithms = new LinkedList<DecisionCombiningAlgorithm>();
		for(Method m : Reflections.getAnnotatedMethods(clazz,
				XacmlPolicyDecisionCombiningAlgorithm.class)){
			policyAlgorithms.add(createPolicyDecisionCombineAlgorithm(m));
		}
		return new DecisionCombiningAlgorithmProviderImpl(ruleAlgorithms, policyAlgorithms);
	}


	DecisionCombiningAlgorithm createRuleDecisionCombineAlgorithm(Method m)
	{
		validateDecisionCombiningMethod(m);

		XacmlRuleDecisionCombiningAlgorithm algo = m.getAnnotation(XacmlRuleDecisionCombiningAlgorithm.class);
		Preconditions.checkState(algo != null,
				"Invalid decision combining algorithm method=\"%s\", annotation=\"%s\" must be present",
				m.getName(), XacmlRuleDecisionCombiningAlgorithm.class.getName());
		if(log.isDebugEnabled()){
			log.debug("Creating rule decision combining algorithm=\"{}\" from method=\"{}\"",
					algo.value(), m.getName());
		}
		return createDecisionCombiningAlgorithm(algo.value(), invocationFactory.<Decision>create(null, m));
	}

	DecisionCombiningAlgorithm createPolicyDecisionCombineAlgorithm(Method m)
	{
		validateDecisionCombiningMethod(m);
		XacmlPolicyDecisionCombiningAlgorithm algo = m.getAnnotation(XacmlPolicyDecisionCombiningAlgorithm.class);
		Preconditions.checkState(algo != null,
				"Invalid decision combining algorithm method=\"%s\", annotation=\"%s\" must be present",
				m.getName(), XacmlPolicyDecisionCombiningAlgorithm.class.getName());
		if(log.isDebugEnabled()){
			log.debug("Creating policy decision combining algorithm=\"{}\" from method=\"{}\"",
					algo.value(), m.getName());
		}
		return createDecisionCombiningAlgorithm(algo.value(), invocationFactory.<Decision>create(null, m));
	}

	private DecisionCombiningAlgorithm createDecisionCombiningAlgorithm(
			final String algorithmId, final Invocation<Decision> invocation)
	{
		Preconditions.checkNotNull(algorithmId);
		Preconditions.checkNotNull(invocation);
		return new BaseDecisionCombiningAlgorithm(algorithmId)
		{
			@Override
			public Decision combine(DecisionRuleEvaluationContext context,
                                    List<? extends DecisionRule> decisions) {
				try{
					return invocation.invoke(context, decisions);
				}catch(Exception e){
					if(log.isDebugEnabled()){
						log.debug("Failed to invoke decision combine algorithm=\"{}\"", algorithmId, e);
					}
					return Decision.INDETERMINATE;
				}
			}

		};
	}

	private void validateDecisionCombiningMethod(Method m)
	{
		Preconditions.checkArgument(
				Modifier.isStatic(m.getModifiers()),
				"Decision combining method=\"%s\" must be static", m.getName());
		Preconditions.checkArgument(m.getReturnType().equals(Decision.class),
				"Decision combining method=\"%s\" return type must be=\"%s\"",
				m.getName(), Decision.class);
		Class<?>[] params = m.getParameterTypes();
		Preconditions.checkArgument(params != null && params.length == 2,
				"Decision combining method=\"%s\" must have 2 parameters", m.getName());
		Preconditions.checkArgument(params[0].equals(DecisionRuleEvaluationContext.class),
				"Decision combining method=\"%s\" first parameter must be of type=\"%s\"",
				m.getName(), DecisionRuleEvaluationContext.class);
		Preconditions.checkArgument(params[1].equals(List.class),
				"Decision combining method=\"%s\" first parameter must be of type=\"%s\"",
				m.getName(), List.class);
		Type[] genericTypes = m.getGenericParameterTypes();
		Preconditions.checkArgument((genericTypes[1] instanceof ParameterizedType));
		ParameterizedType decisionList = (ParameterizedType)genericTypes[1];
		if(log.isDebugEnabled()){
			log.debug(decisionList.getActualTypeArguments()[0].getClass().getName());
		}
	}
}
