package com.artagon.xacml.v30;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasis.xacml.v30.jaxb.AdviceExpressionType;
import org.oasis.xacml.v30.jaxb.AdviceExpressionsType;
import org.oasis.xacml.v30.jaxb.AllOfType;
import org.oasis.xacml.v30.jaxb.AnyOfType;
import org.oasis.xacml.v30.jaxb.ApplyType;
import org.oasis.xacml.v30.jaxb.AttributeAssignmentExpressionType;
import org.oasis.xacml.v30.jaxb.AttributeDesignatorType;
import org.oasis.xacml.v30.jaxb.AttributeSelectorType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.ConditionType;
import org.oasis.xacml.v30.jaxb.DefaultsType;
import org.oasis.xacml.v30.jaxb.EffectType;
import org.oasis.xacml.v30.jaxb.FunctionType;
import org.oasis.xacml.v30.jaxb.IdReferenceType;
import org.oasis.xacml.v30.jaxb.MatchType;
import org.oasis.xacml.v30.jaxb.ObligationExpressionType;
import org.oasis.xacml.v30.jaxb.ObligationExpressionsType;
import org.oasis.xacml.v30.jaxb.PolicySetType;
import org.oasis.xacml.v30.jaxb.PolicyType;
import org.oasis.xacml.v30.jaxb.RuleType;
import org.oasis.xacml.v30.jaxb.TargetType;
import org.oasis.xacml.v30.jaxb.VariableDefinitionType;
import org.oasis.xacml.v30.jaxb.VariableReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.VariableManager;
import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.Apply;
import com.artagon.xacml.v3.AttributeAssignmentExpression;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReference;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.AttributeValue;
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
import com.artagon.xacml.v3.VersionMatch;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class Xacml30PolicyMapper 
{
	private final static Logger log = LoggerFactory.getLogger(Xacml30PolicyMapper.class);
	
	private PolicyFactory factory;

	private final static Map<Effect, EffectType> nativeToJaxbEffectMappings = new HashMap<Effect, EffectType>();
	private final static Map<EffectType, Effect> jaxbToNativeEffectMappings = new HashMap<EffectType, Effect>();

	static {
		nativeToJaxbEffectMappings.put(Effect.DENY, EffectType.DENY);
		nativeToJaxbEffectMappings.put(Effect.PERMIT, EffectType.PERMIT);
		jaxbToNativeEffectMappings.put(EffectType.DENY, Effect.DENY);
		jaxbToNativeEffectMappings.put(EffectType.PERMIT, Effect.PERMIT);
	}

	public Xacml30PolicyMapper(PolicyFactory factory) {
		Preconditions.checkNotNull(factory);
		this.factory = factory;
	}

	/**
	 * Creates {@link Policy} instance from a giveb JAXB
	 * {@link PolicyType} object
	 * 
	 * @param p a JAXB policy representation
	 * @return {@link Policy} instance
	 * @throws PolicySyntaxException if syntax error occurs
	 * while creating XACML policy
	 */
	public Policy createPolicy(PolicyType p) throws PolicySyntaxException 
	{
		VariableManager<JAXBElement<?>> variableDef = getVariables(p);
		Collection<AdviceExpression> adviceExpressions = getExpressions(
				p.getAdviceExpressions(), variableDef);
		Collection<ObligationExpression> obligationExpressions = getExpressions(
				p.getObligationExpressions(), variableDef);
		Target target = create(p.getTarget());
		return factory.createPolicy(p.getPolicyId(), p.getVersion() , 
				p.getDescription(), createPolicyDefaults(p.getPolicyDefaults()), target, 
				variableDef.getVariableDefinitions().values(), p.getRuleCombiningAlgId(), 
				createRules(p, variableDef), obligationExpressions, adviceExpressions);
	}
	
	private Collection<Rule> createRules(PolicyType p, VariableManager<JAXBElement<?>> m) 
		throws PolicySyntaxException
	{
		Collection<Rule> rules = new LinkedList<Rule>();
		for(Object o : p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition())
		{
			if(o instanceof RuleType){
				rules.add(create((RuleType)o, m));
			}
		}
		return rules;
	}
	
	/**
	 * Tries to create either {@link Policy} or {@link PolicySet}
	 * based on runtime type of the given JAXB object
	 * 
	 * @param jaxbObject a JAXB object representing either 
	 * {@link PolicySetType} or {@link PolicyType}
	 * @return {@link CompositeDecisionRule}
	 * @throws PolicySyntaxException if given policy or policy set
	 * can not be created due syntactical errors
	 */
	public CompositeDecisionRule create(Object jaxbObject) throws PolicySyntaxException
	{
		if(jaxbObject instanceof PolicyType){
			return createPolicy((PolicyType)jaxbObject);
		}
		if(jaxbObject instanceof PolicySetType){
			return createPolicySet((PolicySetType)jaxbObject);
		}
		throw new PolicySyntaxException(
				"Given object can not be mapped to Policy or PolicySet");
	}
	
	/**
	 * Creates {@link PolicySet} instance from a given JAXB
	 * {@link PolicySetType} object
	 * 
	 * @param p a JAXB policy set representation
	 * @return {@link PolicySet} instance
	 * @throws PolicySyntaxException if syntax error occurs
	 * while creating XACML policy set
	 */
	public PolicySet createPolicySet(PolicySetType p) throws PolicySyntaxException 
	{
		VariableManager<JAXBElement<?>> variableDef = new VariableManager<JAXBElement<?>>(
				Collections.<String, JAXBElement<?>>emptyMap());
		Collection<AdviceExpression> adviceExpressions = getExpressions(p.getAdviceExpressions(), variableDef);
		Collection<ObligationExpression> obligationExpressions = getExpressions(p.getObligationExpressions(), variableDef);
		Target target = create(p.getTarget());
		return factory.createPolicySet(
				p.getPolicySetId(), 
				p.getVersion(),
				p.getDescription(), 
				createPolicySetDefaults(p.getPolicySetDefaults()),   
				target, p.getPolicyCombiningAlgId(), 
				createPolicies(p), obligationExpressions, adviceExpressions);
	}
	
	private Collection<CompositeDecisionRule> createPolicies(PolicySetType policySet) 
		throws PolicySyntaxException
	{
		Collection<CompositeDecisionRule> all = new LinkedList<CompositeDecisionRule>();
		for(JAXBElement<?> e : policySet.getPolicySetOrPolicyOrPolicySetIdReference())
		{
			if(e.getValue() instanceof PolicyType){
				all.add(createPolicy((PolicyType)e.getValue()));
				continue;
			}
			if(e.getValue() instanceof PolicySetType){
				all.add(create((PolicySetType)e.getValue()));
				continue;
			}
			if(e.getValue() instanceof IdReferenceType)
			{
					IdReferenceType ref = (IdReferenceType)e.getValue();
					if (e.getName().getLocalPart().equals("PolicySetIdReference")) {
						if(ref.getValue() == null){
							throw new PolicySyntaxException(
									"PolicySet reference id can't be null");
						}
						PolicySetIDReference policySetRef = factory
								.createPolicySetIDReference(ref.getValue(),
										(ref.getVersion() != null)?VersionMatch.parse(ref.getVersion()):null,
										(ref.getEarliestVersion() != null)?VersionMatch.parse(ref.getEarliestVersion()):null,
										(ref.getLatestVersion() != null)?VersionMatch.parse(ref.getLatestVersion()):null);
						all.add(policySetRef);
						continue;
					}
					if(e.getName().getLocalPart().equals("PolicyIdReference")) {
						if(ref.getValue() == null){
							throw new PolicySyntaxException(
									"Policy reference id can't be null");
						}
						PolicyIDReference policyRef = factory
								.createPolicyIDReference(ref.getValue(),
										(ref.getVersion() != null)?VersionMatch.parse(ref.getVersion()):null,
										(ref.getEarliestVersion() != null)?VersionMatch.parse(ref.getEarliestVersion()):null,
										(ref.getLatestVersion() != null)?VersionMatch.parse(ref.getLatestVersion()):null);
						all.add(policyRef);
						continue;
					}
				}
		}
		return all;
	}

	private Rule create(RuleType r, VariableManager<JAXBElement<?>> variables)
		throws PolicySyntaxException 
	{
		Effect effect = r.getEffect() == EffectType.DENY ? Effect.DENY: Effect.PERMIT;
		return factory.createRule(r.getRuleId(), r.getDescription(), 
				create(r.getTarget()), create(r.getCondition(), variables), effect);
	}
	
	private Condition create(ConditionType c, VariableManager<JAXBElement<?>> variables)
		throws PolicySyntaxException 
	{
		if (c == null) {
			return null;
		}
		JAXBElement<?> expression = c.getExpression();
		if(expression == null){
			return null;
		}
		return factory.createCondition(parseExpression(expression, variables));
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

	private Collection<AdviceExpression> getExpressions(
			AdviceExpressionsType expressions, VariableManager<JAXBElement<?>> m) throws PolicySyntaxException 
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
	 * @throws PolicySyntaxException
	 */
	private Collection<ObligationExpression> getExpressions(
			ObligationExpressionsType expressions, VariableManager<JAXBElement<?>> m) throws PolicySyntaxException 
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
	 * @throws PolicySyntaxException
	 *             if {@link AdviceExpression} can not be created from a given
	 *             JAXB object due syntactical error
	 */
	private AdviceExpression create(AdviceExpressionType exp, VariableManager<JAXBElement<?>> m)
			throws PolicySyntaxException {
		Preconditions.checkArgument(exp != null);
		Collection<AttributeAssignmentExpression> attrExp = new LinkedList<AttributeAssignmentExpression>();
		for (AttributeAssignmentExpressionType e : exp
				.getAttributeAssignmentExpression()) {
			attrExp.add(create(e, m));
		}
		return new AdviceExpression(exp.getAdviceId(),
				jaxbToNativeEffectMappings.get(exp.getAppliesTo()), attrExp);
	}

	/**
	 * Maps {@link ObligationExpressionsType} to {@link ObligationExpression}
	 * 
	 * @param exp
	 *            a JAXB object representing {@link ObligationExpression}
	 * @return {@link ObligationExpression} instance
	 * @throws PolicySyntaxException
	 *             if {@link ObligationExpression} can not be created from a
	 *             given JAXB object due syntactical error
	 */
	private ObligationExpression create(ObligationExpressionType exp, 
			VariableManager<JAXBElement<?>> m)
			throws PolicySyntaxException {
		Preconditions.checkArgument(exp != null);
		Collection<AttributeAssignmentExpression> attrExp = new LinkedList<AttributeAssignmentExpression>();
		for (AttributeAssignmentExpressionType e : exp
				.getAttributeAssignmentExpression()) {
			attrExp.add(create(e, m));
		}
		return new ObligationExpression(exp.getObligationId(),
				jaxbToNativeEffectMappings.get(exp.getFulfillOn()), attrExp);
	}

	/**
	 * Maps {@link TargetType} to {@link Target}
	 * 
	 * @param target
	 *            a JAXB target instance
	 * @return {@link Target} instance
	 * @throws PolicySyntaxException
	 *             if {@link Target} can not be created from a given JAXB object
	 *             due syntactical error
	 */
	private Target create(TargetType target) throws PolicySyntaxException {
		if (target == null) {
			return null;
		}
		Collection<MatchAnyOf> m = new LinkedList<MatchAnyOf>();
		for (AnyOfType anyOf : target.getAnyOf()) {
			m.add(create(anyOf));
		}
		return new Target(m);
	}

	private MatchAnyOf create(AnyOfType anyOf) throws PolicySyntaxException {
		Preconditions.checkArgument(anyOf != null);
		Collection<MatchAllOf> m = new LinkedList<MatchAllOf>();
		for (AllOfType allOf : anyOf.getAllOf()) {
			m.add(create(allOf));
		}
		return factory.createAnyOf(m);
	}

	private MatchAllOf create(AllOfType allOf) throws PolicySyntaxException {
		Preconditions.checkArgument(allOf != null);
		List<Match> m = new LinkedList<Match>();
		for (MatchType match : allOf.getMatch()) {
			m.add(createMatch(match));
		}
		return factory.createAllOf(m);
	}

	/**
	 * Maps {@link MatchType} to the {@link Match}
	 * 
	 * @param m
	 *            a JAXB representation of XACML match
	 * @return {@link Match} instance
	 * @throws PolicySyntaxException
	 */
	private Match createMatch(MatchType m) throws PolicySyntaxException {
		Preconditions.checkArgument(m != null);
		AttributeValueType v = m.getAttributeValue();
		if (v == null) {
			throw new PolicySyntaxException(
					"Match=\"%s\" attribute value must be specified");
		}
		return factory.createMatch(m.getMatchId(), createValue(v.getDataType(),
				v.getContent(), v.getOtherAttributes()), createAttributeReference((m
				.getAttributeDesignator() != null) ? m.getAttributeDesignator()
				: m.getAttributeSelector()));
	}

	/**
	 * Creates either {@link AttributeDesignator} or {@link AttributeSelector}
	 * based on the type of the given JAXB object.
	 * 
	 * @param ref
	 *            an instance of {@link AttributeSelectorType} or
	 *            {@link AttributeDesignatorType}
	 * @return {@link AttributeReference} instance
	 * @throws PolicySyntaxException
	 *             if given
	 * @exception IllegalArgumentException
	 *                if given argument is <code>null</code>
	 */
	private AttributeReference createAttributeReference(Object ref)
			throws PolicySyntaxException {
		Preconditions.checkArgument(ref != null);
		if (ref instanceof AttributeSelectorType) {
			AttributeSelectorType selector = (AttributeSelectorType)ref;
			if(log.isDebugEnabled()){
				log.debug(selector.getPath());
			}
			return factory.createAttributeSelector(
					selector.getCategory(),
					selector.getPath(), 
					selector.getContextSelectorId(),
					selector.getDataType(), 
					selector.isMustBePresent());
		}
		if (ref instanceof AttributeDesignatorType) {
			AttributeDesignatorType desig = (AttributeDesignatorType) ref;
			return factory.createAttributeDesignator(desig.getCategory(), 
					desig.getAttributeId(), 
					desig.getDataType(), 
					desig.isMustBePresent(), 
					desig.getIssuer());
		}
		throw new PolicySyntaxException(
				"Given JAXB object instance of=\"%s\" can not be converted"
						+ "to XACML AttributeSelector or AttributeDesignator",
				ref.getClass().getName());
	}

	/**
	 * Creates {@link AttributeValueExpression} instance
	 * 
	 * @param dataType
	 *            a data type identifier
	 * @param content
	 *            an list with attribute content
	 * @return {@link AttributeValueExpression} instance
	 * @throws PolicySyntaxException
	 */
	private AttributeValue createValue(String dataType, List<Object> content, 
			Map<QName, String> otherAttributes)
			throws PolicySyntaxException {
		if (content == null || content.isEmpty()) {
			throw new PolicySyntaxException("Attribute does not have content");
		}
		return factory.createAttributeValue(dataType, Iterables
				.getOnlyElement(content), otherAttributes);
	}

	/**
	 * Creates {@link AttributeAssignmentExpression} from a given JAXB object
	 * {@link AttributeAssignmentExpressionType}
	 * 
	 * @param exp
	 *            a JAXB instance
	 * @return {@link AttributeAssignmentExpression} instance
	 * @throws PolicySyntaxException
	 */
	private AttributeAssignmentExpression create(
			AttributeAssignmentExpressionType exp, 
			VariableManager<JAXBElement<?>> m) throws PolicySyntaxException {
		Preconditions.checkArgument(exp != null);
		return factory.createAttributeAssigmentExpression(exp.getAttributeId(),
				parseExpression(exp.getExpression(), m), exp.getCategory(), exp
						.getIssuer());
	}

	/**
	 * Creates {@link Apply} from a given {@link ApplyType} JAXB object
	 * 
	 * @param apply an JAXB XACML apply instance
	 * @param m an variable state manager
	 * @return {@link Apply} instance
	 * @throws PolicySyntaxException if an syntax error
	 * occurs while mapping JAXB instance
	 */
	private Apply createApply(ApplyType apply, VariableManager<JAXBElement<?>> m) throws PolicySyntaxException 
	{
		Collection<Expression> arguments = new LinkedList<Expression>();
		for (JAXBElement<?> exp : apply.getExpression()) {
			arguments.add(parseExpression(exp, m));
		}
		return factory.createApply(apply.getFunctionId(), arguments);
	}

	/**
	 * Creates instance of {@link Expression} from a given JAXB object
	 * 
	 * @param expression
	 * @return
	 * @throws PolicySyntaxException
	 */
	private Expression parseExpression(JAXBElement<?> expression,
			VariableManager<JAXBElement<?>> m) throws PolicySyntaxException 
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
			return createValue(t.getDataType(), t.getContent(), 
					t.getOtherAttributes());
		}
		if (e instanceof VariableReferenceType) {
			VariableReferenceType varRef = (VariableReferenceType)e;
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
		if (e instanceof FunctionType) {
			FunctionType fRef = (FunctionType) e;
			return factory.createFunctionReference(fRef.getFunctionId());
		}
		throw new PolicySyntaxException(
				"Unknown XACML expression JAXB object=\"%s\"", e.getClass());
	}

	

	/**
	 * Adds {@link VariableDefinitionType} from a given {@link PolicyType} 
	 * to the created {@link VariableManager} instance
	 * 
	 * @param p an JAXB representation of XACML policy
	 * @return {@link VariableManager}
	 * @throws PolicySyntaxException if and syntax error occurs
	 * while parsing variable definitions
	 */
	private VariableManager<JAXBElement<?>> getVariables(PolicyType p)
			throws PolicySyntaxException 
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
					throw new PolicySyntaxException(
							"Policy contains a variableId=\"%s\" is alerady "
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
			throw new PolicySyntaxException(e);
		}
	}
	
	private void parseVariables(VariableManager<JAXBElement<?>> m)
		throws PolicySyntaxException 
	{
		for (String varId : m.getVariableDefinitionExpressions()) 
		{
			JAXBElement<?> varDefExp = m.getVariableDefinitionExpression(varId);
			m.pushVariableDefinition(varId);
			Expression expression = parseExpression(varDefExp, m);
			Preconditions.checkState(expression != null);
			if(log.isDebugEnabled()){
				log.debug("Resolved variable " +
						"defintion variableId=\"{}\", expression=\"{}\"", varId, expression);
			}
			m.resolveVariableDefinition(factory.createVariableDefinition(varId, expression));
	}
}
}
