package com.artagon.xacml.v3.policy.jaxb;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.policy.ApplyType;
import org.oasis.xacml.v20.policy.AttributeDesignatorType;
import org.oasis.xacml.v20.policy.AttributeSelectorType;
import org.oasis.xacml.v20.policy.DefaultsType;
import org.oasis.xacml.v20.policy.FunctionType;
import org.oasis.xacml.v20.policy.ObligationsType;
import org.oasis.xacml.v20.policy.PolicySetType;
import org.oasis.xacml.v20.policy.PolicyType;
import org.oasis.xacml.v20.policy.SubjectAttributeDesignatorType;
import org.oasis.xacml.v20.policy.TargetType;
import org.oasis.xacml.v20.policy.VariableDefinitionType;

import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionReference;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyDefaults;
import com.artagon.xacml.v3.policy.PolicyFactory;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySyntaxException;
import com.artagon.xacml.v3.policy.Rule;
import com.artagon.xacml.v3.policy.Target;
import com.artagon.xacml.v3.policy.VariableDefinition;
import com.artagon.xacml.v3.policy.VariableReference;
import com.google.common.collect.Iterables;

public class JAXBPolicyMapper 
{

	private PolicyFactory factory;
	
	public JAXBPolicyMapper(PolicyFactory factory){
		this.factory = factory;
	}
	
	public Policy create(PolicyType p) throws PolicySyntaxException
	{
		return null;
	}
	
	public PolicySet create(PolicySetType xmlPolicy) throws PolicySyntaxException
	{
		return null;
	}
	
	private PolicyDefaults create(DefaultsType defaults)
	{
		return null;
	}
	
	private Target create(TargetType target)
	{
		target.getActions();
		return null;
	}
	
	private Collection<VariableDefinition> getVariables(PolicyType p) throws PolicySyntaxException
	{
		List<Object> objects = p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
		if(objects == null || objects.isEmpty()){
			return Collections.emptyList();
		}
		List<VariableDefinition> variables = new LinkedList<VariableDefinition>();
		for(Object o : objects){
			if(!(o instanceof VariableDefinitionType)){
				continue;
			}
			VariableDefinitionType v = (VariableDefinitionType)o;
			variables.add(new VariableDefinition(v.getVariableId(), createExpression(v.getExpression())));
		}
		return variables;
	}
	
	private Collection<Rule> getRules(PolicyType p)
	{
		return Collections.emptyList();
	}
	
	private Collection<Obligation> getObligations(ObligationsType obligations)
	{
		return Collections.emptyList();
	}
	
	private Expression createExpression(JAXBElement<?> expression) 
		throws PolicySyntaxException
	{
		Object exp = expression.getValue();
		if(exp instanceof org.oasis.xacml.v20.policy.AttributeValueType){
			org.oasis.xacml.v20.policy.AttributeValueType a = (org.oasis.xacml.v20.policy.AttributeValueType)exp;
			List<Object> content = a.getContent();
			if(content == null || 
					content.isEmpty()){
				throw new PolicySyntaxException("Attribute does not have content");
			}
			return factory.createValue(a.getDataType(), Iterables.getOnlyElement(content));
		}
		if(exp instanceof ApplyType){
			ApplyType apply = (ApplyType)exp;
			List<Expression> arguments = new LinkedList<Expression>();
			for(JAXBElement<?> arg : apply.getExpression()){
				arguments.add(createExpression(arg));
			}
			return factory.createApply(apply.getFunctionId(), arguments);
		}
		if(exp instanceof FunctionType){
			FunctionType f = (FunctionType)exp;
			return factory.createFunctionReference(f.getFunctionId());
		}
		if(exp instanceof SubjectAttributeDesignatorType){
			
		}
		if(exp instanceof AttributeDesignatorType){
			
		}
		if(exp instanceof AttributeSelectorType){
			
		}
		if(exp instanceof VariableReference){
			
		}
		throw new PolicySyntaxException("Unsupported expression=\"%s\"", expression.getName());
	}
}
