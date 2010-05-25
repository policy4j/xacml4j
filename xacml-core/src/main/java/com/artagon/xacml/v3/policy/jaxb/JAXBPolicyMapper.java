package com.artagon.xacml.v3.policy.jaxb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.policy.ActionMatchType;
import org.oasis.xacml.v20.policy.ActionType;
import org.oasis.xacml.v20.policy.ActionsType;
import org.oasis.xacml.v20.policy.ApplyType;
import org.oasis.xacml.v20.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.policy.AttributeDesignatorType;
import org.oasis.xacml.v20.policy.AttributeSelectorType;
import org.oasis.xacml.v20.policy.ConditionType;
import org.oasis.xacml.v20.policy.DefaultsType;
import org.oasis.xacml.v20.policy.EffectType;
import org.oasis.xacml.v20.policy.EnvironmentMatchType;
import org.oasis.xacml.v20.policy.EnvironmentType;
import org.oasis.xacml.v20.policy.EnvironmentsType;
import org.oasis.xacml.v20.policy.FunctionType;
import org.oasis.xacml.v20.policy.ObligationType;
import org.oasis.xacml.v20.policy.ObligationsType;
import org.oasis.xacml.v20.policy.PolicySetType;
import org.oasis.xacml.v20.policy.PolicyType;
import org.oasis.xacml.v20.policy.ResourceMatchType;
import org.oasis.xacml.v20.policy.ResourceType;
import org.oasis.xacml.v20.policy.ResourcesType;
import org.oasis.xacml.v20.policy.RuleType;
import org.oasis.xacml.v20.policy.SubjectAttributeDesignatorType;
import org.oasis.xacml.v20.policy.SubjectMatchType;
import org.oasis.xacml.v20.policy.SubjectType;
import org.oasis.xacml.v20.policy.SubjectsType;
import org.oasis.xacml.v20.policy.TargetType;
import org.oasis.xacml.v20.policy.VariableDefinitionType;
import org.oasis.xacml.v20.policy.VariableReferenceType;

import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.Apply;
import com.artagon.xacml.v3.AttributeAssigmentExpression;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Condition;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.Match;
import com.artagon.xacml.v3.MatchAllOf;
import com.artagon.xacml.v3.MatchAnyOf;
import com.artagon.xacml.v3.ObligationExpression;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyDefaults;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetDefaults;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.VariableDefinition;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.google.common.collect.Iterables;

public class JAXBPolicyMapper 
{

	private final static Map<String, AttributeCategoryId> designatorMappings = new HashMap<String, AttributeCategoryId>();
	
	static{
		designatorMappings.put("ResourceAttributeDesignator", AttributeCategoryId.RESOURCE);
		designatorMappings.put("ActionAttributeDesignator", AttributeCategoryId.ACTION);
		designatorMappings.put("EnviromentAttributeDesignator", AttributeCategoryId.ENVIRONMENT);
	}
	
	private PolicyFactory factory;
	
	public JAXBPolicyMapper(PolicyFactory factory){
		this.factory = factory;
	}
	
	public Policy create(PolicyType p) throws PolicySyntaxException
	{
		Map<String, VariableDefinition> varDefinitions = getVariables(p);
		Version version = Version.valueOf(p.getVersion());
		Collection<ObligationExpression> obligations = getObligations(p.getObligations());
		PolicyDefaults policyDefaults = createPolicyDefaults(p.getPolicyDefaults());
		Target target = create(p.getTarget());
		return factory.createPolicy(
				p.getPolicyId(), 
				version, 
				policyDefaults,
				target,
				varDefinitions.values(), 
				p.getRuleCombiningAlgId(), 
				getRules(p), obligations, 
				Collections.<AdviceExpression>emptyList());
	}
	
	public PolicySet create(PolicySetType p) throws PolicySyntaxException
	{
		Version version = Version.valueOf(p.getVersion());
		Collection<ObligationExpression> obligations = getObligations(p.getObligations());
		PolicySetDefaults policySetDefaults = createPolicySetDefaults(p.getPolicySetDefaults());
		Collection<CompositeDecisionRule> policies = getPolicies(p);
		Target target = create(p.getTarget());
		return factory.createPolicySet(
				p.getPolicySetId(), 
				version, policySetDefaults, 
				target, p.getPolicyCombiningAlgId(), 
				policies, obligations, Collections.<AdviceExpression>emptyList());
	}
	
	private Collection<CompositeDecisionRule> getPolicies(PolicySetType p) throws PolicySyntaxException
	{
		p.getPolicySetOrPolicyOrPolicySetIdReference();
		return Collections.emptyList();
	}
	
	private PolicyDefaults createPolicyDefaults(DefaultsType defaults)
	{
		return null;
	}
	
	private PolicySetDefaults createPolicySetDefaults(DefaultsType defaults)
	{
		return null;
	}
	
	private Target create(TargetType target) throws PolicySyntaxException
	{
		Collection<MatchAnyOf> match = new LinkedList<MatchAnyOf>();
		ActionsType actions = target.getActions();
		if(actions != null){
			match.add(create(actions));
		}
		EnvironmentsType env = target.getEnvironments();
		if(env != null){
			match.add(create(env));
		}
		ResourcesType resources = target.getResources();
		if(resources != null){
			match.add(create(resources));
		}
		SubjectsType subjects = target.getSubjects();
		if(subjects != null){
			match.add(create(subjects));
		}
		return factory.createTarget(match);
	}
	
	private MatchAnyOf create(ActionsType actions) throws PolicySyntaxException
	{
		if(actions == null){
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for(ActionType action : actions.getAction())
		{
			Collection<Match> matches = new LinkedList<Match>();
			for(ActionMatchType match : action.getActionMatch()){
				matches.add(createMatch(match));
			}
			allOf.add(factory.createAllOf(matches));
		}
		return factory.createAnyOf(allOf);
	}
	
	private MatchAnyOf create(ResourcesType resources) throws PolicySyntaxException
	{
		if(resources == null){
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for(ResourceType action : resources.getResource())
		{
			Collection<Match> matches = new LinkedList<Match>();
			for(ResourceMatchType match : action.getResourceMatch()){
				matches.add(createMatch(match));
			}
			allOf.add(factory.createAllOf(matches));
		}
		return factory.createAnyOf(allOf);
	}
	
	private MatchAnyOf create(SubjectsType resources) throws PolicySyntaxException
	{
		if(resources == null){
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for(SubjectType action : resources.getSubject())
		{
			Collection<Match> matches = new LinkedList<Match>();
			for(SubjectMatchType match : action.getSubjectMatch()){
				matches.add(createMatch(match));
			}
			allOf.add(factory.createAllOf(matches));
		}
		return factory.createAnyOf(allOf);
	}
	
	private MatchAnyOf create(EnvironmentsType actions) throws PolicySyntaxException
	{
		if(actions == null){
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for(EnvironmentType action : actions.getEnvironment())
		{
			Collection<Match> matches = new LinkedList<Match>();
			for(EnvironmentMatchType match : action.getEnvironmentMatch()){
				matches.add(createMatch(match));
			}
			allOf.add(factory.createAllOf(matches));
		}
		return factory.createAnyOf(allOf);
	}
	
	private Map<String, VariableDefinition> getVariables(PolicyType p) throws PolicySyntaxException
	{
		List<Object> objects = p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
		if(objects == null || objects.isEmpty()){
			return Collections.emptyMap();
		}
		Map<String, VariableDefinition> variables = new HashMap<String, VariableDefinition>();
		for(Object o : objects){
			if(!(o instanceof VariableDefinitionType)){
				continue;
			}
			VariableDefinitionType v = (VariableDefinitionType)o;
			variables.put(v.getVariableId(), new VariableDefinition(v.getVariableId(), 
					createExpression(v.getExpression())));
		}
		return variables;
	}
	
	private Collection<Rule> getRules(PolicyType p) throws PolicySyntaxException
	{
		Collection<Object> objects = p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
		Collection<Rule> rules = new LinkedList<Rule>();
		for(Object o : objects){
			if(o instanceof RuleType){
				rules.add(create((RuleType)o));
			}
		}
		return rules;
	}
	
	private Rule create(RuleType r) throws PolicySyntaxException
	{
		Effect effect  = r.getEffect() == EffectType.DENY?Effect.DENY:Effect.PERMIT;
		return factory.createRule(r.getRuleId(), 
				create(r.getTarget()), 
				create(r.getCondition()),
				effect);
	}
	
	private Condition create(ConditionType c) throws PolicySyntaxException
	{
		if(c == null){
			return null;
		}
		JAXBElement<?> expression = c.getExpression();
		return factory.createCondition(createExpression(expression));
	}
	
	private Collection<ObligationExpression> getObligations(ObligationsType obligations) 
		throws PolicySyntaxException
	{
		if(obligations == null){
			return Collections.<ObligationExpression>emptyList();
		}
		Collection<ObligationExpression> o = new LinkedList<ObligationExpression>();
		for(ObligationType obligation : obligations.getObligation()){
			o.add(factory.createObligationExpression(obligation.getObligationId(), 
					obligation.getFulfillOn() == EffectType.PERMIT?Effect.PERMIT:Effect.DENY,
							createAttributeAssigments(obligation.getAttributeAssignment())));
		}	
		return o;
	}
	
	private Collection<AttributeAssigmentExpression> createAttributeAssigments(Collection<AttributeAssignmentType> exp)
	{
		return Collections.emptyList();
	}
	
	Expression createExpression(JAXBElement<?> expression) 
		throws PolicySyntaxException
	{
		Object exp = expression.getValue();
		if(exp instanceof org.oasis.xacml.v20.policy.AttributeValueType){
			return createValue((org.oasis.xacml.v20.policy.AttributeValueType)exp);
		}
		if(exp instanceof ApplyType){
			return createApply((ApplyType)exp);
		}
		if(exp instanceof FunctionType){
			FunctionType f = (FunctionType)exp;
			return factory.createFunctionReference(f.getFunctionId());
		}
		if(exp instanceof AttributeDesignatorType){
			AttributeCategoryId categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType)exp);
		}
		if(exp instanceof AttributeSelectorType){
			AttributeCategoryId categoryId = getSelectoryCategory((AttributeSelectorType)exp);
			return createSelector(categoryId, (AttributeSelectorType)exp);
		}
		if(exp instanceof VariableReferenceType){
			throw new PolicySyntaxException("Unsupported");
		}
		throw new PolicySyntaxException(
				"Unsupported expression=\"%s\"", expression.getName());
	}
	
	Match createMatch(Object exp) throws PolicySyntaxException
	{
		if(exp instanceof SubjectMatchType){
			SubjectMatchType match = (SubjectMatchType)exp;
			SubjectAttributeDesignatorType desig = match.getSubjectAttributeDesignator();
			if(desig != null){
				AttributeCategoryId categoryId = AttributeCategoryId.parse(
						desig.getSubjectCategory());
				if(categoryId == null){
					throw new PolicySyntaxException("Unknown subject " +
							"attribute designator category=\"%s\"", 
							desig.getSubjectCategory());
				}
				return factory.createMatch(match.getMatchId(), 
						createValue(match.getAttributeValue()), 
						createDesignator(categoryId, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if(selector != null){
				return factory.createMatch(match.getMatchId(), 
						createValue(match.getAttributeValue()), 
						createSelector(getSelectoryCategory(selector), selector));
			}
			throw new PolicySyntaxException("Match with functionId=\"%s\" " +
					"does not have designator or selector", match.getMatchId());
		}
		if(exp instanceof ActionMatchType){
			ActionMatchType match = (ActionMatchType)exp;
			AttributeDesignatorType desig = match.getActionAttributeDesignator();
			if(desig != null){
				return factory.createMatch(match.getMatchId(), 
						createValue(match.getAttributeValue()), 
						createDesignator(AttributeCategoryId.ACTION, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if(selector != null){
				return factory.createMatch(match.getMatchId(), 
						createValue(match.getAttributeValue()), 
						createSelector(getSelectoryCategory(selector), selector));
			}
			throw new PolicySyntaxException("Match with functionId=\"%s\" " +
					"does not have designator or selector", match.getMatchId());
		}
		if(exp instanceof ResourceMatchType){
			ResourceMatchType match = (ResourceMatchType)exp;
			AttributeDesignatorType desig = match.getResourceAttributeDesignator();
			if(desig != null){
				return factory.createMatch(match.getMatchId(), 
						createValue(match.getAttributeValue()), 
						createDesignator(AttributeCategoryId.RESOURCE, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if(selector != null){
				return factory.createMatch(match.getMatchId(), 
						createValue(match.getAttributeValue()), 
						createSelector(getSelectoryCategory(selector), selector));
			}
			throw new PolicySyntaxException("Match with functionId=\"%s\" " +
					"does not have designator or selector", match.getMatchId());
		}
		if(exp instanceof EnvironmentMatchType){
			EnvironmentMatchType match = (EnvironmentMatchType)exp;
			AttributeDesignatorType desig = match.getEnvironmentAttributeDesignator();
			if(desig != null){
				return factory.createMatch(match.getMatchId(), 
						createValue(match.getAttributeValue()), 
						createDesignator(AttributeCategoryId.ENVIRONMENT, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if(selector != null){
				return factory.createMatch(match.getMatchId(), 
						createValue(match.getAttributeValue()), 
						createSelector(getSelectoryCategory(selector), selector));
			}
			throw new PolicySyntaxException("Match with functionId=\"%s\" " +
					"does not have designator or selector", match.getMatchId());
		}
		throw new PolicySyntaxException("Can't create Match from a given instance=\"%s\"", exp);
	}
	
	Apply createApply(ApplyType apply) throws PolicySyntaxException
	{
		List<Expression> arguments = new LinkedList<Expression>();
		for(JAXBElement<?> arg : apply.getExpression()){
			arguments.add(createExpression(arg));
		}
		return factory.createApply(apply.getFunctionId(), arguments);
	}
	
	/**
	 * Creates {@link AttributeValue} from a given
	 * {@link JAXBElement}
	 * 
	 * @param element a JAXB element
	 * @return {@link AttributeValue}
	 * @throws PolicySyntaxException
	 */
	AttributeValue createValue(org.oasis.xacml.v20.policy.AttributeValueType value) 
		throws PolicySyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new PolicySyntaxException("Attribute does not have content");
		}
		return factory.createValue(value.getDataType(), Iterables.getOnlyElement(content));
	}
	
	AttributeSelector createSelector(AttributeCategoryId categoryId, 
			AttributeSelectorType selector) throws PolicySyntaxException
	{
		AttributeValueType dataType = DataTypes.getByTypeId(selector.getDataType());
		if(dataType == null){
			throw new PolicySyntaxException("Unknown dataType=\"%s\"", 
					selector.getDataType());
		}
		String xpath = transformSelectorXPath(selector);
		return factory.createSelector(categoryId, xpath, dataType, selector.isMustBePresent());
	}
	
	AttributeCategoryId getSelectoryCategory(AttributeSelectorType selector){
		return AttributeCategoryId.RESOURCE;
	}
	
	String transformSelectorXPath(AttributeSelectorType selector){
		return selector.getRequestContextPath();
	}
	
	/**
	 * Creates {@link AttributeDesignator} from a given {@link JAXBElement}
	 * 
	 * @param element a JAXB element
	 * @return {@link AttributeDesignator} instance
	 * @throws PolicySyntaxException
	 */
	AttributeDesignator createDesignator(AttributeCategoryId categoryId, 
			AttributeDesignatorType ref) throws PolicySyntaxException
	{
		AttributeValueType dataType = DataTypes.getByTypeId(ref.getDataType());
		if(dataType == null){
			throw new PolicySyntaxException(
					"Unknown dataType=\"%s\"", ref.getDataType());
		}
		return factory.createDesignator(categoryId, 
				ref.getAttributeId(), dataType, ref.isMustBePresent(), ref.getIssuer());
	}
	
	AttributeCategoryId getDesignatorCategory(JAXBElement<?> element) 
		throws PolicySyntaxException
	{
		Object ref = element.getValue();
		if(ref instanceof SubjectAttributeDesignatorType){	
			SubjectAttributeDesignatorType subjectRef = (SubjectAttributeDesignatorType)ref;
			AttributeCategoryId categoryId = AttributeCategoryId.valueOf(subjectRef.getSubjectCategory());
			if(categoryId == null){
				throw new PolicySyntaxException("Unknown subject " +
						"attribute designator category=\"%s\"", ref);
			}
		}
		AttributeCategoryId categoryId  = designatorMappings.get(element.getName().getLocalPart());
		if(categoryId == null){
			throw new PolicySyntaxException("Unknown attribute designator=\"%s\"", element.getName());
		}
		return categoryId;
	}
}
