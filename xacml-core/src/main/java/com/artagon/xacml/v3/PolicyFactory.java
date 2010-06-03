package com.artagon.xacml.v3;

import java.util.Collection;


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
	AttributeValue createAttributeValue(String typeId, Object value) 
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
	
	Apply createApply(String functionId, 
			Expression ...arguments) throws PolicySyntaxException;
	
	/**
	 * Creates {@link Match} from a given
	 * function, attribute and attribute reference
	 * 
	 * @param functionId
	 * @param value
	 * @param reference
	 * @return {@link Match} instance
	 * @throws PolicySyntaxException
	 */
	Match createMatch(String functionId, 
			AttributeValue value, AttributeReference reference) throws PolicySyntaxException;
	
	MatchAllOf createAllOfMatch(Collection<Match> matches) 
		throws PolicySyntaxException;
	
	MatchAnyOf createAnyOfMatch(Collection<MatchAllOf> matches) throws PolicySyntaxException;

	Target createTarget(Collection<MatchAnyOf> match) throws PolicySyntaxException;

	MatchAnyOf createAnyOf(Collection<MatchAllOf> allOf) throws PolicySyntaxException;
	MatchAllOf createAllOf(Collection<Match> match) throws PolicySyntaxException;
	
	PolicyDefaults createPolicyDefaults(Object ...objects) 
		throws PolicySyntaxException;
	
	PolicySetDefaults createPolicySetDefaults(Object ...objects) 
		throws PolicySyntaxException;
	
	/**
	 * Creates condition with a given predicate
	 * 
	 * @param predicate a predicate expression
	 * @return {@link Condition} instance
	 * @throws PolicySyntaxException
	 */
	Condition createCondition(Expression predicate) 
		throws PolicySyntaxException;

	FunctionReference createFunctionReference(String functionId) 
		throws PolicySyntaxException;
	
	VariableReference createVariableReference(VariableDefinition varDef) 
		throws PolicySyntaxException;
	
	VariableDefinition createVariableDefinition(
			String variableId, Expression expression) 
		throws PolicySyntaxException;
	/**
	 * 
	 * @param category
	 * @param attributeId
	 * @param dataType
	 * @param issuer
	 * @param mustBePresent
	 * @return
	 * @throws PolicySyntaxException
	 */
	AttributeDesignator createAttributeDesignator(AttributeCategoryId category, 
			String attributeId, AttributeValueType dataType, 
			boolean mustBePresent, String issuer) throws PolicySyntaxException;
	
	
	/**
	 * Creates {@link AttributeSelector}
	 * 
	 * @param category an attribute category
	 * @param selectXPath a selector XPath
	 * @param dataType a data type
	 * @param mustBePresent a flag indicating
	 * if attribute must be present
	 * @return {@link AttributeSelector} instance
	 * @throws PolicySyntaxException
	 */
	AttributeSelector createAttributeSelector(AttributeCategoryId category, 
			String selectXPath, AttributeValueType dataType, boolean mustBePresent) 
		throws PolicySyntaxException;
	
	/**
	 * Creates {@link Rule} instance with a give 
	 * target, condition and effect
	 * 
	 * @param ruleId a rule identifier
	 * @param description a rule description
	 * @param target a rule target
	 * @param condition a rule condition
	 * @param effect a rule effect
	 * @return {@link Rule} instance
	 * @throws PolicySyntaxException
	 */
	Rule createRule(String ruleId, 
			String description, 
			Target target, Condition condition, Effect effect)
			throws PolicySyntaxException;
	
	/**
	 * Creates {@link AttributeAssignmentExpression} instance
	 * for a given attribute id
	 * 
	 * @param attributeId an attribute identifier
	 * @param expression an attribute expression
	 * @param categoryId an attribute category
	 * @param issuer an attribute issuer
	 * @return {@link AttributeAssigmentExpression}
	 * @throws PolicySyntaxException
	 */
	AttributeAssignmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression, 
			AttributeCategoryId categoryId, 
			String issuer) 
		throws PolicySyntaxException;
	
	
	/**
	 * Creates {@link AttributeAssignmentExpression} instance
	 * for a given attribute id
	 * 
	 * @param attributeId an attribute identifier
	 * @param expression an attribute expression
	 * @return {@link AttributeAssigmentExpression}
	 * @throws PolicySyntaxException
	 */
	AttributeAssignmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression) throws PolicySyntaxException;
	
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
			Collection<AttributeAssignmentExpression> attributeAssigments) 
		throws PolicySyntaxException;
	
	ObligationExpression createObligationExpression(
			String id, 
			Effect effect,  
			Collection<AttributeAssignmentExpression> attributeAssigments) 
		throws PolicySyntaxException;
	
	/**
	 * Creates {@link Policy} instance
	 * 
	 * @param policyId a policy identifier
	 * @param version a policy version
	 * @param description a policy description
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
			String description,
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
			String description,
			PolicySetDefaults policyDefaults,
			Target target,  
			String algorithmId, 
			Collection<CompositeDecisionRule> policies, 
			Collection<ObligationExpression> obligation,
			Collection<AdviceExpression> advice) throws PolicySyntaxException;

}
