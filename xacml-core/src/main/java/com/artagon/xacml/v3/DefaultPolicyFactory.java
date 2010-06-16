package com.artagon.xacml.v3;

import java.util.Collection;

import com.artagon.xacml.v3.policy.combine.DefaultDecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.policy.combine.legacy.LegacyDecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.policy.function.DefaultFunctionProvider;
import com.artagon.xacml.v3.policy.spi.combine.DecisionCombiningAlgorithmProviderRegistry;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class DefaultPolicyFactory extends BasePolicyFactory
{
	public DefaultPolicyFactory()
	{
		super(new DefaultFunctionProvider(), 
				new DecisionCombiningAlgorithmProviderRegistry(
						new DefaultDecisionCombiningAlgorithmProvider(), 
						new LegacyDecisionCombiningAlgorithmProvider()));
	}
	
	@Override
	public AttributeValue createAttributeValue(String typeId, Object value) 
		throws PolicySyntaxException
	{
		AttributeValueType type = XacmlDataTypes.getByTypeId(typeId);
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
		return new MatchAllOf(matches);
	}

	@Override
	public MatchAnyOf createAnyOfMatch(Collection<MatchAllOf> matches)
			throws PolicySyntaxException {
		return new MatchAnyOf(matches);
	}

	@Override
	public Apply createApply(String functionId, Collection<Expression> arguments)
			throws PolicySyntaxException 
	{
		return new Apply(createFunction(functionId), arguments);
	}
	
	public Apply createApply(String functionId, 
			Expression ...arguments) throws PolicySyntaxException{
		return new Apply(createFunction(functionId), arguments);
	}

	@Override
	public Match createMatch(String functionId, AttributeValue value, AttributeReference reference)
			throws PolicySyntaxException 
	{
		return new Match(createFunction(functionId), value, reference);
	}
	
	@Override
	public PolicySetIDReference createPolicySetIDReference(String policyId,
			VersionMatch version, VersionMatch earliest, VersionMatch latest) 
		throws PolicySyntaxException 
	{
		return new PolicySetIDReference(policyId, version, earliest, latest);
	}
	
	@Override
	public PolicyIDReference createPolicyIDReference(String policyId,
			VersionMatch version, VersionMatch earliest, VersionMatch latest) 
		throws PolicySyntaxException 
	{
		return new PolicyIDReference(policyId, version, earliest, latest);
	}
	
	public Target createTarget(Collection<MatchAnyOf> match) throws PolicySyntaxException
	{
		return new Target(match);
	}
	
	@Override
	public Condition createCondition(Expression predicate) throws PolicySyntaxException
	{
		if(predicate == null){
			throw new PolicySyntaxException("Condition predicate must be specified");
		}
		if(!predicate.getEvaluatesTo().equals(
				XacmlDataTypes.BOOLEAN.getType())){
			throw new PolicySyntaxException(
					"Condition predicate must evaluate to=\"%s\"", XacmlDataTypes.BOOLEAN.getType());
		}
		return new Condition(predicate);
	}
	
	public MatchAnyOf createAnyOf(Collection<MatchAllOf> allOf) throws PolicySyntaxException{
		return new MatchAnyOf(allOf);
	}
	
	public MatchAllOf createAllOf(Collection<Match> match) throws PolicySyntaxException{
		return new MatchAllOf(match);
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
		return new Rule(ruleId, description, target, condition, effect);
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
		Policy policy = new Policy(
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
	public AttributeAssignmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression,
			AttributeCategoryId categoryId, 
			String issuer)
			throws PolicySyntaxException {
		return new AttributeAssignmentExpression(
				attributeId, expression, categoryId, issuer);
	}
	
	@Override
	public AttributeAssignmentExpression createAttributeAssigmentExpression(
			String attributeId, 
			Expression expression) throws PolicySyntaxException {
		return createAttributeAssigmentExpression(attributeId, expression, null, null);
	}
	
	@Override
	public AdviceExpression createAdviceExpression(String id, Effect appliesTo,
			Collection<AttributeAssignmentExpression> attributeAssigments)
			throws PolicySyntaxException {
		return new AdviceExpression(id, appliesTo, attributeAssigments);
	}

	@Override
	public ObligationExpression createObligationExpression(String id,
			Effect effect,
			Collection<AttributeAssignmentExpression> attributeAssigments)
			throws PolicySyntaxException 
	{
		return new ObligationExpression(id, effect, attributeAssigments);
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
		return new PolicySet(policySetId, version, description, 
				policySetDefaults, target, null, null, null,
				algorithm, policies, advice, obligation);
	}

	@Override
	public FunctionReference createFunctionReference(String functionId)
			throws PolicySyntaxException {
		return new FunctionReference(createFunction(functionId));
	}

	@Override
	public AttributeDesignator createAttributeDesignator(AttributeCategoryId category,
			String attributeId, AttributeValueType dataType, boolean mustBePresent, String issuer)
			throws PolicySyntaxException {
		return new AttributeDesignator(category, attributeId, issuer, dataType, mustBePresent);
	}

	@Override
	public AttributeSelector createAttributeSelector(AttributeCategoryId category,
			String selectXPath, AttributeValueType dataType, boolean mustBePresent)
			throws PolicySyntaxException {
		return new AttributeSelector(category, selectXPath, dataType, mustBePresent);
	}
	
	@Override
	public VariableReference createVariableReference(VariableDefinition varDef)
			throws PolicySyntaxException 
	{
		return new VariableReference(varDef);
	}
	
	public VariableDefinition createVariableDefinition(
			String variableId, Expression expression) throws PolicySyntaxException
	{
		if(variableId == null || 
				expression == null){
			throw new PolicySyntaxException("Variable identifier " +
					"or expression can not be null");
		}
		return new VariableDefinition(variableId, expression);
	}

	@Override
	public PolicyDefaults createPolicyDefaults(Object... objects)
			throws PolicySyntaxException 
	{
		if(objects != null && 
				objects.length > 0){
			if(objects[0] instanceof String){
				String value = (String)objects[0];
				XPathVersion v = XPathVersion.parse(value);
				if(v == null){
					throw new PolicySyntaxException(
							"Unparsable XPath version=\"%s\"", value);
				}
				return new PolicyDefaults(v);
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
				return new PolicySetDefaults(v);
			}
		}
		return null;
	}	
}
