package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.Apply;
import com.artagon.xacml.v3.AttributeAssigmentExpression;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReference;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Condition;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionReference;
import com.artagon.xacml.v3.Match;
import com.artagon.xacml.v3.MatchAllOf;
import com.artagon.xacml.v3.MatchAnyOf;
import com.artagon.xacml.v3.ObligationExpression;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyDefaults;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetDefaults;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.VariableDefinition;
import com.artagon.xacml.v3.VariableReference;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.policy.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.policy.spi.FunctionProvidersRegistry;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultPolicyFactory extends BasePolicyFactory
{
		
	public DefaultPolicyFactory(FunctionProvidersRegistry functions, 
			DecisionCombiningAlgorithmProvider combiningAlgorithms)
	{
		super(functions, combiningAlgorithms);
	}
	
	@Override
	public AttributeValue createAttributeValue(String typeId, Object value) 
		throws PolicySyntaxException
	{
		AttributeValueType type = DataTypes.getByTypeId(typeId);
		if(type == null){
			throw new PolicySyntaxException(
					"TypeId=\"%s\" can not be resolved as an XACML type",typeId);
		}
		try{
			return type.create(value);
		}catch(Exception e){
			throw new PolicySyntaxException(e);
		}
	}

	@Override
	public MatchAllOf createAllOfMatch(Collection<Match> matches) throws PolicySyntaxException 
	{
		return new DefaultMatchAllOf(matches);
	}

	@Override
	public MatchAnyOf createAnyOfMatch(Collection<MatchAllOf> matches)
			throws PolicySyntaxException {
		return new DefaultMatchAnyOf(matches);
	}

	@Override
	public Apply createApply(String functionId, Collection<Expression> arguments)
			throws PolicySyntaxException 
	{
		return new DefaultApply(createFunction(functionId), arguments);
	}

	@Override
	public Match createMatch(String functionId, AttributeValue value, AttributeReference reference)
			throws PolicySyntaxException 
	{
		return new DefaultMatch(createFunction(functionId), value, reference);
	}
	
	@Override
	public PolicySetIDReference createPolicySetIDReference(String policyId,
			VersionMatch version, VersionMatch earliest, VersionMatch latest) 
		throws PolicySyntaxException 
	{
		return new DefaultPolicySetIDReference(policyId, version, earliest, latest);
	}
	
	@Override
	public PolicyIDReference createPolicyIDReference(String policyId,
			VersionMatch version, VersionMatch earliest, VersionMatch latest) 
		throws PolicySyntaxException 
	{
		return new DefaultPolicyIDReference(policyId, version, earliest, latest);
	}
	
	public Target createTarget(Collection<MatchAnyOf> match) throws PolicySyntaxException
	{
		return new DefaultTarget(match);
	}
	
	@Override
	public Condition createCondition(Expression predicate) throws PolicySyntaxException
	{
		if(predicate == null){
			throw new PolicySyntaxException("Condition predicate must be specified");
		}
		if(!predicate.getEvaluatesTo().equals(
				DataTypes.BOOLEAN.getType())){
			throw new PolicySyntaxException(
					"Condition predicate must evaluate to=\"%s\"", DataTypes.BOOLEAN.getType());
		}
		return new DefaultCondition(predicate);
	}
	
	public MatchAnyOf createAnyOf(Collection<MatchAllOf> allOf) throws PolicySyntaxException{
		return new DefaultMatchAnyOf(allOf);
	}
	
	public MatchAllOf createAllOf(Collection<Match> match) throws PolicySyntaxException{
		return new DefaultMatchAllOf(match);
	}
	
	@Override
	public Rule createRule(String ruleId, String description, 
			Target target, Condition condition, Effect effect) 
		throws PolicySyntaxException
	{
		if(ruleId == null){
			throw new PolicySyntaxException("Rule identifier must be specified");
		}
		if(effect == null){
			throw new PolicySyntaxException("Rule id=\"%s\" effect must be specified", ruleId);
		}
		return new DefaultRule(ruleId, description, target, condition, effect);
	}
	
	public Policy createPolicy(
			String policyId, 
			Version version, 
			String description,
			PolicyDefaults policyDefaults, 
			Target target, 
			Collection<VariableDefinition> variables,
			String algorithmId,
			Collection<Rule> rules, 
			Collection<ObligationExpression> obligation, Collection<AdviceExpression> advice) 
		throws PolicySyntaxException
	{
		Policy policy = new DefaultPolicy(
				policyId, 
				version, 
				description, 
				policyDefaults, 
				target, 
				variables,
				createRuleCombingingAlgorithm(algorithmId), 
				rules, advice, obligation);
		return policy;
	}

	
	@Override
	public AttributeAssigmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression,
			AttributeCategoryId categoryId, 
			String issuer)
			throws PolicySyntaxException {
		return new DefaultAttributeAssignmentExpression(
				attributeId, expression, categoryId, issuer);
	}
	
	@Override
	public AttributeAssigmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression) throws PolicySyntaxException {
		return createAttributeAssigmentExpression(attributeId, expression, null, null);
	}
	
	@Override
	public AdviceExpression createAdviceExpression(String id, Effect appliesTo,
			Collection<AttributeAssigmentExpression> attributeAssigments)
			throws PolicySyntaxException {
		return new DefaultAdviceExpression(id, appliesTo, attributeAssigments);
	}

	@Override
	public ObligationExpression createObligationExpression(String id,
			Effect effect,
			Collection<AttributeAssigmentExpression> attributeAssigments)
			throws PolicySyntaxException 
	{
		return new DefaultObligationExpression(id, effect, attributeAssigments);
	}

	@Override
	public PolicySet createPolicySet(String policySetId, 
			Version version, 
			String description,
			PolicySetDefaults policySetDefaults, 
			Target target,
			String algorithmId, 
			Collection<CompositeDecisionRule> policies,
			Collection<ObligationExpression> obligation,
			Collection<AdviceExpression> advice) throws PolicySyntaxException 
	{
		DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm = createPolicyCombingingAlgorithm(algorithmId);
		return new DefaultPolicySet(policySetId, version, description, 
				policySetDefaults, target, null, null, null,
				algorithm, policies, advice, obligation);
	}

	@Override
	public FunctionReference createFunctionReference(String functionId)
			throws PolicySyntaxException {
		return new DefaultFunctionReference(createFunction(functionId));
	}

	@Override
	public AttributeDesignator createAttributeDesignator(AttributeCategoryId category,
			String attributeId, AttributeValueType dataType, boolean mustBePresent, String issuer)
			throws PolicySyntaxException {
		return new DefaultAttributeDesignator(category, attributeId, issuer, dataType, mustBePresent);
	}

	@Override
	public AttributeSelector createAttributeSelector(AttributeCategoryId category,
			String selectXPath, AttributeValueType dataType, boolean mustBePresent)
			throws PolicySyntaxException {
		return new DefaultAttributeSelector(category, selectXPath, dataType, mustBePresent);
	}
	
	@Override
	public VariableReference createVariableReference(VariableDefinition varDef)
			throws PolicySyntaxException 
	{
		return new DefaultVariableReference(varDef);
	}
	
	public VariableDefinition createVariableDefinition(
			String variableId, Expression expression) throws PolicySyntaxException
	{
		if(variableId == null || 
				expression == null){
			throw new PolicySyntaxException("Variable identifier " +
					"or expression can not be null");
		}
		return new DefaultVariableDefinition(variableId, expression);
	}

	@Override
	public PolicyDefaults createPolicyDefaults(Object... objects)
			throws PolicySyntaxException {
		if(objects != null && 
				objects.length > 0){
			if(objects[0] instanceof String){
				String value = (String)objects[0];
				XPathVersion v = XPathVersion.parse(value);
				if(v == null){
					throw new PolicySyntaxException(
							"Unparsable XPath version=\"%s\"", value);
				}
				return new DefaultPolicyDefaults(v);
			}
		}
		return null;
	}
	
	@Override
	public PolicySetDefaults createPolicySetDefaults(Object... objects)
			throws PolicySyntaxException {
		if(objects != null && 
				objects.length > 0){
			if(objects[0] instanceof String){
				String value = (String)objects[0];
				XPathVersion v = XPathVersion.parse(value);
				if(v == null){
					throw new PolicySyntaxException(
							"Unparsable XPath version=\"%s\"", value);
				}
				return new DefaultPolicySetDefaults(v);
			}
		}
		return null;
	}	
}
