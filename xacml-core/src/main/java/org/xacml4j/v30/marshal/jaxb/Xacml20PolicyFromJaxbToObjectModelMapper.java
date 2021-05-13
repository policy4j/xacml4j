package org.xacml4j.v30.marshal.jaxb;

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
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.util.Xacml20XPathTo30Transformer;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.PolicyUnmarshallerSupport;
import org.xacml4j.v30.pdp.Apply;
import org.xacml4j.v30.pdp.AttributeAssignmentExpression;
import org.xacml4j.v30.pdp.AttributeDesignator;
import org.xacml4j.v30.pdp.AttributeSelector;
import org.xacml4j.v30.pdp.Condition;
import org.xacml4j.v30.pdp.FunctionReference;
import org.xacml4j.v30.pdp.Match;
import org.xacml4j.v30.pdp.MatchAllOf;
import org.xacml4j.v30.pdp.MatchAnyOf;
import org.xacml4j.v30.pdp.ObligationExpression;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicyDefaults;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.pdp.PolicySetDefaults;
import org.xacml4j.v30.pdp.PolicySetIDReference;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.pdp.Target;
import org.xacml4j.v30.pdp.VariableDefinition;
import org.xacml4j.v30.pdp.VariableReference;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.TypeCapability;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class Xacml20PolicyFromJaxbToObjectModelMapper extends PolicyUnmarshallerSupport
{
	private final static Map<String, Categories> designatorMappings = ImmutableMap.of(
			"ResourceAttributeDesignator",	Categories.RESOURCE,
			"ActionAttributeDesignator", Categories.ACTION,
			"EnvironmentAttributeDesignator", Categories.ENVIRONMENT);

	private final static Map<EffectType, Effect> v20ToV30EffectMapping = ImmutableMap.of(
			EffectType.DENY, Effect.DENY,
			EffectType.PERMIT, Effect.PERMIT);

	private final static TypeCapability.Index<TypeToXacml30> INDEX = TypeCapability.Index.<TypeToXacml30>build(TypeToXacml30.Types.values());

	public Xacml20PolicyFromJaxbToObjectModelMapper(
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionAlgorithms) throws Exception{
		super(functions, decisionAlgorithms);
	}

	public CompositeDecisionRule create(Object o)
		throws XacmlSyntaxException
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
		}catch(IllegalArgumentException e){
			throw new XacmlSyntaxException(e);
		}
	}

	public PolicySet createPolicySet(PolicySetType p) throws XacmlSyntaxException
	{
		try
		{
			return PolicySet
					.builder(p.getPolicySetId())
					.version(p.getVersion())
					.description(p.getDescription())
					.target(create(p.getTarget()))
					.defaults(createPolicySetDefaults(p.getPolicySetDefaults()))
					.withCombiningAlgorithm(createPolicyCombiningAlgorithm(p.getPolicyCombiningAlgId()))
					.obligation(getObligations(p.getObligations()))
					.compositeDecisionRules(getPolicies(p))
					.build();
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
						throw new XacmlSyntaxException(
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
						throw new XacmlSyntaxException(
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
			throws XacmlSyntaxException
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
			throws XacmlSyntaxException
	{
		if(defaults == null){
			return null;
		}
		return PolicySetDefaults
				.builder()
				.xpathVersion(defaults.getXPathVersion())
				.build();
	}

	private Target create(TargetType target) throws XacmlSyntaxException
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

	private MatchAnyOf create(ActionsType actions) throws XacmlSyntaxException {
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
			throws XacmlSyntaxException {
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
			throws XacmlSyntaxException {
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
			throws XacmlSyntaxException {
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
					throw new XacmlSyntaxException("Policy contains a variableId=\"%s\" is already " +
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
			CategoryId categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType) exp);
		}
		if (exp instanceof AttributeSelectorType) {
			Categories categoryId = getSelectorCategory((AttributeSelectorType) exp);
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
		return Rule
				.builder(r.getRuleId(), v20ToV30EffectMapping.get(r.getEffect()))
				.description(r.getDescription())
				.target(create(r.getTarget()))
				.condition(create(r.getCondition(), variables))
				.build();
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
			return Collections.emptyList();
		}
		Collection<ObligationExpression> o = new LinkedList<ObligationExpression>();
		for (ObligationType obligation : obligations.getObligation()) {
			o.add(ObligationExpression.builder(obligation.getObligationId(),
									obligation.getFulfillOn() == EffectType.PERMIT ? Effect.PERMIT
											: Effect.DENY)
											.attribute(createAttributeAssignments(obligation
													.getAttributeAssignment()))
											.build());
		}
		return o;
	}

	private Collection<AttributeAssignmentExpression> createAttributeAssignments(
			Collection<AttributeAssignmentType> exp)
			throws XacmlSyntaxException {
		Collection<AttributeAssignmentExpression> expressions = new LinkedList<AttributeAssignmentExpression>();
		for (AttributeAssignmentType attr : exp) {
			AttributeExp value = createValue(attr);
			expressions.add(AttributeAssignmentExpression
					.builder(attr.getAttributeId())
					.expression(value)
					.build());
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
			CategoryId categoryId = getDesignatorCategory(expression);
			return createDesignator(categoryId, (AttributeDesignatorType) exp);
		}
		if (exp instanceof AttributeSelectorType) {
			Categories categoryId = getSelectorCategory((AttributeSelectorType) exp);
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
			SubjectAttributeDesignatorType desig = match.getSubjectAttributeDesignator();
			if (desig != null) {
				CategoryId categoryId = Categories.parse(desig.getSubjectCategory());
				return Match
						.builder()
						.predicate(createFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createDesignator(categoryId, desig))
						.build();
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return Match
						.builder()
						.predicate(createFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createSelector(getSelectorCategory(selector), selector))
						.build();
			}
			throw new XacmlSyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof ActionMatchType) {
			ActionMatchType match = (ActionMatchType) exp;
			AttributeDesignatorType desig = match.getActionAttributeDesignator();
			if (desig != null) {
				return Match.builder()
						.predicate(createFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createDesignator(Categories.ACTION, desig))
						.build();
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return Match
						.builder()
						.predicate(createFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createSelector(getSelectorCategory(selector), selector))
						.build();
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
				return Match
						.builder()
						.predicate(createFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createDesignator(Categories.RESOURCE, desig))
						.build();
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return Match
						.builder()
						.predicate(createFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createSelector(getSelectorCategory(selector), selector))
						.build();
			}
			throw new XacmlSyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		if (exp instanceof EnvironmentMatchType) {
			EnvironmentMatchType match = (EnvironmentMatchType) exp;
			AttributeDesignatorType desig = match.getEnvironmentAttributeDesignator();
			if (desig != null) {
				return Match
						.builder()
						.predicate(createFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createDesignator(Categories.ENVIRONMENT, desig))
						.build();
			}
			AttributeSelectorType selector = match.getAttributeSelector();
			if (selector != null) {
				return Match
						.builder()
						.predicate(createFunction(match.getMatchId()))
						.attribute(createValue(match.getAttributeValue()))
						.attrRef(createSelector(getSelectorCategory(selector), selector))
						.build();
			}
			throw new XacmlSyntaxException("Match with functionId=\"%s\" "
					+ "does not have designator or selector", match
					.getMatchId());
		}
		throw new XacmlSyntaxException(
				"Can't build Match from a given instance=\"%s\"", exp);
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
		return Apply
				.builder(createFunction(apply.getFunctionId()))
				.param(arguments)
				.build();
	}

	/**
	 * Creates {@link AttributeExp} from a given {@link JAXBElement}
	 *
	 * @param value
	 *            a JAXB element
	 * @return {@link AttributeExp}
	 * @throws XacmlSyntaxException
	 */
	private AttributeExp createValue(
			org.oasis.xacml.v20.jaxb.policy.AttributeValueType value)
			throws XacmlSyntaxException {
		AttributeValueType v = new AttributeValueType();
		v.setDataType(value.getDataType());
		v.getContent().addAll(value.getContent());
		v.getOtherAttributes().putAll(value.getOtherAttributes());
		Optional<TypeToXacml30> toXacml30 = INDEX.get(v.getDataType());
		if(!toXacml30.isPresent()){
			throw new XacmlSyntaxException("Unknown XACML type=\"%s\"",
					value.getDataType());
		}
		return toXacml30.get().fromXacml30(v);
	}

	private AttributeSelector createSelector(Categories categoryId,
			AttributeSelectorType selector) throws XacmlSyntaxException
	{
		Optional<AttributeExpType> optional = XacmlTypes.getType(selector.getDataType());
		Preconditions.checkState(optional.isPresent());
		return AttributeSelector
				.builder()
				.category(categoryId)
				.xpath(transformSelectorXPath(selector))
				.dataType(optional.get())
				.mustBePresent(selector.isMustBePresent())
				.build();
	}

	private Categories getSelectorCategory(AttributeSelectorType selector) {
		return Categories.RESOURCE;
	}

	private String transformSelectorXPath(AttributeSelectorType selector) throws XacmlSyntaxException
	{
		return Xacml20XPathTo30Transformer.transform20PathTo30(selector.getRequestContextPath());
	}

	/**
	 * Creates {@link AttributeDesignator}
	 * @param categoryId attribute category identifier
	 * @param ref attribute designator type
	 * @return {@link AttributeDesignator} instance
	 * @throws XacmlSyntaxException
	 */
	private AttributeDesignator createDesignator(CategoryId categoryId,
			AttributeDesignatorType ref) throws XacmlSyntaxException
	{
		Optional<AttributeExpType> optional = XacmlTypes.getType(ref.getDataType());
		Preconditions.checkState(optional.isPresent());
		return AttributeDesignator
				.builder()
				.category(categoryId)
				.attributeId(ref.getAttributeId())
				.dataType(optional.get())
				.mustBePresent(ref.isMustBePresent())
				.issuer(ref.getIssuer())
				.build();
	}

	/**
	 * Gets {@link Categories} from a given XACML 2.0
	 * attribute designator instance
	 *
	 * @param element an designator element
	 * @return {@link Categories} instance
	 * @throws XacmlSyntaxException if error occurs
	 */
	private CategoryId getDesignatorCategory(JAXBElement<?> element)
			throws XacmlSyntaxException
	{
		Object ref = element.getValue();
		if (ref instanceof SubjectAttributeDesignatorType) {
			SubjectAttributeDesignatorType subjectRef = (SubjectAttributeDesignatorType) ref;
			CategoryId categoryId = Categories
					.parse(subjectRef.getSubjectCategory());
			if(categoryId == null) {
				throw new XacmlSyntaxException("Unknown subject "
						+ "attribute designator category=\"%s\"", ref);
			}
			return categoryId;
		}
		Categories categoryId = designatorMappings.get(element.getName().getLocalPart());
		if (categoryId == null) {
			throw new XacmlSyntaxException(
					"Unknown attribute designator=\"%s\"", element.getName());
		}
		return categoryId;
	}
}
