package com.artagon.xacml.v3.policy;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeCategoryId;

public interface PolicyFactory 
{
	/**
	 * Creates an {@link AttributeValue} of the
	 * given type initialized with a given value
	 * 
	 * @param typeId an XACML type identifier
	 * @param value an attribute value
	 * @return {@link AttributeValue} instance
	 * 
	 * @throws PolicySyntaxException
	 */
	AttributeValue createValue(String typeId, Object value) 
		throws PolicySyntaxException;
	
	/**
	 * Creates an {@link PolicySetIDReference} to a
	 * given policy set specified via given identifier
	 * 
	 * @param policyId a policy set identifier
	 * @param version a policy set version
	 * @param earliest a policy set earliest version
	 * @param latest a policy set latest version
	 * @return {@link PolicySetIDReference}
	 * @throws PolicySyntaxException
	 */
	PolicySetIDReference createPolicySetIDReference(
			String policyId, 
			VersionMatch version, 
			VersionMatch earliest, 
			VersionMatch latest) throws PolicySyntaxException;
	
	PolicyIDReference createPolicyIDReference(
			String policyId, 
			VersionMatch version, 
			VersionMatch earliest, 
			VersionMatch latest) throws PolicySyntaxException;
	
	Apply createApply(String functionId, 
			Collection<Expression> arguments) throws PolicySyntaxException;
	
	Match createMatch(String functionId, 
			AttributeValue value, AttributeReference reference) throws PolicySyntaxException;
	
	MatchAllOf createAllOfMatch(Collection<Match> matches) 
		throws PolicySyntaxException;
	
	MatchAnyOf createAnyOfMatch(Collection<MatchAllOf> matches) throws PolicySyntaxException;

	Target createTarget(Collection<MatchAnyOf> match) throws PolicySyntaxException;

	Condition createCondition(Expression predicate) throws PolicySyntaxException;

	Rule createRule(String ruleId, Target target, Condition condition, Effect effect)
			throws PolicySyntaxException;

	/**
	 * Creates {@link AttributeAssigmentExpression} instance
	 * for a given attribute id
	 * 
	 * @param attributeId an attribute identifier
	 * @param expression an attribute expression
	 * @param categoryId an attribute category
	 * @param issuer an attribute issuer
	 * @return {@link AttributeAssigmentExpression}
	 * @throws PolicySyntaxException
	 */
	AttributeAssigmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression, 
			AttributeCategoryId categoryId, 
			String issuer) 
		throws PolicySyntaxException;
	
	/**
	 * Creates {@link AdviceExpression} instance
	 * for given advice id, effect and attribute
	 * assignments
	 * 
	 * @param id an advice identifier
	 * @param appliesTo an effect when advice is
	 * applicable
	 * @param attributeAssigments an advice attributes
	 * @return {@link AdviceExpression} instance
	 * @throws PolicySyntaxException
	 */
	AdviceExpression createAdviceExpression(
			String id, 
			Effect appliesTo,  
			Collection<AttributeAssigmentExpression> attributeAssigments) 
		throws PolicySyntaxException;
	
	ObligationExpression createObligationExpression(
			String id, 
			Effect effect,  
			Collection<AttributeAssigmentExpression> attributeAssigments) 
		throws PolicySyntaxException;
	
	/**
	 * Creates {@link Policy} instance
	 * 
	 * @param policyId a policy identifier
	 * @param version a policy version
	 * @param policyDefaults an optional policy defaults
	 * @param target an optional policy target
	 * @param variables an optional policy variables
	 * @param algorithmId a rule combining algorithm identifier
	 * @param rules a collection of policy rules
	 * @param obligation a collection of policy obligations
	 * @param advice a collection of policy advice
	 * @return {@link Policy}
	 * @throws PolicySyntaxException
	 */
	Policy createPolicy(
			String policyId, 
			Version version, 
			PolicyDefaults policyDefaults,
			Target target, 
			Collection<VariableDefinition> variables, 
			String algorithmId, 
			Collection<Rule> rules, 
			Collection<ObligationExpression> obligation,
			Collection<AdviceExpression> advice) throws PolicySyntaxException;
	
	PolicySet createPolicySet(
			String policySetId, 
			Version version, 
			PolicySetDefaults policyDefaults,
			Target target,  
			String algorithmId, 
			Collection<CompositeDecisionRule> policies, 
			Collection<ObligationExpression> obligation,
			Collection<AdviceExpression> advice) throws PolicySyntaxException;

}
