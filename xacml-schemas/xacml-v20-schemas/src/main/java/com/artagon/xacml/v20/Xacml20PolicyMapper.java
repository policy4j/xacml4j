package com.artagon.xacml.v20;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.jaxb.policy.ActionMatchType;
import org.oasis.xacml.v20.jaxb.policy.ActionType;
import org.oasis.xacml.v20.jaxb.policy.ActionsType;
import org.oasis.xacml.v20.jaxb.policy.ApplyType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.AttributeDesignatorType;
import org.oasis.xacml.v20.jaxb.policy.AttributeSelectorType;
import org.oasis.xacml.v20.jaxb.policy.ConditionType;
import org.oasis.xacml.v20.jaxb.policy.DefaultsType;
import org.oasis.xacml.v20.jaxb.policy.EffectType;
import org.oasis.xacml.v20.jaxb.policy.EnvironmentMatchType;
import org.oasis.xacml.v20.jaxb.policy.EnvironmentType;
import org.oasis.xacml.v20.jaxb.policy.EnvironmentsType;
import org.oasis.xacml.v20.jaxb.policy.FunctionType;
import org.oasis.xacml.v20.jaxb.policy.IdReferenceType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.oasis.xacml.v20.jaxb.policy.ObligationsType;
import org.oasis.xacml.v20.jaxb.policy.PolicySetType;
import org.oasis.xacml.v20.jaxb.policy.PolicyType;
import org.oasis.xacml.v20.jaxb.policy.ResourceMatchType;
import org.oasis.xacml.v20.jaxb.policy.ResourceType;
import org.oasis.xacml.v20.jaxb.policy.ResourcesType;
import org.oasis.xacml.v20.jaxb.policy.RuleType;
import org.oasis.xacml.v20.jaxb.policy.SubjectAttributeDesignatorType;
import org.oasis.xacml.v20.jaxb.policy.SubjectMatchType;
import org.oasis.xacml.v20.jaxb.policy.SubjectType;
import org.oasis.xacml.v20.jaxb.policy.SubjectsType;
import org.oasis.xacml.v20.jaxb.policy.TargetType;
import org.oasis.xacml.v20.jaxb.policy.VariableDefinitionType;
import org.oasis.xacml.v20.jaxb.policy.VariableReferenceType;

import com.artagon.xacml.util.VariableManager;
import com.artagon.xacml.util.Xacml20XPathTo30Transformer;
import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.Apply;
import com.artagon.xacml.v3.AttributeAssignmentExpression;
import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Condition;
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
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.VariableDefinition;
import com.artagon.xacml.v3.VariableReference;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.PolicyUnmarshallerSupport;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.types.AttributeValueTypes;
import com.google.common.base.Preconditions;

public class Xacml20PolicyMapper extends PolicyUnmarshallerSupport
{
	private final static Map<String, AttributeCategories> designatorMappings = new HashMap<String, AttributeCategories>();

	private final static Map<EffectType, Effect> v20ToV30EffectnMapping = new HashMap<EffectType, Effect>();
	private final static Map<Effect, EffectType> v30ToV20EffectnMapping = new HashMap<Effect, EffectType>();
	
	static 
	{
		designatorMappings.put("ResourceAttributeDesignator",
				AttributeCategories.RESOURCE);
		designatorMappings.put("ActionAttributeDesignator",
				AttributeCategories.ACTION);
		designatorMappings.put("EnvironmentAttributeDesignator",
				AttributeCategories.ENVIRONMENT);
		
		v20ToV30EffectnMapping.put(EffectType.DENY, Effect.DENY);
		v20ToV30EffectnMapping.put(EffectType.PERMIT, Effect.PERMIT);
		
		v30ToV20EffectnMapping.put(Effect.DENY, EffectType.DENY);
		v30ToV20EffectnMapping.put(Effect.PERMIT, EffectType.PERMIT);
	}
	
	public Xacml20PolicyMapper(FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms) throws Exception{
		super(functions, decisionAlgorithms);
	}
	
	public Xacml20PolicyMapper() throws Exception{
		super();
	}
	
	public CompositeDecisionRule create(Object o) throws XacmlSyntaxException
	{
		if(o instanceof PolicyType){
			return createPolicy((PolicyType)o);
		}
		if(o instanceof PolicySetType){
			return createPolicySet((PolicySetType)o);
		}
		throw new XacmlSyntaxException(
				"Given object can not be mapped to Policy or PolicySet");
	}

	public Policy createPolicy(PolicyType p) throws XacmlSyntaxException 
	{
		try{
			VariableManager<JAXBElement<?>> m = getVariables(p);
			Collection<ObligationExpression> obligations = getObligations(p.getObligations());
			PolicyDefaults policyDefaults = createPolicyDefaults(p.getPolicyDefaults());
			Target target = create(p.getTarget());
			Map<String, VariableDefinition> variableDefinitions = m.getVariableDefinitions();
			Collection<Rule> rules = getRules(p, m);
			return new Policy(
					p.getPolicyId(), 
					Version.parse(p.getVersion()), 
					p.getDescription(), 
					policyDefaults, 
					target, 
					variableDefinitions.values(),
					createRuleCombingingAlgorithm(p.getRuleCombiningAlgId()),
					rules, 
					Collections.<AdviceExpression> emptyList(),
					obligations);
		}catch(XacmlSyntaxException e){
			throw e;
		}catch(IllegalArgumentException e){
			throw new XacmlSyntaxException(e);
		}
	}

	public PolicySet createPolicySet(PolicySetType p) throws XacmlSyntaxException 
	{
		try
		{
			Collection<ObligationExpression> obligations = getObligations(p
					.getObligations());
			PolicySetDefaults policySetDefaults = createPolicySetDefaults(p
					.getPolicySetDefaults());
			Collection<CompositeDecisionRule> policies = getPolicies(p);
			Target target = create(p.getTarget());
			return new PolicySet(
					p.getPolicySetId(), 
					Version.parse(p.getVersion()), 
					p.getDescription(), 
					policySetDefaults, 
					target,
					null, 
					null,
					null,
					createPolicyCombingingAlgorithm(p.getPolicyCombiningAlgId()), 
					policies, 
					Collections.<AdviceExpression> emptyList(),
					obligations);
		}catch(XacmlSyntaxException e){
			throw e;
		}catch(Exception e){
			throw new XacmlSyntaxException(e);
		}
	}

	private Collection<CompositeDecisionRule> getPolicies(PolicySetType p)
			throws XacmlSyntaxException {
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
					if(ref.getValue() == null){
						throw new XacmlSyntaxException(
								"PolicySet reference id can't be null");
					}
					PolicySetIDReference policySetRef = 
						PolicySetIDReference.create(ref.getValue(),
								ref.getVersion(), ref.getEarliestVersion(), ref.getLatestVersion());
					policies.add(policySetRef);
					continue;
				}
				if (o.getName().getLocalPart().equals("PolicyIdReference")) {
					if(ref.getValue() == null){
						throw new XacmlSyntaxException(
								"Policy reference id can't be null");
					}
					PolicyIDReference policyRef = PolicyIDReference.create(ref.getValue(),
							ref.getVersion(), ref.getEarliestVersion(), ref.getLatestVersion());
					policies.add(policyRef);
					continue;
				}
			}
		}
		return policies;
	}

	private PolicyDefaults createPolicyDefaults(DefaultsType defaults)
			throws XacmlSyntaxException 
	{
		if(defaults == null){
			return null;
		}
		return PolicyDefaults.create(defaults.getXPathVersion());
	}

	private PolicySetDefaults createPolicySetDefaults(DefaultsType defaults)
			throws XacmlSyntaxException 
	{
		if(defaults == null){
			return null;
		}
		return PolicySetDefaults.create(defaults.getXPathVersion());
	}

	private Target create(TargetType target) throws XacmlSyntaxException 
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
		return !match.isEmpty() ?new Target(match):null;
	}

	private MatchAnyOf create(ActionsType actions) throws XacmlSyntaxException {
		if (actions == null) {
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for (ActionType action : actions.getAction()) {
			Collection<Match> matches = new LinkedList<Match>();
			for (ActionMatchType match : action.getActionMatch()) {
				matches.add(createMatch(match));
			}
			allOf.add(new MatchAllOf(matches));
		}
		return new MatchAnyOf(allOf);
	}

	private MatchAnyOf create(ResourcesType resources)
			throws XacmlSyntaxException {
		if (resources == null) {
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for (ResourceType action : resources.getResource()) {
			Collection<Match> matches = new LinkedList<Match>();
			for (ResourceMatchType match : action.getResourceMatch()) {
				matches.add(createMatch(match));
			}
			allOf.add(new MatchAllOf(matches));
		}
		return new MatchAnyOf(allOf);
	}

	private MatchAnyOf create(SubjectsType resources)
			throws XacmlSyntaxException {
		if (resources == null) {
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for (SubjectType action : resources.getSubject()) {
			Collection<Match> matches = new LinkedList<Match>();
			for (SubjectMatchType match : action.getSubjectMatch()) {
				matches.add(createMatch(match));
			}
			allOf.add(new MatchAllOf(matches));
		}
		return new MatchAnyOf(allOf);
	}

	private MatchAnyOf create(EnvironmentsType actions)
			throws XacmlSyntaxException {
		if (actions == null) {
			return null;
		}
		Collection<MatchAllOf> allOf = new LinkedList<MatchAllOf>();
		for (EnvironmentType action : actions.getEnvironment()) {
			Collection<Match> matches = new LinkedList<Match>();
			for (EnvironmentMatchType match : action.getEnvironmentMatch()) {
				matches.add(createMatch(match));
			}
			allOf.add(new MatchAllOf(matches));
		}
		return new MatchAnyOf(allOf);
	}

	private VariableManager<JAXBElement<?>> getVariables(PolicyType p)
			throws XacmlSyntaxException 
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
					throw new XacmlSyntaxException("Policy contains a variableId=\"%s\" is alerady " +
							"used for previously defined variable", varDef.getVariableId());
				}
				expressions.put(varDef.getVariableId(), varDef.getExpression());
			}
			VariableManager<JAXBElement<?>> manager = new VariableManager<JAXBElement<?>>(expressions);
			parseVariables(manager);
			return manager;
		}catch(IllegalArgumentException e){
			throw new XacmlSyntaxException(e);
		}
	}

	
	
	private void parseVariables(VariableManager<JAXBElement<?>> m) throws XacmlSyntaxException
	{
		for(String varId : m.getVariableDefinitionExpressions()){
			JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varId);	
			m.pushVariableDefinition(varId);
			Expression expression = parseExpression(varDefExp, m);
			m.resolveVariableDefinition(new VariableDefinition(varId, expression));
		}
	}
	
	private Expression parseExpression(JAXBElement<?> expression, 
			VariableManager<JAXBElement<?>> m) throws XacmlSyntaxException
	{	
		Object exp = expression.getValue();
		if (exp instanceof org.oasis.xacml.v20.jaxb.policy.AttributeValueType) {
			return createValue((org.oasis.xacml.v20.jaxb.policy.AttributeValueType) exp);
		}
		if (exp instanceof ApplyType) {
			return createApply((ApplyType)exp, m);
		}
		if (exp instanceof FunctionType) {
			FunctionType f = (FunctionType) exp;
			return new FunctionReference(
					createFunction(f.getFunctionId()));
		}
		if (exp instanceof AttributeDesignatorType) {
			AttributeCategory categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType) exp);
		}
		if (exp instanceof AttributeSelectorType) {
			AttributeCategories categoryId = getSelectoryCategory((AttributeSelectorType) exp);
			return createSelector(categoryId, (AttributeSelectorType) exp);
		}
		if (exp instanceof VariableReferenceType) {
			VariableReferenceType varRef = (VariableReferenceType) exp;
			VariableDefinition varDef = m.getVariableDefinition(varRef.getVariableId());
			if(varDef != null){
				return new VariableReference(varDef);
			}
			JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varRef.getVariableId());
			m.pushVariableDefinition(varRef.getVariableId());
			parseExpression(varDefExp, m);
			varDef = m.getVariableDefinition(varRef.getVariableId());
			Preconditions.checkState(varDef != null);
			m.resolveVariableDefinition(varDef);
			return new VariableReference(varDef);
		}
		throw new XacmlSyntaxException("Expression=\"%s\" is not supported",
				expression.getName());
	}
	
	private Collection<Rule> getRules(PolicyType p,
			VariableManager<JAXBElement<?>> variables)
			throws XacmlSyntaxException 
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
	
	private Rule create(RuleType r, VariableManager<JAXBElement<?>> variables)
			throws XacmlSyntaxException 
	{
		return new Rule(
				r.getRuleId(), 
				r.getDescription(), 
				create(r.getTarget()), 
				create(r.getCondition(), variables), 
				v20ToV30EffectnMapping.get(r.getEffect()));
	}

	private Condition create(ConditionType c, 
			VariableManager<JAXBElement<?>> variables) 
	throws XacmlSyntaxException {
		if (c == null) {
			return null;
		}
		JAXBElement<?> expression = c.getExpression();
		if(expression == null){
			return null;
		}
		return new Condition(createExpression(expression, variables));
	}

	private Collection<ObligationExpression> getObligations(ObligationsType obligations)
			throws XacmlSyntaxException {
		if (obligations == null) {
			return Collections.<ObligationExpression> emptyList();
		}
		Collection<ObligationExpression> o = new LinkedList<ObligationExpression>();
		for (ObligationType obligation : obligations.getObligation()) {
			o.add(new ObligationExpression(obligation.getObligationId(),
									obligation.getFulfillOn() == EffectType.PERMIT ? Effect.PERMIT
											: Effect.DENY,
									createAttributeAssigments(obligation
											.getAttributeAssignment())));
		}
		return o;
	}

	private Collection<AttributeAssignmentExpression> createAttributeAssigments(
			Collection<AttributeAssignmentType> exp)
			throws XacmlSyntaxException {
		Collection<AttributeAssignmentExpression> expressions = new LinkedList<AttributeAssignmentExpression>();
		for (AttributeAssignmentType attr : exp) {
			AttributeValue value = createValue(attr.getDataType(), attr
					.getContent());
			expressions.add(new AttributeAssignmentExpression(attr.getAttributeId(), value));
		}
		return expressions;
	}

	private Expression createExpression(JAXBElement<?> expression, 
			VariableManager<JAXBElement<?>> m)
			throws XacmlSyntaxException 
	{
		if(expression == null){
			return null;
		}
		Object exp = expression.getValue();
		if (exp instanceof org.oasis.xacml.v20.jaxb.policy.AttributeValueType) {
			return createValue((org.oasis.xacml.v20.jaxb.policy.AttributeValueType) exp);
		}
		if (exp instanceof ApplyType) {
			return createApply((ApplyType)exp, m);
		}
		if (exp instanceof FunctionType) {
			FunctionType f = (FunctionType) exp;
			return new FunctionReference(createFunction(f.getFunctionId()));
		}
		if (exp instanceof AttributeDesignatorType) {
			AttributeCategory categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType) exp);
		}
		if (exp instanceof AttributeSelectorType) {
			AttributeCategories categoryId = getSelectoryCategory((AttributeSelectorType) exp);
			return createSelector(categoryId, (AttributeSelectorType) exp);
		}
		if (exp instanceof VariableReferenceType) {
			VariableReferenceType varRef = (VariableReferenceType) exp;
			VariableDefinition varDef = m.getVariableDefinition(varRef.getVariableId());
			if(varDef == null){
				throw new XacmlSyntaxException("Can not resolve variable=\"%s\"",
						varRef.getVariableId());
			}
			return new VariableReference(varDef);
		}
		throw new XacmlSyntaxException("Expression=\"%s\" is not supported",
				expression.getName());
	}

	private Match createMatch(Object exp) throws XacmlSyntaxException 
	{
		if (exp instanceof SubjectMatchType) {
			SubjectMatchType match = (SubjectMatchType) exp;
			SubjectAttributeDesignatorType desig = match
					.getSubjectAttributeDesignator();
			if (desig != null) {
				AttributeCategory categoryId = AttributeCategories.parse(desig.getSubjectCategory());
				return new Match(createFunction(match.getMatchId()),
						createValue(match.getAttributeValue()),
						createDesignator(categoryId, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return new Match(createFunction(match.getMatchId()),
						createValue(match.getAttributeValue()), 
						createSelector(getSelectoryCategory(selector), selector));
			}
			throw new XacmlSyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof ActionMatchType) {
			ActionMatchType match = (ActionMatchType) exp;
			AttributeDesignatorType desig = match.getActionAttributeDesignator();
			if (desig != null) {
				return new Match(createFunction(match.getMatchId()),
						createValue(match.getAttributeValue()),
						createDesignator(AttributeCategories.ACTION, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return new Match(createFunction(match.getMatchId()),
						createValue(match.getAttributeValue()), createSelector(
								getSelectoryCategory(selector), selector));
			}
			throw new XacmlSyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof ResourceMatchType) {
			ResourceMatchType match = (ResourceMatchType) exp;
			AttributeDesignatorType desig = match
					.getResourceAttributeDesignator();
			if (desig != null) {
				return new Match(createFunction(match.getMatchId()),
						createValue(match.getAttributeValue()),
						createDesignator(AttributeCategories.RESOURCE, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return new Match(createFunction(match.getMatchId()),
						createValue(match.getAttributeValue()), createSelector(
								getSelectoryCategory(selector), selector));
			}
			throw new XacmlSyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof EnvironmentMatchType) {
			EnvironmentMatchType match = (EnvironmentMatchType) exp;
			AttributeDesignatorType desig = match.getEnvironmentAttributeDesignator();
			if (desig != null) {
				return new Match(createFunction(match.getMatchId()), 
						createValue(match.getAttributeValue()), 
						createDesignator(AttributeCategories.ENVIRONMENT, desig));
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return new Match(
						createFunction(match.getMatchId()),
						createValue(match.getAttributeValue()),
						createSelector(getSelectoryCategory(selector), 
								selector));
			}
			throw new XacmlSyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		throw new XacmlSyntaxException(
				"Can't create Match from a given instance=\"%s\"", exp);
	}

	private Apply createApply(ApplyType apply, VariableManager<JAXBElement<?>> m) 
		throws XacmlSyntaxException 
	{
		List<Expression> arguments = new LinkedList<Expression>();
		for (JAXBElement<?> arg : apply.getExpression()) 
		{
			Expression exp = parseExpression(arg, m);
			arguments.add(exp);
		}
		return new Apply(
				createFunction(apply.getFunctionId()), arguments);
	}

	/**
	 * Creates {@link AttributeValue} from a given {@link JAXBElement}
	 * 
	 * @param element
	 *            a JAXB element
	 * @return {@link AttributeValue}
	 * @throws XacmlSyntaxException
	 */
	private AttributeValue createValue(
			org.oasis.xacml.v20.jaxb.policy.AttributeValueType value)
			throws XacmlSyntaxException {
		return createValue(value.getDataType(), value.getContent());
	}

	private AttributeValue createValue(String dataType, List<Object> content)
			throws XacmlSyntaxException {
		if (content == null || content.isEmpty()) {
			throw new XacmlSyntaxException("Attribute does not have content");
		}
		
		return AttributeValueTypes.createAttributeValue(dataType, content.iterator().next());
	}
	
	private AttributeSelector createSelector(AttributeCategories categoryId,
			AttributeSelectorType selector) throws XacmlSyntaxException 
	{
		String xpath = transformSelectorXPath(selector);	
		return AttributeSelector.create(categoryId, xpath, null,
				selector.getDataType(),
				selector.isMustBePresent());
	}
	
	private AttributeCategories getSelectoryCategory(AttributeSelectorType selector) {
		return AttributeCategories.RESOURCE;
	}

	private String transformSelectorXPath(AttributeSelectorType selector) throws XacmlSyntaxException
	{
		return Xacml20XPathTo30Transformer.transform20PathTo30(selector.getRequestContextPath());
	}

	/**
	 * Creates {@link AttributeDesignator} from a given {@link JAXBElement}
	 * 
	 * @param element
	 *            a JAXB element
	 * @return {@link AttributeDesignator} instance
	 * @throws XacmlSyntaxException
	 */
	private AttributeDesignator createDesignator(AttributeCategory categoryId,
			AttributeDesignatorType ref) throws XacmlSyntaxException {
	
		return AttributeDesignator.create(categoryId, 
				ref.getAttributeId(), 
				ref.getIssuer(), 
				ref.getDataType(), 
				ref.isMustBePresent());
	}
	
	/**
	 * Gets {@link AttributeCategories} from a given XACML 2.0
	 * attribute designator instance
	 * 
	 * @param element an designator element
	 * @return {@link AttributeCategories} instance
	 * @throws XacmlSyntaxException if error occurs
	 */
	private AttributeCategory getDesignatorCategory(JAXBElement<?> element)
			throws XacmlSyntaxException 
	{
		Object ref = element.getValue();
		if (ref instanceof SubjectAttributeDesignatorType) {
			SubjectAttributeDesignatorType subjectRef = (SubjectAttributeDesignatorType) ref;
			AttributeCategory categoryId = AttributeCategories
					.parse(subjectRef.getSubjectCategory());
			if(categoryId == null) {
				throw new XacmlSyntaxException("Unknown subject "
						+ "attribute designator category=\"%s\"", ref);
			}
			return categoryId;
		}
		AttributeCategories categoryId = designatorMappings.get(element.getName().getLocalPart());
		if (categoryId == null) {
			throw new XacmlSyntaxException(
					"Unknown attribute designator=\"%s\"", element.getName());
		}
		return categoryId;
	}
}
