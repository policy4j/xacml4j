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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.AdviceExpressionType;
import org.oasis.xacml.v30.jaxb.AdviceExpressionsType;
import org.oasis.xacml.v30.jaxb.AllOfType;
import org.oasis.xacml.v30.jaxb.AnyOfType;
import org.oasis.xacml.v30.jaxb.AttributeAssignmentExpressionType;
import org.oasis.xacml.v30.jaxb.AttributeDesignatorType;
import org.oasis.xacml.v30.jaxb.AttributeSelectorType;
import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.ConditionType;
import org.oasis.xacml.v30.jaxb.DefaultsType;
import org.oasis.xacml.v30.jaxb.EffectType;
import org.oasis.xacml.v30.jaxb.IdReferenceType;
import org.oasis.xacml.v30.jaxb.MatchType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.ObligationExpressionType;
import org.oasis.xacml.v30.jaxb.ObligationExpressionsType;
import org.oasis.xacml.v30.jaxb.PolicyIssuerType;
import org.oasis.xacml.v30.jaxb.PolicySetType;
import org.oasis.xacml.v30.jaxb.PolicyType;
import org.oasis.xacml.v30.jaxb.RuleType;
import org.oasis.xacml.v30.jaxb.TargetType;
import org.oasis.xacml.v30.jaxb.VariableDefinitionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.AttributeAssignmentExpression;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.AttributeReference;
import org.xacml4j.v30.policy.AttributeSelector;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

final class Xacml30PolicyFromObjectModelToJaxbMapper
{
	private final static Logger log = LoggerFactory.getLogger(Xacml30PolicyFromObjectModelToJaxbMapper.class);

	private final static ObjectFactory factory = new ObjectFactory();

	private final static Map<Effect, EffectType> nativeToJaxbEffectMappings = ImmutableMap.of(
			Effect.DENY, EffectType.DENY,
			Effect.PERMIT, EffectType.PERMIT);

	public JAXBElement<?> map(CompositeDecisionRule d){
		if(d instanceof PolicySet){
			return factory.createPolicySet(toJaxb((PolicySet)d));
		}
		if(d instanceof Policy){
			return factory.createPolicy(toJaxb((Policy)d));
		}
		if(d instanceof Rule){
			return factory.createRule(toJaxb((Rule)d));
		}
		if(d instanceof PolicyIDReference){
			return factory.createPolicyIdReference(toJaxb((PolicyIDReference)d));
		}
		if(d instanceof PolicySetIDReference){
			return factory.createPolicySetIdReference(toJaxb((PolicySetIDReference)d));
		}
		throw new IllegalArgumentException(
				String.format(
						"Unsupported decision rule type=\"%s\"",
						d.getClass().getName()));
	}

	private IdReferenceType toJaxb(PolicyIDReference ref)
	{
		IdReferenceType jaxbRef = factory.createIdReferenceType();
		if(ref.getEarliestVersion() != null){
			jaxbRef.setEarliestVersion(ref.getEarliestVersion().getPattern());
		}
		if(ref.getLatestVersion() != null){
			jaxbRef.setLatestVersion(ref.getLatestVersion().getPattern());
		}
		if(ref.getVersion() != null){
			jaxbRef.setLatestVersion(ref.getVersion().getPattern());
		}
		jaxbRef.setValue(ref.getId());
		return jaxbRef;
	}

	private IdReferenceType toJaxb(PolicySetIDReference ref)
	{
		IdReferenceType jaxbRef = factory.createIdReferenceType();
		if(ref.getEarliestVersion() != null){
			jaxbRef.setEarliestVersion(ref.getEarliestVersion().getPattern());
		}
		if(ref.getLatestVersion() != null){
			jaxbRef.setLatestVersion(ref.getLatestVersion().getPattern());
		}
		if(ref.getVersion() != null){
			jaxbRef.setLatestVersion(ref.getVersion().getPattern());
		}
		jaxbRef.setValue(ref.getId());
		return jaxbRef;
	}

	private RuleType toJaxb(Rule rule){
		if(log.isDebugEnabled()){
			log.debug("Mapping Rule id=\"{}\"", rule.getId());
		}
		RuleType jaxbRule = factory.createRuleType();
		jaxbRule.setRuleId(rule.getId());
		jaxbRule.setDescription(rule.getDescription());
		jaxbRule.setTarget(toJaxb(rule.getTarget()));
		EffectType effect = nativeToJaxbEffectMappings.get(rule.getEffect());
		Preconditions.checkState(effect != null);
		jaxbRule.setEffect(effect);
		if(rule.getCondition() != null){
			ConditionType condition = factory.createConditionType();
			condition.setExpression(ExpressionTypeBuilder.Expressions.getBuilder(rule.getCondition().getExpression()).from(rule.getCondition().getExpression()));
			jaxbRule.setCondition(condition);
		}
		jaxbRule.setAdviceExpressions(toJaxbAdvices(rule.getAdviceExpressions()));
		jaxbRule.setObligationExpressions(toJaxbObligations(rule.getObligationExpressions()));
		return jaxbRule;
	}

	private PolicyType toJaxb(Policy p){
		if(log.isDebugEnabled()){
			log.debug("Mapping Policy id=\"{}\"", p.getId());
		}
		PolicyType jaxbPolicy = factory.createPolicyType();
		jaxbPolicy.setVersion(p.getVersion().getValue());
		jaxbPolicy.setPolicyId(p.getId());
		jaxbPolicy.setPolicyIssuer(toJaxb(p.getIssuer()));
		jaxbPolicy.setPolicyDefaults(toJaxb(p.getDefaults()));
		jaxbPolicy.setDescription(p.getDescription());
		jaxbPolicy.setTarget(toJaxb(p.getTarget()));
		jaxbPolicy.setRuleCombiningAlgId(p.getRuleCombiningAlgorithm().getId());
		if(p.getCondition() != null){
			ConditionType condition = factory.createConditionType();
			condition.setExpression(ExpressionTypeBuilder.Expressions.getBuilder(p.getCondition().getExpression()).from(p.getCondition().getExpression()));
			jaxbPolicy.setCondition(condition);
		}
		jaxbPolicy.setAdviceExpressions(toJaxbAdvices(p.getAdviceExpressions()));
		jaxbPolicy.setObligationExpressions(toJaxbObligations(p.getObligationExpressions()));
		for(VariableDefinition var : p.getVariableDefinitions()){
			jaxbPolicy.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(toJaxb(var));
		}
		for(Rule r : p.getRules()){
			jaxbPolicy.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(toJaxb(r));
		}
		return jaxbPolicy;
	}

	private PolicySetType toJaxb(PolicySet ps){
		if(log.isDebugEnabled()){
			log.debug("Mapping PolicySet id=\"{}\"", ps.getId());
		}
		PolicySetType jaxbPolicySet = factory.createPolicySetType();
		jaxbPolicySet.setPolicySetId(ps.getId());
		jaxbPolicySet.setVersion(ps.getVersion().getValue());
		jaxbPolicySet.setPolicyIssuer(toJaxb(ps.getIssuer()));
		jaxbPolicySet.setPolicySetDefaults(toJaxb(ps.getDefaults()));
		jaxbPolicySet.setDescription(ps.getDescription());
		jaxbPolicySet.setTarget(toJaxb(ps.getTarget()));
		jaxbPolicySet.setPolicyCombiningAlgId(ps.getPolicyDecisionCombiningAlgorithm().getId());
		if(ps.getCondition() != null){
			ConditionType condition = factory.createConditionType();
			condition.setExpression(ExpressionTypeBuilder.Expressions.getBuilder(ps.getCondition().getExpression()).from(ps.getCondition().getExpression()));
			jaxbPolicySet.setCondition(condition);
		}
		jaxbPolicySet.setAdviceExpressions(toJaxbAdvices(ps.getAdviceExpressions()));
		jaxbPolicySet.setObligationExpressions(toJaxbObligations(ps.getObligationExpressions()));
		for(CompositeDecisionRule r : ps.getDecisions()){
			jaxbPolicySet.getPolicySetOrPolicyOrPolicySetIdReference().add(map(r));
		}
		return jaxbPolicySet;
	}

	private DefaultsType toJaxb(PolicyDefaults d)
	{
		if(d == null){
			return null;
		}
		DefaultsType jaxbDef = factory.createDefaultsType();
		jaxbDef.setXPathVersion(d.getXPathVersion().toString());
		return jaxbDef;
	}

	private DefaultsType toJaxb(PolicySetDefaults d)
	{
		if(d == null){
			return null;
		}
		DefaultsType jaxbDef = factory.createDefaultsType();
		jaxbDef.setXPathVersion(d.getXPathVersion().toString());
		return jaxbDef;
	}

	private VariableDefinitionType toJaxb(VariableDefinition var)
	{
		Preconditions.checkNotNull(var);
		VariableDefinitionType jaxbVar = factory.createVariableDefinitionType();
		jaxbVar.setVariableId(var.getVariableId());
		JAXBElement<?> expression = ExpressionTypeBuilder.Expressions.getBuilder(var.getExpression()).from(var.getExpression());
		jaxbVar.setExpression(expression);
		return jaxbVar;
	}

	private PolicyIssuerType toJaxb(Entity issuer)
	{
		if(issuer == null){
			return null;
		}
		PolicyIssuerType jaxbIssuer = factory.createPolicyIssuerType();
		if(issuer.getContent() != null & issuer.hasContent()){
			jaxbIssuer.getContent().getContent()
					.add(issuer.getContent().map(v->v.toNode()).get());
		}
		for(Attribute a : issuer.getAttributes()){
			jaxbIssuer.getAttribute().add(toJaxb(a));
		}
		return jaxbIssuer;
	}

	private AttributeType toJaxb(Attribute a)
	{
		Preconditions.checkNotNull(a);
		AttributeType jaxbAttr = factory.createAttributeType();
		jaxbAttr.setAttributeId(a.getAttributeId());
		jaxbAttr.setIncludeInResult(a.isIncludeInResult());
		jaxbAttr.setIssuer(a.getIssuer());
		for(Value v : a.getValues()){
			jaxbAttr.getAttributeValue().add(toJaxb(v));
		}
		return jaxbAttr;
	}

	private AttributeValueType toJaxb(Value a)
	{
		Preconditions.checkNotNull(a);
		Optional<TypeToXacml30> toXacml30 = TypeToXacml30.forType(a.getType());
		Preconditions.checkState(toXacml30.isPresent());
		return toXacml30.get().toXacml30(a);
	}

	private TargetType toJaxb(Target t)
	{
		if(t == null){
			return null;
		}
		TargetType jaxbTarget = factory.createTargetType();
		for(MatchAnyOf m : t.getAnyOf()){
			jaxbTarget.getAnyOf().add(toJaxb(m));
		}
		return jaxbTarget;
	}

	private AnyOfType toJaxb(MatchAnyOf t)
	{
		Preconditions.checkNotNull(t);
		AnyOfType jaxbAnyOf = factory.createAnyOfType();
		for(MatchAllOf m : t.getAllOf()){
			jaxbAnyOf.getAllOf().add(toJaxb(m));
		}
		return jaxbAnyOf;
	}

	private AllOfType toJaxb(MatchAllOf t){
		Preconditions.checkNotNull(t);
		AllOfType jaxbAllOf = factory.createAllOfType();
		for(Match m : t.getMatch()){
			jaxbAllOf.getMatch().add(toJaxb(m));
		}
		return jaxbAllOf;
	}

	private MatchType toJaxb(Match t)
	{
		Preconditions.checkNotNull(t);
		MatchType jaxbMatch = factory.createMatchType();
		jaxbMatch.setMatchId(t.getMatchId());
		jaxbMatch.setAttributeValue(toJaxb(t.getAttributeValue()));
		AttributeReference ref = t.getReference();
		if(ref instanceof AttributeSelector){
			@SuppressWarnings("unchecked")
			JAXBElement<AttributeSelectorType> s = (JAXBElement<AttributeSelectorType>)ExpressionTypeBuilder.Expressions
			.getBuilder(ref)
			.from(ref);
			jaxbMatch.setAttributeSelector(s.getValue());
		}
		if(ref instanceof AttributeDesignator){
			@SuppressWarnings("unchecked")
			JAXBElement<AttributeDesignatorType> d = (JAXBElement<AttributeDesignatorType>)ExpressionTypeBuilder.Expressions.getBuilder(ref)
			.from(ref);
			jaxbMatch.setAttributeDesignator(d.getValue());
		}
		return jaxbMatch;
	}

	private ObligationExpressionsType toJaxbObligations(
			Collection<ObligationExpression> obligations){
		if(obligations == null ||
				obligations.isEmpty()){
			return null;
		}
		ObligationExpressionsType o = factory.createObligationExpressionsType();
		for(ObligationExpression e : obligations){
			o.getObligationExpression().add(toJaxb(e));
		}
		return o;
	}

	private AdviceExpressionsType toJaxbAdvices(
			Collection<AdviceExpression> advices){
		if(advices == null ||
				advices.isEmpty()){
			return null;
		}
		AdviceExpressionsType a = factory.createAdviceExpressionsType();
		for(AdviceExpression advice : advices){
			a.getAdviceExpression().add(toJaxb(advice));
		}
		return a;
	}

	private ObligationExpressionType toJaxb(ObligationExpression o){
		Preconditions.checkNotNull(o);
		ObligationExpressionType jaxb = factory.createObligationExpressionType();
		jaxb.setObligationId(o.getId());
		EffectType effect = nativeToJaxbEffectMappings.get(o.getEffect());
		Preconditions.checkState(effect != null);
		jaxb.setFulfillOn(effect);
		jaxb.getAttributeAssignmentExpression().addAll(toJaxb(o.getAttributeAssignmentExpressions()));
		return jaxb;
	}

	private AdviceExpressionType toJaxb(AdviceExpression o){
		Preconditions.checkNotNull(o);
		AdviceExpressionType jaxb = factory.createAdviceExpressionType();
		jaxb.setAdviceId(o.getId());
		EffectType effect = nativeToJaxbEffectMappings.get(o.getEffect());
		Preconditions.checkState(effect != null);
		jaxb.setAppliesTo(effect);
		jaxb.getAttributeAssignmentExpression().addAll(toJaxb(o.getAttributeAssignmentExpressions()));
		return jaxb;
	}

	private Collection<AttributeAssignmentExpressionType> toJaxb(
			Collection<AttributeAssignmentExpression> exps){
		ImmutableList.Builder<AttributeAssignmentExpressionType> jaxbExps = ImmutableList.builder();
		for(AttributeAssignmentExpression  e : exps){
			jaxbExps.add(toJaxb(e));
		}
		return jaxbExps.build();
	}

	private AttributeAssignmentExpressionType toJaxb(
			AttributeAssignmentExpression exp){
		AttributeAssignmentExpressionType jaxbExp = factory.createAttributeAssignmentExpressionType();
		JAXBElement<?> expression = ExpressionTypeBuilder.Expressions.getBuilder(exp.getExpression()).from(exp.getExpression());
		jaxbExp.setExpression(expression);
		jaxbExp.setAttributeId(exp.getAttributeId());
		jaxbExp.setCategory(exp.getCategory().getId());
		jaxbExp.setIssuer(exp.getIssuer());
		return jaxbExp;
	}
}
