package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.*;
import java.util.stream.Collectors;

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
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.*;
import org.xacml4j.v30.marshal.PolicyUnmarshallerSupport;
import org.xacml4j.v30.types.*;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeAssignmentExpression;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.AttributeSelector;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.Match;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyDefaults;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetDefaults;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.VariableDefinition;
import org.xacml4j.v30.policy.VariableReference;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;

import com.google.common.collect.ImmutableMap;

public class Xacml20PolicyFromJaxbToObjectModelMapper extends PolicyUnmarshallerSupport
{
	private static final Map<String, CategoryId> designatorMappings = ImmutableMap.of(
			"ResourceAttributeDesignator",	CategoryId.RESOURCE,
			"ActionAttributeDesignator", CategoryId.ACTION,
			"EnvironmentAttributeDesignator", CategoryId.ENVIRONMENT);

	private static final Map<EffectType, Effect> v20ToV30EffectMapping = ImmutableMap.of(
			EffectType.DENY, Effect.DENY,
			EffectType.PERMIT, Effect.PERMIT);


	public Xacml20PolicyFromJaxbToObjectModelMapper(
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionAlgorithms) throws Exception{
		super(functions, decisionAlgorithms);
	}

	public CompositeDecisionRule create(Object o) throws SyntaxException
	{
		if (o instanceof PolicyType) {
			return createPolicy((PolicyType) o);
		} else if (o instanceof PolicySetType) {
			return createPolicySet((PolicySetType) o);
		}
		throw new SyntaxException(
				"Given object can not be mapped to Policy or PolicySet");
	}

	public Policy createPolicy(PolicyType p) throws SyntaxException
	{
		try {
			VariableManager<JAXBElement<?>> m = getVariables(p);
			Map<String, VariableDefinition> variableDefinitions = m.getVariableDefinitions();
			return Policy
					.builder(p.getPolicyId())
					.description(p.getDescription())
					.version(p.getVersion())
					.defaults(createPolicyDefaults(p.getPolicyDefaults()))
					.target(create(p.getTarget()))
					.combiningAlgorithm(createRuleCombiningAlgorithm(p.getRuleCombiningAlgId()))
					.rules(getRules(p, m))
					.vars(variableDefinitions.values())
					.obligation(getObligations(p.getObligations()))
					.build();
		} catch (IllegalArgumentException e) {
			throw new SyntaxException(e);
		}
	}

	public PolicySet createPolicySet(PolicySetType p) throws SyntaxException
	{
		try
		{
			return PolicySet
					.builder(p.getPolicySetId())
					.version(p.getVersion())
					.description(p.getDescription())
					.target(create(p.getTarget()))
					.defaults(createPolicySetDefaults(p.getPolicySetDefaults()))
					.withCombiningAlgorithm(lookUpPolicyCombiningAlgorithm(p.getPolicyCombiningAlgId()))
					.obligation(getObligations(p.getObligations()))
					.compositeDecisionRules(getPolicies(p))
					.build();
		}catch(SyntaxException e){
			throw e;
		}catch(Exception e){
			throw new SyntaxException(e);
		}
	}

	private Collection<CompositeDecisionRule> getPolicies(PolicySetType p)
			throws SyntaxException {
		Collection<CompositeDecisionRule> policies = new LinkedList<CompositeDecisionRule>();
		for (JAXBElement<?> o : p.getPolicySetOrPolicyOrPolicySetIdReference()) {
			Object v = o.getValue();
			if (v instanceof PolicySetType) {
				policies.add(create(v));
				continue;
			}
			if (v instanceof PolicyType) {
				policies.add(create(v));
				continue;
			}
			if (v instanceof IdReferenceType) {
				IdReferenceType ref = (IdReferenceType) v;
				if (o.getName().getLocalPart().equals("PolicySetIdReference")) {
					if(ref.getValue() == null){
						throw new SyntaxException(
								"PolicySet reference id can't be null");
					}
					PolicySetIDReference policySetRef = PolicySetIDReference
						.builder(ref.getValue())
						.versionAsString(ref.getVersion())
						.earliest(ref.getEarliestVersion())
						.latest(ref.getLatestVersion())
						.build();
					policies.add(policySetRef);
					continue;
				}
				if (o.getName().getLocalPart().equals("PolicyIdReference")) {
					if(ref.getValue() == null){
						throw new SyntaxException(
								"Policy reference id can't be null");
					}
					PolicyIDReference policyRef = PolicyIDReference
							.builder(ref.getValue())
							.versionAsString(ref.getVersion())
							.earliest(ref.getEarliestVersion())
							.latest(ref.getLatestVersion())
							.build();
					policies.add(policyRef);
				}
			}
		}
		return policies;
	}

	private PolicyDefaults createPolicyDefaults(DefaultsType defaults)
			throws SyntaxException
	{
		if(defaults == null){
			return null;
		}
		return PolicyDefaults
				.builder()
				.xpathVersion(defaults.getXPathVersion())
				.build();
	}

	private PolicySetDefaults createPolicySetDefaults(DefaultsType defaults)
			throws SyntaxException
	{
		if(defaults == null){
			return null;
		}
		return PolicySetDefaults
				.builder()
				.xpathVersion(defaults.getXPathVersion())
				.build();
	}

	private Target create(TargetType target) throws SyntaxException
	{
		if(target == null){
			return null;
		}
		Target.Builder b = Target.builder();
		ActionsType actions = target.getActions();
		if (actions != null) {
			b.anyOf(create(actions));
		}
		EnvironmentsType env = target.getEnvironments();
		if (env != null) {
			b.anyOf(create(env));
		}
		ResourcesType resources = target.getResources();
		if (resources != null) {
			b.anyOf(create(resources));
		}
		SubjectsType subjects = target.getSubjects();
		if (subjects != null) {
			b.anyOf(create(subjects));
		}
		return b.build();
	}

	private MatchAnyOf create(ActionsType actions) throws SyntaxException {
		if (actions == null) {
			return null;
		}
		MatchAnyOf.Builder anyOfb = MatchAnyOf.builder();
		for (ActionType action : actions.getAction()) {
			MatchAllOf.Builder allOfb = MatchAllOf.builder();
			for (ActionMatchType match : action.getActionMatch()) {
				allOfb.allOf(createMatch(match));
			}
			anyOfb.anyOf(allOfb.build());
		}
		return anyOfb.build();
	}

	private MatchAnyOf create(ResourcesType resources)
			throws SyntaxException {
		if (resources == null) {
			return null;
		}
		MatchAnyOf.Builder anyOfb = MatchAnyOf.builder();
		for (ResourceType action : resources.getResource()) {
			MatchAllOf.Builder allOfb = MatchAllOf.builder();
			for (ResourceMatchType match : action.getResourceMatch()) {
				allOfb.allOf(createMatch(match));
			}
			anyOfb.anyOf(allOfb.build());
		}
		return anyOfb.build();
	}

	private MatchAnyOf create(SubjectsType resources)
			throws SyntaxException {
		if (resources == null) {
			return null;
		}
		MatchAnyOf.Builder anyOfBuilder = MatchAnyOf.builder();
		for (SubjectType action : resources.getSubject()) {
			MatchAllOf.Builder allOfBuilder = MatchAllOf.builder();
			for (SubjectMatchType match : action.getSubjectMatch()) {
				allOfBuilder.allOf(createMatch(match));
			}
			anyOfBuilder.anyOf(allOfBuilder.build());
		}
		return anyOfBuilder.build();
	}

	private MatchAnyOf create(EnvironmentsType actions)
			throws SyntaxException {
		if (actions == null) {
			return null;
		}
		MatchAnyOf.Builder anyOfBuilder = MatchAnyOf.builder();
		for (EnvironmentType action : actions.getEnvironment()) {
			MatchAllOf.Builder allOfBuilder = MatchAllOf.builder();
			for (EnvironmentMatchType match : action.getEnvironmentMatch()) {
				allOfBuilder.allOf(createMatch(match));
			}
			anyOfBuilder.anyOf(allOfBuilder.build());
		}
		return anyOfBuilder.build();
	}

	private VariableManager<JAXBElement<?>> getVariables(PolicyType p)
			throws SyntaxException
	{
		try
		{
			List<Object> objects = p
					.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
			if (objects == null || objects.isEmpty()) {
				return new VariableManager<>(Collections.<String, JAXBElement<?>>emptyMap());
			}
			Map<String, JAXBElement<?>> expressions = new HashMap<String, JAXBElement<?>>();
			for (Object o : objects) {
				if (!(o instanceof VariableDefinitionType)) {
					continue;
				}
				VariableDefinitionType varDef = (VariableDefinitionType)o;
				if(expressions.containsKey(varDef.getVariableId())){
					throw new SyntaxException("Policy contains a variableId=\"%s\" is already " +
							"used for previously defined variable", varDef.getVariableId());
				}
				expressions.put(varDef.getVariableId(), varDef.getExpression());
			}
			VariableManager<JAXBElement<?>> manager = new VariableManager<JAXBElement<?>>(expressions);
			parseVariables(manager);
			return manager;
		}catch(IllegalArgumentException e){
			throw new SyntaxException(e);
		}
	}

	private void parseVariables(VariableManager<JAXBElement<?>> m) throws SyntaxException
	{
		for (String varId : m.getVariableDefinitionExpressions()) {
			if (m.getVariableDefinition(varId) == null) {
				JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varId);
				m.pushVariableDefinition(varId);
				Expression expression = parseExpression(varDefExp, m);
				m.resolveVariableDefinition(new VariableDefinition(varId, expression));
			}
		}
	}

	private Expression parseExpression(JAXBElement<?> expression,
			VariableManager<JAXBElement<?>> m) throws SyntaxException
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
					lookUpFunction(f.getFunctionId()));
		}
		if (exp instanceof AttributeDesignatorType) {
			CategoryId categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType) exp);
		}
		if (exp instanceof AttributeSelectorType) {
			CategoryId categoryId = getSelectorCategory((AttributeSelectorType) exp);
			return createSelector(categoryId, (AttributeSelectorType) exp);
		}
		if (exp instanceof VariableReferenceType) {
			final VariableReferenceType varRef = (VariableReferenceType) exp;
			final VariableDefinition existingVarDef = m.getVariableDefinition(varRef.getVariableId());
			if (existingVarDef != null) {
				return new VariableReference(existingVarDef);
			}
			final JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varRef.getVariableId());
			m.pushVariableDefinition(varRef.getVariableId());
			final Expression parsedExpression = parseExpression(varDefExp, m);
			VariableDefinition varDef = new VariableDefinition(varRef.getVariableId(), parsedExpression);
			m.resolveVariableDefinition(varDef);
			return new VariableReference(varDef);
		}
		throw new SyntaxException("Expression=\"%s\" is not supported",
				expression.getName());
	}

	private Collection<Rule> getRules(PolicyType p,
			VariableManager<JAXBElement<?>> variables)
			throws SyntaxException
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
			throws SyntaxException
	{
		return Rule
				.builder(r.getRuleId(), v20ToV30EffectMapping.get(r.getEffect()))
				.description(r.getDescription())
				.target(create(r.getTarget()))
				.condition(create(r.getCondition(), variables))
				.build();
	}

	private Condition create(ConditionType c,
			VariableManager<JAXBElement<?>> variables)
	throws SyntaxException {
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
			throws SyntaxException {
		if (obligations == null) {
			return Collections.emptyList();
		}
		Collection<ObligationExpression> o = new LinkedList<ObligationExpression>();
		for (ObligationType obligation : obligations.getObligation()) {
			o.add(ObligationExpression
					.builder(
							obligation.getObligationId(),
							obligation.getFulfillOn() == EffectType.PERMIT
									? Effect.PERMIT : Effect.DENY)
					.attribute(
							createAttributeAssignments(
									obligation.getAttributeAssignment()))
					.build());
		}
		return o;
	}

	private Collection<AttributeAssignmentExpression> createAttributeAssignments(
			Collection<AttributeAssignmentType> exp)
			throws SyntaxException {
		Collection<AttributeAssignmentExpression> expressions = new LinkedList<AttributeAssignmentExpression>();
		for (AttributeAssignmentType attr : exp) {
			Expression value = null;

			for (Object o : attr.getContent()) {
				if (o instanceof JAXBElement<?>) {
					JAXBElement<?> e = (JAXBElement<?>) o;
					Object val = e.getValue();
					if (val instanceof AttributeDesignatorType) {
						CategoryId categoryId = getDesignatorCategory(e);
						value = createDesignator(categoryId, (AttributeDesignatorType) val);
						break;
					} else if (val instanceof AttributeSelectorType) {
						CategoryId categoryId = getSelectorCategory((AttributeSelectorType) val);
						value = createSelector(categoryId, (AttributeSelectorType) val);
						break;
					}
				}
			}
			if (value == null) {
				value = createValue(attr);
			}
			expressions.add(AttributeAssignmentExpression
					.builder(attr.getAttributeId())
					.expression(value)
					.build());
		}
		return expressions;
	}

	private Expression createExpression(JAXBElement<?> expression,
			VariableManager<JAXBElement<?>> m)
			throws SyntaxException
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
			return new FunctionReference(lookUpFunction(f.getFunctionId()));
		}
		if (exp instanceof AttributeDesignatorType) {
			CategoryId categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType) exp);
		}
		if (exp instanceof AttributeSelectorType) {
			CategoryId categoryId = getSelectorCategory((AttributeSelectorType) exp);
			return createSelector(categoryId, (AttributeSelectorType) exp);
		}
		if (exp instanceof VariableReferenceType) {
			VariableReferenceType varRef = (VariableReferenceType) exp;
			VariableDefinition varDef = m.getVariableDefinition(varRef.getVariableId());
			if(varDef == null){
				throw new SyntaxException("Can not resolve variable=\"%s\"",
						varRef.getVariableId());
			}
			return new VariableReference(varDef);
		}
		throw new SyntaxException("Expression=\"%s\" is not supported",
				expression.getName());
	}

	private Match createMatch(Object exp) throws SyntaxException
	{
		if (exp instanceof SubjectMatchType) {
			SubjectMatchType match = (SubjectMatchType) exp;
			SubjectAttributeDesignatorType desig = match.getSubjectAttributeDesignator();
			if (desig != null) {
				CategoryId categoryId = CategoryId.parse(desig.getSubjectCategory()).get();
				return Match
						.builder()
						.predicate(lookUpFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createDesignator(categoryId, desig))
						.build();
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return Match
						.builder()
						.predicate(lookUpFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createSelector(getSelectorCategory(selector), selector))
						.build();
			}
			throw new SyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof ActionMatchType) {
			ActionMatchType match = (ActionMatchType) exp;
			AttributeDesignatorType desig = match.getActionAttributeDesignator();
			if (desig != null) {
				return Match.builder()
						.predicate(lookUpFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createDesignator(CategoryId.ACTION, desig))
						.build();
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return Match
						.builder()
						.predicate(lookUpFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createSelector(getSelectorCategory(selector), selector))
						.build();
			}
			throw new SyntaxException(
					"Match with functionId=\"%s\" does not have designator or selector",
					match.getMatchId());
		}
		if (exp instanceof ResourceMatchType) {
			ResourceMatchType match = (ResourceMatchType) exp;
			AttributeDesignatorType desig = match.getResourceAttributeDesignator();
			if (desig != null) {
				return Match
						.builder()
						.predicate(lookUpFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createDesignator(CategoryId.RESOURCE, desig))
						.build();
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return Match
						.builder()
						.predicate(lookUpFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createSelector(getSelectorCategory(selector), selector))
						.build();
			}
			throw new SyntaxException(
					"Match with functionId=\"%s\" does not have designator or selector",
					match.getMatchId());
		}
		if (exp instanceof EnvironmentMatchType) {
			EnvironmentMatchType match = (EnvironmentMatchType) exp;
			AttributeDesignatorType desig = match.getEnvironmentAttributeDesignator();
			if (desig != null) {
				return Match
						.builder()
						.predicate(lookUpFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createDesignator(CategoryId.ENVIRONMENT, desig))
						.build();
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return Match
						.builder()
						.predicate(lookUpFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createSelector(getSelectorCategory(selector), selector))
						.build();
			}
			throw new SyntaxException(
					"Match with functionId=\"%s\" does not have designator or selector",
					match.getMatchId());
		}
		throw new SyntaxException(
				"Can't build Match from a given defaultProvider=\"%s\"", exp);
	}

	private Apply createApply(ApplyType apply, VariableManager<JAXBElement<?>> m)
		throws SyntaxException
	{
		return Apply
				.builder(lookUpFunction(apply.getFunctionId()))
				.params(apply.getExpression()
						.stream()
						.map(v->parseExpression(v, m))
						.collect(Collectors.toList()))
				.build();
	}

	/**
	 * Creates {@link Value} from a given {@link JAXBElement}
	 *
	 * @param value
	 *            a JAXB element
	 * @return {@link Value}
	 * @throws SyntaxException
	 */
	private Value createValue(
			org.oasis.xacml.v20.jaxb.policy.AttributeValueType value)
			throws SyntaxException {
		AttributeValueType v = new AttributeValueType();
		v.setDataType(value.getDataType());
		v.getContent().addAll(value.getContent());
		v.getOtherAttributes().putAll(value.getOtherAttributes());
		Optional<TypeToXacml30> toXacml30 = TypeToXacml30.forType(value.getDataType());
		if(!toXacml30.isPresent()){
			throw new SyntaxException("Unknown XACML type=\"%s\"",
					value.getDataType());
		}
		return toXacml30.get().fromXacml30(v);
	}

	private AttributeSelector createSelector(CategoryId categoryId,
											 AttributeSelectorType selector) throws SyntaxException
	{
		return AttributeSelector
				.builder()
				.key(AttributeSelectorKey
						.builder()
						.path(transformSelectorXPath(selector))
						.category(categoryId)
						.dataType(selector.getDataType())
						.build())
				.mustBePresent(selector.isMustBePresent())
				.build();
	}

	private CategoryId getSelectorCategory(AttributeSelectorType selector) {
		return CategoryId.RESOURCE;
	}

	private PathValue transformSelectorXPath(AttributeSelectorType selector) throws SyntaxException
	{
		return Xacml20XPathTo30Transformer.transform20PathTo30(selector.getRequestContextPath());
	}

	/**
	 * Creates {@link AttributeDesignator}
	 * @param categoryId attribute category identifier
	 * @param ref attribute designator type
	 * @return {@link AttributeDesignator} defaultProvider
	 * @throws SyntaxException
	 */
	private AttributeDesignator createDesignator(CategoryId categoryId,
			AttributeDesignatorType ref) throws SyntaxException
	{
		return AttributeDesignator
				.builder()
				.key(AttributeDesignatorKey.builder().
						category(categoryId)
						.attributeId(ref.getAttributeId())
						.dataType(ref.getDataType())
						.issuer(ref.getIssuer()).build())
				.mustBePresent(ref.isMustBePresent())
				.build();
	}

	/**
	 * Gets {@link XacmlCategories} from a given XACML 2.0
	 * attribute designator defaultProvider
	 *
	 * @param element an designator element
	 * @return {@link XacmlCategories} defaultProvider
	 * @throws SyntaxException if error occurs
	 */
	private CategoryId getDesignatorCategory(JAXBElement<?> element)
			throws SyntaxException
	{
		Object ref = element.getValue();
		if (ref instanceof SubjectAttributeDesignatorType) {
			SubjectAttributeDesignatorType subjectRef = (SubjectAttributeDesignatorType) ref;
			CategoryId categoryId = CategoryId.parse(subjectRef.getSubjectCategory()).get();
			if(categoryId == null) {
				throw new SyntaxException(
						"Unknown subject attribute designator category=\"%s\"", ref);
			}
			return categoryId;
		}
		CategoryId categoryId = designatorMappings.get(element.getName().getLocalPart());
		if (categoryId == null) {
			throw new SyntaxException(
					"Unknown attribute designator=\"%s\"", element.getName());
		}
		return categoryId;
	}
}
