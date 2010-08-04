package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

public interface XacmlFactory 
{
	/**
	 * Creates {@link AttributeValueType} from a given type
	 * identifier
	 * 
	 * @param dataTypeId an XACML type identifier
	 * @return {@link AttributeValueType} instance
	 * @exception XacmlSyntaxException
	 */
	AttributeValueType createAttributeValueType(String dataTypeId) 
		throws XacmlSyntaxException;
	
	Advice createAdvice(String adviceId, 
			Collection<AttributeAssignment> attributes) 
		throws XacmlSyntaxException;
	
	Obligation createObligation(
			String obligationId, 
			Effect effect,
			Collection<AttributeAssignment> attributes) 
		throws XacmlSyntaxException;
	
	
	/**
	 * Creates an {@link AttributeValue} of the
	 * given type initialized with a given value
	 * 
	 * @param typeId an XACML type identifier
	 * @param value an attribute value
	 * @return {@link AttributeValue} instance
	 * 
	 * @throws XacmlSyntaxException
	 */
	AttributeValue createAttributeValue(String typeId, Object value) 
		throws XacmlSyntaxException;
	
	/**
	 * Creates an {@link AttributeValue} of the given type
	 * 
	 * @param typeId a type identifier
	 * @param value an attribute value
	 * @param values an additional information
	 * @return {@link AttributeValue} instance
	 * @throws XacmlSyntaxException if an XACML
	 * syntax error occurs
	 */
	AttributeValue createAttributeValue(String typeId, Object value, Map<QName, String> other) 
		throws XacmlSyntaxException;
	
	/**
	 * Creates {@link AttributeCategoryId} from a given category
	 * identifier
	 * 
	 * @param categoryId a category identifier
	 * @return {@link AttributeCategoryId} instance
	 * @throws XacmlSyntaxException if given category identifier
	 * does not correspond to any known attribute category
	 */
	AttributeCategoryId createAttributeCategory(String categoryId) 
		throws XacmlSyntaxException;
	
	/**
	 * Creates an {@link PolicySetIDReference} to a
	 * given policy set specified via given identifier
	 * 
	 * @param policyId a policy set identifier
	 * @param version a policy set version
	 * @param earliest a policy set earliest version
	 * @param latest a policy set latest version
	 * @return {@link PolicySetIDReference}
	 * @throws XacmlSyntaxException
	 */
	PolicySetIDReference createPolicySetIDReference(
			String policyId, 
			VersionMatch version, 
			VersionMatch earliest, 
			VersionMatch latest) throws XacmlSyntaxException;
	
	PolicyIDReference createPolicyIDReference(
			String policyId, 
			VersionMatch version, 
			VersionMatch earliest, 
			VersionMatch latest) throws XacmlSyntaxException;
	
	Apply createApply(String functionId, 
			Collection<Expression> arguments) throws XacmlSyntaxException;
	
	Apply createApply(String functionId, 
			Expression ...arguments) throws XacmlSyntaxException;
	
	/**
	 * Creates {@link Match} from a given
	 * function, attribute and attribute reference
	 * 
	 * @param functionId
	 * @param value
	 * @param reference
	 * @return {@link Match} instance
	 * @throws XacmlSyntaxException
	 */
	Match createMatch(String functionId, 
			AttributeValue value, AttributeReference reference) throws XacmlSyntaxException;
	
	/**
	 * Creates {@link MatchAnyOf} from a give
	 * @param matches
	 * @return
	 * @throws XacmlSyntaxException
	 */
	MatchAnyOf createAnyOfMatch(Collection<MatchAllOf> matches) throws XacmlSyntaxException;

	Target createTarget(Collection<MatchAnyOf> match) throws XacmlSyntaxException;

	MatchAnyOf createAnyOf(Collection<MatchAllOf> allOf) throws XacmlSyntaxException;
	MatchAllOf createAllOf(Collection<Match> match) throws XacmlSyntaxException;
	
	PolicyDefaults createPolicyDefaults(Object ...objects) 
		throws XacmlSyntaxException;
	
	PolicySetDefaults createPolicySetDefaults(Object ...objects) 
		throws XacmlSyntaxException;
	
	/**
	 * Creates condition with a given predicate
	 * 
	 * @param predicate a predicate expression
	 * @return {@link Condition} instance
	 * @throws XacmlSyntaxException
	 */
	Condition createCondition(Expression predicate) 
		throws XacmlSyntaxException;

	FunctionReference createFunctionReference(String functionId) 
		throws XacmlSyntaxException;
	
	VariableReference createVariableReference(VariableDefinition varDef) 
		throws XacmlSyntaxException;
	
	VariableDefinition createVariableDefinition(
			String variableId, Expression expression) 
		throws XacmlSyntaxException;
	/**
	 * 
	 * @param category
	 * @param attributeId
	 * @param dataType
	 * @param issuer
	 * @param mustBePresent
	 * @return
	 * @throws XacmlSyntaxException
	 */
	AttributeDesignator createAttributeDesignator(AttributeCategoryId category, 
			String attributeId, AttributeValueType dataType, 
			boolean mustBePresent, String issuer) throws XacmlSyntaxException;
	
	/**
	 * Creates {@link AttributeDesignator} instance
	 * 
	 * @param category an attribute category
	 * @param attributeId an attribute identifier
	 * @param dataTypeId an attribute data type identifier
	 * @param mustBePresent a flag 
	 * @param issuer an attribute issuer identifier
	 * @return {@link AttributeDesignator} instance
	 * @throws XacmlSyntaxException
	 */
	AttributeDesignator createAttributeDesignator(String category, 
			String attributeId, String dataTypeId, 
			boolean mustBePresent, String issuer) throws XacmlSyntaxException;

	/**
	 * Creates {@link AttributeSelector}
	 * 
	 * @param category an attribute category
	 * @param selectXPath a selector XPath
	 * @param dataType a data type
	 * @param mustBePresent a flag indicating
	 * if attribute must be present
	 * @return {@link AttributeSelector} instance
	 * @throws XacmlSyntaxException
	 */
	AttributeSelector createAttributeSelector(AttributeCategoryId category, 
			String selectXPath, AttributeValueType dataType, boolean mustBePresent) 
		throws XacmlSyntaxException;
	
	AttributeSelector createAttributeSelector(AttributeCategoryId category, 
			String selectXPath, String contextAttributeId,
			AttributeValueType dataType, boolean mustBePresent) 
		throws XacmlSyntaxException;
	
	AttributeSelector createAttributeSelector(String categoryId, 
			String selectXPath, String dataTypeId, boolean mustBePresent) 
		throws XacmlSyntaxException;
	
	AttributeSelector createAttributeSelector(String categoryId, 
			String selectXPath, String contextAttributeId,
			String dataTypeId, boolean mustBePresent) 
		throws XacmlSyntaxException;
	
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
	 * @throws XacmlSyntaxException
	 */
	Rule createRule(String ruleId, 
			String description, 
			Target target, Condition condition, Effect effect)
			throws XacmlSyntaxException;
	
	/**
	 * Creates {@link AttributeAssignmentExpression} instance
	 * for a given attribute id
	 * 
	 * @param attributeId an attribute identifier
	 * @param expression an attribute expression
	 * @param categoryId an attribute category
	 * @param issuer an attribute issuer
	 * @return {@link AttributeAssigmentExpression}
	 * @throws XacmlSyntaxException
	 */
	AttributeAssignmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression, 
			AttributeCategoryId categoryId, 
			String issuer) 
		throws XacmlSyntaxException;
	
	
	AttributeAssignmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression, 
			String categoryId, 
			String issuer) 
		throws XacmlSyntaxException;
	
	/**
	 * Creates {@link AttributeAssignmentExpression} instance
	 * for a given attribute id
	 * 
	 * @param attributeId an attribute identifier
	 * @param expression an attribute expression
	 * @return {@link AttributeAssigmentExpression}
	 * @throws XacmlSyntaxException
	 */
	AttributeAssignmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression) throws XacmlSyntaxException;
	
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
	 * @throws XacmlSyntaxException
	 */
	AdviceExpression createAdviceExpression(
			String id, 
			Effect appliesTo,  
			Collection<AttributeAssignmentExpression> attributeAssigments) 
		throws XacmlSyntaxException;
	
	ObligationExpression createObligationExpression(
			String id, 
			Effect effect,  
			Collection<AttributeAssignmentExpression> attributeAssigments) 
		throws XacmlSyntaxException;
	
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
	 * @throws XacmlSyntaxException
	 */
	Policy createPolicy(
			String policyId, 
			String version, 
			String description,
			PolicyDefaults policyDefaults,
			Target target, 
			Collection<VariableDefinition> variables, 
			String algorithmId, 
			Collection<Rule> rules, 
			Collection<ObligationExpression> obligation,
			Collection<AdviceExpression> advice) throws XacmlSyntaxException;
	
	PolicySet createPolicySet(
			String policySetId, 
			String version, 
			String description,
			PolicySetDefaults policyDefaults,
			Target target,  
			String algorithmId, 
			Collection<CompositeDecisionRule> policies, 
			Collection<ObligationExpression> obligation,
			Collection<AdviceExpression> advice) throws XacmlSyntaxException;
	
	
	AttributesReference createAttributesReference(String refercenceId) 
		throws XacmlSyntaxException;

}
