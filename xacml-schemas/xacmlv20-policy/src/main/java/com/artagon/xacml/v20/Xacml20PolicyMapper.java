package com.artagon.xacml.v20;

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
import org.oasis.xacml.v20.policy.IdReferenceType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.VariableManager;
import com.artagon.xacml.util.Xacml20XPathTo30Transformer;
import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.Apply;
import com.artagon.xacml.v3.AttributeAssignmentExpression;
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
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetDefaults;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.VariableDefinition;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

class Xacml20PolicyMapper 
{
	private final static Logger log = LoggerFactory.getLogger(Xacml20PolicyMapper.class);
	
	private final static Map<String, AttributeCategoryId> designatorMappings = new HashMap<String, AttributeCategoryId>();

	static {
		designatorMappings.put("ResourceAttributeDesignator",
				AttributeCategoryId.RESOURCE);
		designatorMappings.put("ActionAttributeDesignator",
				AttributeCategoryId.ACTION);
		designatorMappings.put("EnvironmentAttributeDesignator",
				AttributeCategoryId.ENVIRONMENT);
	}

	private PolicyFactory factory;

	public Xacml20PolicyMapper(PolicyFactory factory) {
		this.factory = factory;
	}
	
	public CompositeDecisionRule create(Object o) throws PolicySyntaxException
	{
		if(o instanceof PolicyType){
			return createPolicy((PolicyType)o);
		}
		if(o instanceof PolicySetType){
			return createPolicySet((PolicySetType)o);
		}
		throw new PolicySyntaxException(
				"Given object can not be mapped to Policy or PolicySet");
	}

	public Policy createPolicy(PolicyType p) throws PolicySyntaxException 
	{
		VariableManager<JAXBElement<?>> m = getVariables(p);
		Version version = Version.parse(p.getVersion());
		Collection<ObligationExpression> obligations = getObligations(p.getObligations());
		PolicyDefaults policyDefaults = createPolicyDefaults(p.getPolicyDefaults());
		Target target = create(p.getTarget());
		Map<String, VariableDefinition> variableDefinitions = m.getVariableDefinitions();
		Collection<Rule> rules = getRules(p, m);
		return factory.createPolicy(p.getPolicyId(), version, p
				.getDescription(), 
				policyDefaults, 
				target, 
				variableDefinitions.values(), 
				p.getRuleCombiningAlgId(), rules, 
				obligations, 
				Collections.<AdviceExpression> emptyList());
	}

	public PolicySet createPolicySet(PolicySetType p) throws PolicySyntaxException 
	{
		Version version = Version.parse(p.getVersion());
		Collection<ObligationExpression> obligations = getObligations(p
				.getObligations());
		PolicySetDefaults policySetDefaults = createPolicySetDefaults(p
				.getPolicySetDefaults());
		Collection<CompositeDecisionRule> policies = getPolicies(p);
		Target target = create(p.getTarget());
		return factory.createPolicySet(p.getPolicySetId(), version, p
				.getDescription(), policySetDefaults, target, p
				.getPolicyCombiningAlgId(), policies, obligations, Collections
				.<AdviceExpression> emptyList());
	}

	private Collection<CompositeDecisionRule> getPolicies(PolicySetType p)
			throws PolicySyntaxException {
		Collection<CompositeDecisionRule> policies = new LinkedList<CompositeDecisionRule>();
		for (JAXBElement<?> o : p.getPolicySetOrPolicyOrPolicySetIdReference()) {
			Object v = o.getValue();
			if (v instanceof PolicySetType) {
				policies.add(create((PolicySetType) v));
				continue;
			}
			if (v instanceof PolicyType) {
				policies.add(create((PolicyType) v));
				continue;
			}
			if (v instanceof IdReferenceType) {
				IdReferenceType ref = (IdReferenceType) v;
				if (o.getName().getLocalPart().equals("PolicySetIdReference")) {
					PolicySetIDReference policySetRef = factory
							.createPolicySetIDReference(ref.getValue(),
									VersionMatch.parse(ref.getVersion()),
									VersionMatch
											.parse(ref.getEarliestVersion()),
									VersionMatch.parse(ref.getLatestVersion()));
					policies.add(policySetRef);
					continue;
				}
				if (o.getName().getLocalPart().equals("PolicyIdReference")) {
					PolicyIDReference policyRef = factory
							.createPolicyIDReference(ref.getValue(),
									VersionMatch.parse(ref.getVersion()),
									VersionMatch
											.parse(ref.getEarliestVersion()),
									VersionMatch.parse(ref.getLatestVersion()));
					policies.add(policyRef);
					continue;
				}
			}
		}
		return policies;
	}

	private PolicyDefaults createPolicyDefaults(DefaultsType defaults)
			throws PolicySyntaxException 
	{
		if(defaults == null){
			return null;
		}
		return factory.createPolicyDefaults(defaults.getXPathVersion());
	}

	private PolicySetDefaults createPolicySetDefaults(DefaultsType defaults)
			throws PolicySyntaxException 
	{
		if(defaults == null){
			return null;
		}
		return factory.createPolicySetDefaults(defaults.getXPathVersion());
	}

	private Target create(TargetType target) throws PolicySyntaxException 
	{
		if(target == null){
			return null;
		}
		Collection<MatchAnyOf> match = new LinkedList<MatchAnyOf>();
		ActionsType actions = target.getActions();
		if (actions != null) {
			match.add(create(actions));
		}
		EnvironmentsType env = target.getEnvironments();
		if (env != null) {
			match.add(create(env));
		}
		ResourcesType resources = target.getResources();
		if (resources != null) {
			match.add(create(resources));
		}
		SubjectsType subjects = target.getSubjects();
		if (subjects != null) {
			match.add(create(subjects));
		}
		return match.isEmpty() ? null : factory.createTarget(match);
	}

	private MatchAnyOf create(ActionsType actions) throws PolicySyntaxException {
		if (actions == null) {
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for (ActionType action : actions.getAction()) {
			Collection<Match> matches = new LinkedList<Match>();
			for (ActionMatchType match : action.getActionMatch()) {
				matches.add(createMatch(match));
			}
			allOf.add(factory.createAllOf(matches));
		}
		return factory.createAnyOf(allOf);
	}

	private MatchAnyOf create(ResourcesType resources)
			throws PolicySyntaxException {
		if (resources == null) {
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for (ResourceType action : resources.getResource()) {
			Collection<Match> matches = new LinkedList<Match>();
			for (ResourceMatchType match : action.getResourceMatch()) {
				matches.add(createMatch(match));
			}
			allOf.add(factory.createAllOf(matches));
		}
		return factory.createAnyOf(allOf);
	}

	private MatchAnyOf create(SubjectsType resources)
			throws PolicySyntaxException {
		if (resources == null) {
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for (SubjectType action : resources.getSubject()) {
			Collection<Match> matches = new LinkedList<Match>();
			for (SubjectMatchType match : action.getSubjectMatch()) {
				matches.add(createMatch(match));
			}
			allOf.add(factory.createAllOf(matches));
		}
		return factory.createAnyOf(allOf);
	}

	private MatchAnyOf create(EnvironmentsType actions)
			throws PolicySyntaxException {
		if (actions == null) {
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for (EnvironmentType action : actions.getEnvironment()) {
			Collection<Match> matches = new LinkedList<Match>();
			for (EnvironmentMatchType match : action.getEnvironmentMatch()) {
				matches.add(createMatch(match));
			}
			allOf.add(factory.createAllOf(matches));
		}
		return factory.createAnyOf(allOf);
	}

	private VariableManager<JAXBElement<?>> getVariables(PolicyType p)
			throws PolicySyntaxException 
	{
		try
		{
			List<Object> objects = p
					.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
			if (objects == null || objects.isEmpty()) {
				return new VariableManager<JAXBElement<?>>(Collections.<String, JAXBElement<?>>emptyMap());
			}
			Map<String, JAXBElement<?>> expressions = new HashMap<String, JAXBElement<?>>();
			for (Object o : objects) {
				if (!(o instanceof VariableDefinitionType)) {
					continue;
				}	
				VariableDefinitionType varDef = (VariableDefinitionType)o;
				if(expressions.containsKey(varDef.getVariableId())){
					throw new PolicySyntaxException("Policy contains a variableId=\"%s\" is alerady " +
							"used for previously defined variable", varDef.getVariableId());
				}
				expressions.put(varDef.getVariableId(), varDef.getExpression());
			}
			VariableManager<JAXBElement<?>> manager = new VariableManager<JAXBElement<?>>(expressions);
			parseVariables(manager);
			return manager;
		}catch(IllegalArgumentException e){
			throw new PolicySyntaxException(e);
		}
	}

	
	
	private void parseVariables(VariableManager<JAXBElement<?>> m) throws PolicySyntaxException
	{
		for(String varId : m.getVariableDefinitionExpressions())
		{
			JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varId);	
			log.debug("Parsing variable definition=\"{}\"", varId);
			m.pushVariableDefinition(varId);
			Expression expression = parseExpression(varDefExp, m);
			m.resolveVariableDefinition(factory.createVariableDefinition(varId, expression));
		}
	}
	
	Expression parseExpression(JAXBElement<?> expression, 
			VariableManager<JAXBElement<?>> m) throws PolicySyntaxException
	{	
		log.debug("Parsing variable definition expression=\"{}\"", expression.getName());
		Object exp = expression.getValue();
		if (exp instanceof org.oasis.xacml.v20.policy.AttributeValueType) {
			return createValue((org.oasis.xacml.v20.policy.AttributeValueType) exp);
		}
		if (exp instanceof ApplyType) {
			return createApply((ApplyType)exp, m);
		}
		if (exp instanceof FunctionType) {
			FunctionType f = (FunctionType) exp;
			return factory.createFunctionReference(f.getFunctionId());
		}
		if (exp instanceof AttributeDesignatorType) {
			AttributeCategoryId categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType) exp);
		}
		if (exp instanceof AttributeSelectorType) {
			AttributeCategoryId categoryId = getSelectoryCategory((AttributeSelectorType) exp);
			return createSelector(categoryId, (AttributeSelectorType) exp);
		}
		if (exp instanceof VariableReferenceType) {
			VariableReferenceType varRef = (VariableReferenceType) exp;
			VariableDefinition varDef = m.getVariableDefinition(varRef.getVariableId());
			if(varDef != null){
				return factory.createVariableReference(varDef);
			}
			JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varRef.getVariableId());
			m.pushVariableDefinition(varRef.getVariableId());
			parseExpression(varDefExp, m);
			varDef = m.getVariableDefinition(varRef.getVariableId());
			Preconditions.checkState(varDef != null);
			m.resolveVariableDefinition(varDef);
			return factory.createVariableReference(varDef);
		}
		throw new PolicySyntaxException("Expression=\"%s\" is not supported",
				expression.getName());
	}
	
	private Collection<Rule> getRules(PolicyType p,
			VariableManager<JAXBElement<?>> variables)
			throws PolicySyntaxException 
	{
		Collection<Object> objects = p
				.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
		Collection<Rule> rules = new LinkedList<Rule>();
		for (Object o : objects) {
			if (o instanceof RuleType) {
				rules.add(create((RuleType) o, variables));
			}
		}
		return rules;
	}
	
	Rule create(RuleType r, VariableManager<JAXBElement<?>> variables)
			throws PolicySyntaxException 
	{
		Effect effect = r.getEffect() == EffectType.DENY ? Effect.DENY
				: Effect.PERMIT;
		return factory.createRule(r.getRuleId(), r.getDescription(), create(r
				.getTarget()), create(r.getCondition(), variables), effect);
	}

	private Condition create(ConditionType c, VariableManager<JAXBElement<?>> variables)
			throws PolicySyntaxException {
		if (c == null) {
			return null;
		}
		JAXBElement<?> expression = c.getExpression();
		if(expression == null){
			return null;
		}
		return factory.createCondition(createExpression(expression, variables));
	}

	Collection<ObligationExpression> getObligations(ObligationsType obligations)
			throws PolicySyntaxException {
		if (obligations == null) {
			return Collections.<ObligationExpression> emptyList();
		}
		Collection<ObligationExpression> o = new LinkedList<ObligationExpression>();
		for (ObligationType obligation : obligations.getObligation()) {
			o.add(factory.createObligationExpression(
									obligation.getObligationId(),
									obligation.getFulfillOn() == EffectType.PERMIT ? Effect.PERMIT
											: Effect.DENY,
									createAttributeAssigments(obligation
											.getAttributeAssignment())));
		}
		return o;
	}

	Collection<AttributeAssignmentExpression> createAttributeAssigments(
			Collection<AttributeAssignmentType> exp)
			throws PolicySyntaxException {
		Collection<AttributeAssignmentExpression> expressions = new LinkedList<AttributeAssignmentExpression>();
		for (AttributeAssignmentType attr : exp) {
			AttributeValue value = createValue(attr.getDataType(), attr
					.getContent());
			expressions.add(factory.createAttributeAssigmentExpression(attr
					.getAttributeId(), value));
		}
		return expressions;
	}

	Expression createExpression(JAXBElement<?> expression, 
			VariableManager<JAXBElement<?>> m)
			throws PolicySyntaxException 
	{
		if(expression == null){
			return null;
		}
		Object exp = expression.getValue();
		if (exp instanceof org.oasis.xacml.v20.policy.AttributeValueType) {
			return createValue((org.oasis.xacml.v20.policy.AttributeValueType) exp);
		}
		if (exp instanceof ApplyType) {
			return createApply((ApplyType)exp, m);
		}
		if (exp instanceof FunctionType) {
			FunctionType f = (FunctionType) exp;
			return factory.createFunctionReference(f.getFunctionId());
		}
		if (exp instanceof AttributeDesignatorType) {
			AttributeCategoryId categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType) exp);
		}
		if (exp instanceof AttributeSelectorType) {
			AttributeCategoryId categoryId = getSelectoryCategory((AttributeSelectorType) exp);
			return createSelector(categoryId, (AttributeSelectorType) exp);
		}
		if (exp instanceof VariableReferenceType) {
			VariableReferenceType varRef = (VariableReferenceType) exp;
			VariableDefinition varDef = m.getVariableDefinition(varRef.getVariableId());
			if(varDef == null){
				throw new PolicySyntaxException("Can not resolve variable=\"%s\"",
						varRef.getVariableId());
			}
			return factory.createVariableReference(varDef);
		}
		throw new PolicySyntaxException("Expression=\"%s\" is not supported",
				expression.getName());
	}

	private Match createMatch(Object exp) throws PolicySyntaxException 
	{
		if (exp instanceof SubjectMatchType) {
			SubjectMatchType match = (SubjectMatchType) exp;
			SubjectAttributeDesignatorType desig = match
					.getSubjectAttributeDesignator();
			if (desig != null) {
				AttributeCategoryId categoryId = AttributeCategoryId
						.parse(desig.getSubjectCategory());
				if (categoryId == null) {
					throw new PolicySyntaxException("Unknown subject "
							+ "attribute designator category=\"%s\"", desig
							.getSubjectCategory());
				}
				return factory.createMatch(match.getMatchId(),
						createValue(match.getAttributeValue()),
						createDesignator(categoryId, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return factory.createMatch(match.getMatchId(),
						createValue(match.getAttributeValue()), createSelector(
								getSelectoryCategory(selector), selector));
			}
			throw new PolicySyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof ActionMatchType) {
			ActionMatchType match = (ActionMatchType) exp;
			AttributeDesignatorType desig = match
					.getActionAttributeDesignator();
			if (desig != null) {
				return factory.createMatch(match.getMatchId(),
						createValue(match.getAttributeValue()),
						createDesignator(AttributeCategoryId.ACTION, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return factory.createMatch(match.getMatchId(),
						createValue(match.getAttributeValue()), createSelector(
								getSelectoryCategory(selector), selector));
			}
			throw new PolicySyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof ResourceMatchType) {
			ResourceMatchType match = (ResourceMatchType) exp;
			AttributeDesignatorType desig = match
					.getResourceAttributeDesignator();
			if (desig != null) {
				return factory.createMatch(match.getMatchId(),
						createValue(match.getAttributeValue()),
						createDesignator(AttributeCategoryId.RESOURCE, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return factory.createMatch(match.getMatchId(),
						createValue(match.getAttributeValue()), createSelector(
								getSelectoryCategory(selector), selector));
			}
			throw new PolicySyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof EnvironmentMatchType) {
			EnvironmentMatchType match = (EnvironmentMatchType) exp;
			AttributeDesignatorType desig = match
					.getEnvironmentAttributeDesignator();
			if (desig != null) {
				return factory
						.createMatch(match.getMatchId(), createValue(match
								.getAttributeValue()), createDesignator(
								AttributeCategoryId.ENVIRONMENT, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return factory.createMatch(match.getMatchId(),
						createValue(match.getAttributeValue()), createSelector(
								getSelectoryCategory(selector), selector));
			}
			throw new PolicySyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		throw new PolicySyntaxException(
				"Can't create Match from a given instance=\"%s\"", exp);
	}

	Apply createApply(ApplyType apply, VariableManager<JAXBElement<?>> m) 
		throws PolicySyntaxException 
	{
		List<Expression> arguments = new LinkedList<Expression>();
		for (JAXBElement<?> arg : apply.getExpression()) 
		{
			Expression exp = parseExpression(arg, m);
			log.debug("Apply argument=\"{}\"", exp);
			arguments.add(exp);
		}
		return factory.createApply(apply.getFunctionId(), arguments);
	}

	/**
	 * Creates {@link AttributeValue} from a given {@link JAXBElement}
	 * 
	 * @param element
	 *            a JAXB element
	 * @return {@link AttributeValue}
	 * @throws PolicySyntaxException
	 */
	AttributeValue createValue(
			org.oasis.xacml.v20.policy.AttributeValueType value)
			throws PolicySyntaxException {
		return createValue(value.getDataType(), value.getContent());
	}

	AttributeValue createValue(String dataType, List<Object> content)
			throws PolicySyntaxException {
		if (content == null || content.isEmpty()) {
			throw new PolicySyntaxException("Attribute does not have content");
		}
		return factory.createAttributeValue(dataType, Iterables
				.getOnlyElement(content));
	}

	AttributeSelector createSelector(AttributeCategoryId categoryId,
			AttributeSelectorType selector) throws PolicySyntaxException 
	{
		AttributeValueType dataType = XacmlDataTypes.getByTypeId(selector
				.getDataType());
		if (dataType == null) {
			throw new PolicySyntaxException("Unknown dataType=\"%s\"", selector
					.getDataType());
		}
		String xpath = transformSelectorXPath(selector);
		log.debug("Processing selector with xpath=\"{}\", transformed xpath=\"{}\"", selector.getRequestContextPath(), xpath);
		return factory.createAttributeSelector(categoryId, xpath, dataType,
				selector.isMustBePresent());
	}

	AttributeCategoryId getSelectoryCategory(AttributeSelectorType selector) {
		return AttributeCategoryId.RESOURCE;
	}

	String transformSelectorXPath(AttributeSelectorType selector) throws PolicySyntaxException
	{
		return Xacml20XPathTo30Transformer.transform20PathTo30(selector.getRequestContextPath());
	}

	/**
	 * Creates {@link AttributeDesignator} from a given {@link JAXBElement}
	 * 
	 * @param element
	 *            a JAXB element
	 * @return {@link AttributeDesignator} instance
	 * @throws PolicySyntaxException
	 */
	AttributeDesignator createDesignator(AttributeCategoryId categoryId,
			AttributeDesignatorType ref) throws PolicySyntaxException {
		AttributeValueType dataType = XacmlDataTypes.getByTypeId(ref.getDataType());
		if (dataType == null) {
			throw new PolicySyntaxException("Unknown dataType=\"%s\"", ref
					.getDataType());
		}
		return factory.createAttributeDesignator(categoryId, ref
				.getAttributeId(), dataType, ref.isMustBePresent(), ref
				.getIssuer());
	}

	AttributeCategoryId getDesignatorCategory(JAXBElement<?> element)
			throws PolicySyntaxException {
		Object ref = element.getValue();
		if (ref instanceof SubjectAttributeDesignatorType) {
			SubjectAttributeDesignatorType subjectRef = (SubjectAttributeDesignatorType) ref;
			AttributeCategoryId categoryId = AttributeCategoryId
					.parse(subjectRef.getSubjectCategory());
			if (categoryId == null) {
				throw new PolicySyntaxException("Unknown subject "
						+ "attribute designator category=\"%s\"", ref);
			}
			return categoryId;
		}
		AttributeCategoryId categoryId = designatorMappings.get(element.getName().getLocalPart());
		if (categoryId == null) {
			throw new PolicySyntaxException(
					"Unknown attribute designator=\"%s\"", element.getName());
		}
		return categoryId;
	}
}
