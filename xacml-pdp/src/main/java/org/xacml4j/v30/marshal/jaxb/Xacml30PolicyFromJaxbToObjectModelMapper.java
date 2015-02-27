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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.oasis.xacml.v30.jaxb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xacml4j.v30.*;
import org.xacml4j.v30.marshal.PolicyUnmarshallerSupport;
import org.xacml4j.v30.pdp.*;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.XacmlTypes;

import javax.xml.bind.JAXBElement;
import java.util.*;

class Xacml30PolicyFromJaxbToObjectModelMapper
	extends PolicyUnmarshallerSupport
{
	private final static Logger log = LoggerFactory.getLogger(Xacml30PolicyFromJaxbToObjectModelMapper.class);


	private final static Map<EffectType, Effect> JAXB_TO_NATIVE_EFFECT_MAPPINGS = ImmutableMap.of(
			EffectType.DENY, Effect.DENY,
			EffectType.PERMIT, Effect.PERMIT);


	public Xacml30PolicyFromJaxbToObjectModelMapper(
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionAlgorithms) {
		super(functions, decisionAlgorithms);
	}

	/**
	 * Creates {@link Policy} instance from a given JAXB
	 * {@link PolicyType} object
	 *
	 * @param p a JAXB policy representation
	 * @return {@link Policy} instance
	 * @throws XacmlSyntaxException if syntax error occurs
	 * while creating XACML policy
	 */
	public Policy createPolicy(PolicyType p) throws XacmlSyntaxException
	{
		VariableManager<JAXBElement<?>> m = getVariables(p);
		Map<String, VariableDefinition> variableDefinitions = m.getVariableDefinitions();
		Condition condition  = create(p.getCondition(), m);
		return Policy
					.builder(p.getPolicyId())
					.description(p.getDescription())
					.version(p.getVersion())
					.condition(condition)
					.target(create(p.getTarget()))
					.issuer(createPolicyIssuer(p.getPolicyIssuer()))
					.combiningAlgorithm(createRuleCombiningAlgorithm(p.getRuleCombiningAlgId()))
					.rules(createRules(p, m))
					.vars(variableDefinitions.values())
					.obligation(getExpressions(p.getObligationExpressions(), m))
					.advice(getExpressions(p.getAdviceExpressions(), m))
					.defaults(createPolicyDefaults(p.getPolicyDefaults()))
					.build();
	}


	/**
	 * Creates {@link Rule} instance from given
	 * {@link PolicyType} JAXB object
	 *
	 * @param p a JAXB policy
	 * @param m a variable manager
	 * @return collection of {@link Rule} instances
	 * @throws XacmlSyntaxException
	 */
	private Collection<Rule> createRules(PolicyType p,
			VariableManager<JAXBElement<?>> m)
		throws XacmlSyntaxException
	{
		Collection<Rule> rules = new LinkedList<Rule>();
		for(Object o : p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition())
		{
			if(o instanceof RuleType){
				RuleType r = (RuleType)o;
				if(log.isDebugEnabled()){
					log.debug("Mapping Rule attributeId=\"{}\"", r.getRuleId());
				}
				rules.add(create(r, m));
			}
		}
		return rules;
	}

	/**
	 * Tries to build either {@link Policy} or {@link PolicySet}
	 * based on runtime type of the given JAXB object
	 *
	 * @param jaxbObject a JAXB object representing either
	 * {@link PolicySetType} or {@link PolicyType}
	 * @return {@link CompositeDecisionRule}
	 * @throws XacmlSyntaxException if given policy or policy set
	 * can not be created due syntactical errors
	 */
	public CompositeDecisionRule create(Object jaxbObject) throws XacmlSyntaxException
	{
		if(jaxbObject instanceof PolicyType){
			return createPolicy((PolicyType)jaxbObject);
		}
		if(jaxbObject instanceof PolicySetType){
			return createPolicySet((PolicySetType)jaxbObject);
		}
		throw new XacmlSyntaxException(
				"Given object can not be mapped to Policy or PolicySet");
	}

	/**
	 * Creates {@link PolicySet} instance from a given JAXB
	 * {@link PolicySetType} object
	 *
	 * @param p a JAXB policy set representation
	 * @return {@link PolicySet} instance
	 * @throws XacmlSyntaxException if syntax error occurs
	 * while creating XACML policy set
	 */
	public PolicySet createPolicySet(PolicySetType p) throws XacmlSyntaxException
	{
		VariableManager<JAXBElement<?>> m = new VariableManager<JAXBElement<?>>(ImmutableMap.<String, JAXBElement<?>>of());
		return PolicySet
				.builder(p.getPolicySetId())
				.version(p.getVersion())
				.condition(create(p.getCondition(), m))
				.description(p.getDescription())
				.issuer(createPolicyIssuer(p.getPolicyIssuer()))
				.target(create(p.getTarget()))
				.defaults(createPolicySetDefaults(p.getPolicySetDefaults()))
				.combiningAlgorithm(createPolicyCombiningAlgorithm(p.getPolicyCombiningAlgId()))
				.compositeDecisionRules(createPolicies(p))
				.obligation(getExpressions(p.getObligationExpressions(), m))
				.advice(getExpressions(p.getAdviceExpressions(), m))
				.build();
	}

	private Collection<DecisionRule> createPolicies(PolicySetType policySet)
		throws XacmlSyntaxException
	{
		Collection<DecisionRule> all = new LinkedList<DecisionRule>();
		for(JAXBElement<?> e : policySet.getPolicySetOrPolicyOrPolicySetIdReference())
		{
			if(e.getValue() instanceof PolicyType){
				all.add(createPolicy((PolicyType)e.getValue()));
				continue;
			}
			if(e.getValue() instanceof PolicySetType){
				all.add(createPolicySet((PolicySetType)e.getValue()));
				continue;
			}
			if(e.getValue() instanceof IdReferenceType)
			{
					IdReferenceType ref = (IdReferenceType)e.getValue();
					if (e.getName().getLocalPart().equals("PolicySetIdReference")) {
						if(ref.getValue() == null){
							throw new XacmlSyntaxException(
									"PolicySet references attributeId can't be null");
						}
						PolicySetReference policySetRef = PolicySetReference
								.builder(ref.getValue())
								.version(ref.getVersion())
								.earliest(ref.getEarliestVersion())
								.latest(ref.getLatestVersion())
								.build();
						all.add(policySetRef);
						continue;
					}
					if(e.getName().getLocalPart().equals("PolicyIdReference")) {
						if(ref.getValue() == null){
							throw new XacmlSyntaxException(
									"Policy references attributeId can't be null");
						}
						PolicyReference policySetRef = PolicyReference
								.builder(ref.getValue())
								.version(ref.getVersion())
								.earliest(ref.getEarliestVersion())
								.latest(ref.getLatestVersion())
								.build();
						all.add(policySetRef);
					}
				}
		}
		return all;
	}

	private Rule create(RuleType r, VariableManager<JAXBElement<?>> variables)
		throws XacmlSyntaxException
	{
		Collection<ObligationExpression> obligations = new LinkedList<ObligationExpression>();
		Collection<AdviceExpression> advice = new LinkedList<AdviceExpression>();
		ObligationExpressionsType obligExps = r.getObligationExpressions();
		if(obligExps != null){
			for(ObligationExpressionType exp : obligExps.getObligationExpression()){
				obligations.add(create(exp, variables));
			}
		}
		AdviceExpressionsType adviceExps = r.getAdviceExpressions();
		if(adviceExps != null){
			for(AdviceExpressionType exp : adviceExps.getAdviceExpression()){
				advice.add(create(exp, variables));
			}
		}
		return Rule
				.builder(r.getRuleId(), (r.getEffect() == EffectType.DENY ? Effect.DENY: Effect.PERMIT))
				.description(r.getDescription())
				.target(create(r.getTarget()))
				.obligation(obligations)
				.advice(advice)
				.condition(create(r.getCondition(), variables))
				.build();
	}

	private Condition create(ConditionType c, VariableManager<JAXBElement<?>> variables)
		throws XacmlSyntaxException
	{
		if (c == null) {
			return null;
		}
		JAXBElement<?> expression = c.getExpression();
		if(expression == null){
			return null;
		}
		return new Condition(parseExpression(expression, variables));
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

	private Entity createPolicyIssuer(PolicyIssuerType issuer)
		throws XacmlSyntaxException
	{
		if(issuer == null){
			return null;
		}
		Entity.Builder b = Entity.builder();
		b.content(createDOMNode(issuer.getContent()));
		for(AttributeType a : issuer.getAttribute()){
			b.attribute(create(a));
		}
		return b.build();
	}

	private Node createDOMNode(ContentType content) throws XacmlSyntaxException
	{
		if(content == null){
			return null;
		}
		List<Object> o = content.getContent();
		if(o.isEmpty()){
			return null;
		}
		return (Node)o.iterator().next();
	}

	private Attribute create(AttributeType a) throws XacmlSyntaxException
	{
		Attribute.Builder b =  Attribute
				.builder(a.getAttributeId())
				.issuer(a.getIssuer())
				.includeInResult(a.isIncludeInResult());
		for(AttributeValueType v : a.getAttributeValue()){
			b.value(create(v));
		}
		return b.build();
	}

	private AttributeExp create(
			AttributeValueType value)
		throws XacmlSyntaxException
	{
	 	Optional<TypeToXacml30> toXacml30 = TypeToXacml30.Types.getIndex().get(value.getDataType());
		Preconditions.checkState(toXacml30.isPresent());
		return toXacml30.get().fromXacml30(value);
	}

	private Collection<AdviceExpression> getExpressions(
			AdviceExpressionsType expressions, VariableManager<JAXBElement<?>> m) throws XacmlSyntaxException
	{
		if (expressions == null) {
			return Collections.emptyList();
		}
		Collection<AdviceExpression> advice = new LinkedList<AdviceExpression>();
		for (AdviceExpressionType e : expressions.getAdviceExpression()) {
			advice.add(create(e, m));
		}
		return advice;
	}

	/**
	 * Parses {@link ObligationExpressionsType} to the collection
	 * of {@link ObligationExpression}
	 *
	 * @param expressions an JAXB object
	 * @param m a variable definition manager
	 * @return a collection of {@link ObligationExpression} instances
	 * @throws XacmlSyntaxException
	 */
	private Collection<ObligationExpression> getExpressions(
			ObligationExpressionsType expressions, VariableManager<JAXBElement<?>> m) throws XacmlSyntaxException
	{
		if (expressions == null) {
			return Collections.emptyList();
		}
		Collection<ObligationExpression> obligations = new LinkedList<ObligationExpression>();
		for (ObligationExpressionType e : expressions.getObligationExpression()) {
			obligations.add(create(e, m));
		}
		return obligations;
	}

	/**
	 * Maps {@link AdviceExpressionsType} to {@link AdviceExpression}
	 *
	 * @param exp
	 *            a JAXB object representing {@link AdviceExpression}
	 * @return {@link AdviceExpression} instance
	 * @throws XacmlSyntaxException
	 *             if {@link AdviceExpression} can not be created from a given
	 *             JAXB object due syntactical error
	 */
	private AdviceExpression create(AdviceExpressionType exp, VariableManager<JAXBElement<?>> m)
			throws XacmlSyntaxException {
		Preconditions.checkArgument(exp != null);
		Collection<AttributeAssignmentExpression> attrExp = new LinkedList<AttributeAssignmentExpression>();
		for (AttributeAssignmentExpressionType e : exp.getAttributeAssignmentExpression()) {
			attrExp.add(create(e, m));
		}
		return AdviceExpression
				.builder(exp.getAdviceId(), JAXB_TO_NATIVE_EFFECT_MAPPINGS.get(exp.getAppliesTo()))
				.attribute(attrExp).build();
	}

	/**
	 * Maps {@link ObligationExpressionsType} to {@link ObligationExpression}
	 *
	 * @param exp
	 *            a JAXB object representing {@link ObligationExpression}
	 * @return {@link ObligationExpression} instance
	 * @throws XacmlSyntaxException
	 *             if {@link ObligationExpression} can not be created from a
	 *             given JAXB object due syntactical error
	 */
	private ObligationExpression create(ObligationExpressionType exp,
			VariableManager<JAXBElement<?>> m)
			throws XacmlSyntaxException {
		Preconditions.checkArgument(exp != null);
		Collection<AttributeAssignmentExpression> attrExp = new LinkedList<AttributeAssignmentExpression>();
		for (AttributeAssignmentExpressionType e : exp.getAttributeAssignmentExpression()) {
			attrExp.add(create(e, m));
		}
		return ObligationExpression.builder(exp.getObligationId(),
				JAXB_TO_NATIVE_EFFECT_MAPPINGS.get(exp.getFulfillOn()))
				.attribute(attrExp).build();
	}

	/**
	 * Maps {@link TargetType} to {@link Target}
	 *
	 * @param target
	 *            a JAXB target instance
	 * @return {@link Target} instance
	 * @throws XacmlSyntaxException
	 *             if {@link Target} can not be created from a given JAXB object
	 *             due syntactical error
	 */
	private Target create(TargetType target) throws XacmlSyntaxException {
		if (target == null) {
			return null;
		}
		Target.Builder b = Target.builder();
		for (AnyOfType anyOf : target.getAnyOf()) {
			b.anyOf(create(anyOf));
		}
		return b.build();
	}

	private MatchAnyOf create(AnyOfType anyOf) throws XacmlSyntaxException {
		Preconditions.checkArgument(anyOf != null);
		MatchAnyOf.Builder b = MatchAnyOf.builder();
		for (AllOfType allOf : anyOf.getAllOf()) {
			b.anyOf(create(allOf));
		}
		return b.build();
	}

	private MatchAllOf create(AllOfType allOf) throws XacmlSyntaxException {
		Preconditions.checkArgument(allOf != null);
		MatchAllOf.Builder b = MatchAllOf.builder();
		for (MatchType match : allOf.getMatch()) {
			b.allOf(createMatch(match));
		}
		return b.build();
	}

	/**
	 * Maps {@link MatchType} to the {@link Match}
	 *
	 * @param m
	 *            a JAXB representation of XACML match
	 * @return {@link Match} instance
	 * @throws XacmlSyntaxException
	 */
	private Match createMatch(MatchType m) throws XacmlSyntaxException {
		Preconditions.checkArgument(m != null);
		AttributeValueType v = m.getAttributeValue();
		if (v == null) {
			throw new XacmlSyntaxException(
					"Match=\"%s\" category value must be specified");
		}
		Optional<TypeToXacml30> toXacml30 = TypeToXacml30.Types.getIndex().get(v.getDataType());
		Preconditions.checkState(toXacml30.isPresent());
		return Match
				.builder()
				.predicate(createFunction(m.getMatchId()))
				.attribute(toXacml30.get().fromXacml30(m.getAttributeValue()))
				.attrRef(createAttributeReference((m.getAttributeDesignator() != null) ? m.getAttributeDesignator():m.getAttributeSelector()))
				.build();
	}

	/**
	 * Creates either {@link AttributeDesignator} or {@link AttributeSelector}
	 * based on the type of the given JAXB object.
	 *
	 * @param ref
	 *            an instance of {@link AttributeSelectorType} or
	 *            {@link AttributeDesignatorType}
	 * @return {@link AttributeReference} instance
	 * @throws XacmlSyntaxException
	 *             if given
	 * @exception IllegalArgumentException
	 *                if given argument is {@code null}
	 */
	private AttributeReference createAttributeReference(Object ref)
			throws XacmlSyntaxException {
		Preconditions.checkArgument(ref != null);
		if (ref instanceof AttributeSelectorType) {
			AttributeSelectorType selector = (AttributeSelectorType)ref;
			if(log.isDebugEnabled()){
				log.debug(selector.getPath());
			}
			Optional<AttributeExpType> type = XacmlTypes.getType(selector.getDataType());
			Preconditions.checkState(type.isPresent());
			return AttributeSelector
					.builder()
					.category(selector.getCategory())
					.xpath(selector.getPath())
					.contextSelectorId(selector.getContextSelectorId())
					.dataType(type.get())
					.mustBePresent(selector.isMustBePresent())
					.build();
		}
		if (ref instanceof AttributeDesignatorType) {
			AttributeDesignatorType desig = (AttributeDesignatorType) ref;
			Optional<AttributeExpType> type = XacmlTypes.getType(desig.getDataType());
			Preconditions.checkState(type.isPresent());
			return AttributeDesignator
					.builder()
					.category(desig.getCategory())
					.attributeId(desig.getAttributeId())
					.issuer(desig.getIssuer())
					.dataType(type.get())
					.mustBePresent(desig.isMustBePresent())
					.build();
		}
		throw new XacmlSyntaxException(
				"Given JAXB object instance of=\"%s\" can not be converted"
						+ "to XACML AttributeSelector or AttributeDesignator",
				ref.getClass().getName());
	}

	/**
	 * Creates {@link AttributeAssignmentExpression} from a given JAXB object
	 * {@link AttributeAssignmentExpressionType}
	 *
	 * @param exp
	 *            a JAXB instance
	 * @return {@link AttributeAssignmentExpression} instance
	 * @throws XacmlSyntaxException
	 */
	private AttributeAssignmentExpression create(
			AttributeAssignmentExpressionType exp,
			VariableManager<JAXBElement<?>> m) throws XacmlSyntaxException {
		Preconditions.checkArgument(exp != null);
		return AttributeAssignmentExpression
				.builder(exp.getAttributeId())
				.expression(parseExpression(exp.getExpression(), m))
				.category(exp.getCategory())
				.issuer(exp.getIssuer())
				.build();
	}

	/**
	 * Creates {@link Apply} from a given {@link ApplyType} JAXB object
	 *
	 * @param apply an JAXB XACML apply instance
	 * @param m an variable state manager
	 * @return {@link Apply} instance
	 * @throws XacmlSyntaxException if an syntax error
	 * occurs while mapping JAXB instance
	 */
	private Apply createApply(ApplyType apply, VariableManager<JAXBElement<?>> m) throws XacmlSyntaxException
	{
		List<Expression> arguments = new LinkedList<Expression>();
		for (JAXBElement<?> exp : apply.getExpression()) {
			arguments.add(parseExpression(exp, m));
		}
		return Apply
				.builder(createFunction(apply.getFunctionId()))
				.param(arguments)
				.build();
	}

	/**
	 * Creates instance of {@link Expression} from a given JAXB object
	 *
	 * @param expression expression JAXB element
	 * @return {@link Expression} instance
	 * @throws XacmlSyntaxException
	 */
	private Expression parseExpression(JAXBElement<?> expression,
			VariableManager<JAXBElement<?>> m) throws XacmlSyntaxException
	{
		Preconditions.checkArgument(expression != null);
		Object e = expression.getValue();
		if ((e instanceof AttributeDesignatorType)
				|| (e instanceof AttributeSelectorType)) {
			return createAttributeReference(e);
		}
		if (e instanceof ApplyType) {
			return createApply((ApplyType)e, m);
		}
		if (e instanceof AttributeValueType) {
			AttributeValueType t = (AttributeValueType) e;
			Optional<TypeToXacml30> toXacml30 = TypeToXacml30.Types.getIndex().get(t.getDataType());
			Preconditions.checkState(toXacml30.isPresent());
			return toXacml30.get().fromXacml30(t);
		}
		if (e instanceof VariableReferenceType) {
			VariableReferenceType varRef = (VariableReferenceType)e;
			VariableDefinition varDef = m.getVariableDefinition(varRef.getVariableId());
			if(varDef != null){
				return new VariableReference(varDef);
			}
			JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varRef.getVariableId());
			if(varDefExp == null){
				throw new XacmlSyntaxException("Variable with attributeId=\"%s\" " +
						"is not defined", varRef.getVariableId());
			}
			m.pushVariableDefinition(varRef.getVariableId());
			parseExpression(varDefExp, m);
			varDef = m.getVariableDefinition(varRef.getVariableId());
			Preconditions.checkState(varDef != null);
			m.resolveVariableDefinition(varDef);
			return new VariableReference(varDef);
		}
		if (e instanceof FunctionType) {
			FunctionType fRef = (FunctionType) e;
			return new FunctionReference(
					createFunction(fRef.getFunctionId()));
		}
		throw new XacmlSyntaxException(
				"Unknown XACML expression JAXB object=\"%s\"", e.getClass());
	}



	/**
	 * Adds {@link VariableDefinitionType} from a given {@link PolicyType}
	 * to the created {@link VariableManager} instance
	 *
	 * @param p an JAXB representation of XACML policy
	 * @return {@link VariableManager}
	 * @throws XacmlSyntaxException if and syntax error occurs
	 * while parsing variable definitions
	 */
	private VariableManager<JAXBElement<?>> getVariables(PolicyType p)
			throws XacmlSyntaxException
	{
		try {
			List<Object> objects = p
					.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
			if (objects == null || objects.isEmpty()) {
				return new VariableManager<JAXBElement<?>>(Collections
						.<String, JAXBElement<?>> emptyMap());
			}
			Map<String, JAXBElement<?>> expressions = new HashMap<String, JAXBElement<?>>();
			for (Object o : objects) {
				if (!(o instanceof VariableDefinitionType)) {
					continue;
				}
				VariableDefinitionType varDef = (VariableDefinitionType) o;
				if (expressions.containsKey(varDef.getVariableId())) {
					throw new XacmlSyntaxException(
							"Policy contains a variableId=\"%s\" is already "
									+ "used for previously defined variable",
							varDef.getVariableId());
				}
				expressions.put(varDef.getVariableId(), varDef.getExpression());
			}
			VariableManager<JAXBElement<?>> manager = new VariableManager<JAXBElement<?>>(
					expressions);
			parseVariables(manager);
			return manager;
		} catch (IllegalArgumentException e) {
			throw new XacmlSyntaxException(e);
		}
	}

	private void parseVariables(VariableManager<JAXBElement<?>> m)
		throws XacmlSyntaxException
	{
		for (String varId : m.getVariableDefinitionExpressions())
		{
			JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varId);
			m.pushVariableDefinition(varId);
			Expression expression = parseExpression(varDefExp, m);
			Preconditions.checkState(expression != null);
			if(log.isDebugEnabled()){
				log.debug("Resolved variable " +
						"definition variableId=\"{}\", " +
                        "expression=\"{}\"", varId, expression);
			}
			m.resolveVariableDefinition(new VariableDefinition(varId, expression));
		}
	}
}
