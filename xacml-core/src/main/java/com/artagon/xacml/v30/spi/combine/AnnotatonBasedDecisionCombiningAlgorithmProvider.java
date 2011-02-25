package com.artagon.xacml.v30.spi.combine;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.DefaultInvocationFactory;
import com.artagon.xacml.util.Invocation;
import com.artagon.xacml.util.InvocationFactory;
import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.Rule;
import com.google.common.base.Preconditions;

public class AnnotatonBasedDecisionCombiningAlgorithmProvider 
{
	private final static Logger log = LoggerFactory.getLogger(AnnotatonBasedDecisionCombiningAlgorithmProvider.class);
	
	private InvocationFactory invocationFactory;
	
	public AnnotatonBasedDecisionCombiningAlgorithmProvider(InvocationFactory invocationFactory){
		Preconditions.checkNotNull(invocationFactory);
		this.invocationFactory = invocationFactory;
	}
	
	public AnnotatonBasedDecisionCombiningAlgorithmProvider(){
		this(new DefaultInvocationFactory());
	}
	
	public DecisionCombiningAlgorithmProvider parse(Class<?> clazz)
	{
		final List<DecisionCombiningAlgorithm<Rule>> ruleAlgorithms = new LinkedList<DecisionCombiningAlgorithm<Rule>>();
		for(Method m : Reflections.getAnnotatedMethods(clazz, XacmlRuleDecisionCombingingAlgorithm.class)){
			ruleAlgorithms.add(createRuleDecisionCombineAlgorithm(m));
		}
		final List<DecisionCombiningAlgorithm<CompositeDecisionRule>> policyAlgorithms = new LinkedList<DecisionCombiningAlgorithm<CompositeDecisionRule>>();
		for(Method m : Reflections.getAnnotatedMethods(clazz, XacmlPolicyDecisionCombingingAlgorithm.class)){
			policyAlgorithms.add(createPolicyDecisionCombineAlgorithm(m));
		}
		return new BaseDecisionCombingingAlgorithmProvider(ruleAlgorithms, policyAlgorithms);
	}
	
	
	DecisionCombiningAlgorithm<Rule> createRuleDecisionCombineAlgorithm(Method m)
	{
		validateDecisionCombineMethod(m);
	
		XacmlRuleDecisionCombingingAlgorithm algo = m.getAnnotation(XacmlRuleDecisionCombingingAlgorithm.class);
		Preconditions.checkState(algo != null, 
				"Invalid decision combining algorithm method=\"%s\", annotiation=\"%s\" must be present", 
				m.getName(), XacmlRuleDecisionCombingingAlgorithm.class.getName());
		if(log.isDebugEnabled()){
			log.debug("Creating rule decision combining" +
					" algorithm=\"{}\" from method=\"{}\"", algo.value(), m.getName());
		}
		return createDecisionCombineAlgorithm(algo.value(), invocationFactory.<Decision>create(null, m));
	}
	
	DecisionCombiningAlgorithm<CompositeDecisionRule> createPolicyDecisionCombineAlgorithm(Method m)
	{
		validateDecisionCombineMethod(m);
		XacmlPolicyDecisionCombingingAlgorithm algo = m.getAnnotation(XacmlPolicyDecisionCombingingAlgorithm.class);
		Preconditions.checkState(algo != null, 
				"Invalid decision combining algorithm method=\"%s\", annotiation=\"%s\" must be present", 
				m.getName(), XacmlPolicyDecisionCombingingAlgorithm.class.getName());
		if(log.isDebugEnabled()){
			log.debug("Creating policy decision combining" +
					" algorithm=\"{}\" from method=\"{}\"", algo.value(), m.getName());
		}
		return createDecisionCombineAlgorithm(algo.value(), invocationFactory.<Decision>create(null, m));
	}
	
	private  <D extends DecisionRule> DecisionCombiningAlgorithm<D> createDecisionCombineAlgorithm(
			final String algorithmId,  final Invocation<Decision> invocation)
	{
		Preconditions.checkNotNull(algorithmId);
		Preconditions.checkNotNull(invocation);
		return new BaseDecisionCombiningAlgorithm<D>(algorithmId) 
		{			
			@Override
			public Decision combine(EvaluationContext context, List<D> decisions) {
				try{
					return invocation.invoke(context, decisions);
				}catch(Exception e){
					if(log.isDebugEnabled()){
						log.debug("Failed to invoke " +
								"decision combine algorithm=\"{}\"", algorithmId);
					}
					return Decision.INDETERMINATE;
				}
			}
			
		};
	}
	
	private void validateDecisionCombineMethod(Method m)
	{
		Preconditions.checkArgument(m.getReturnType().equals(Decision.class),
				"Decision combine method=\"%s\" return type must be=\"%s\"", 
				m.getName(), Decision.class);
		Class<?>[] params = m.getParameterTypes();
		Preconditions.checkArgument(params != null && params.length == 2, 
				"Decision combine method=\"%s\" must have 2 parameters", m.getName());
		Preconditions.checkArgument(params[0].equals(EvaluationContext.class), 
				"Decision combine method=\"%s\" first parameter must be of type=\"%s\"", 
				m.getName(), EvaluationContext.class);
		Preconditions.checkArgument(params[1].equals(List.class), 
				"Decision combine method=\"%s\" first parameter must be of type=\"%s\"", 
				m.getName(), List.class);
		Type[] genericTypes = m.getGenericParameterTypes();
		Preconditions.checkArgument((genericTypes[1] instanceof ParameterizedType));
		ParameterizedType decisionList = (ParameterizedType)genericTypes[1];
		if(log.isDebugEnabled()){
			log.debug(decisionList.getActualTypeArguments()[0].getClass().getName());
		}
	}
}
