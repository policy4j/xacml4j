package com.artagon.xacml.v30.marshall.jaxb;

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
import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.ConditionType;
import org.oasis.xacml.v30.jaxb.ContentType;
import org.oasis.xacml.v30.jaxb.DefaultsType;
import org.oasis.xacml.v30.jaxb.EffectType;
import org.oasis.xacml.v30.jaxb.FunctionType;
import org.oasis.xacml.v30.jaxb.IdReferenceType;
import org.oasis.xacml.v30.jaxb.MatchType;
import org.oasis.xacml.v30.jaxb.ObligationExpressionType;
import org.oasis.xacml.v30.jaxb.ObligationExpressionsType;
import org.oasis.xacml.v30.jaxb.PolicyIssuerType;
import org.oasis.xacml.v30.jaxb.PolicySetType;
import org.oasis.xacml.v30.jaxb.PolicyType;
import org.oasis.xacml.v30.jaxb.RuleType;
import org.oasis.xacml.v30.jaxb.TargetType;
import org.oasis.xacml.v30.jaxb.VariableDefinitionType;
import org.oasis.xacml.v30.jaxb.VariableReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v30.AdviceExpression;
import com.artagon.xacml.v30.Apply;
import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.AttributeAssignmentExpression;
import com.artagon.xacml.v30.AttributeDesignator;
import com.artagon.xacml.v30.AttributeReference;
import com.artagon.xacml.v30.AttributeSelector;
import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.CombinerParameters;
import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.Condition;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.FunctionReference;
import com.artagon.xacml.v30.Match;
import com.artagon.xacml.v30.MatchAllOf;
import com.artagon.xacml.v30.MatchAnyOf;
import com.artagon.xacml.v30.ObligationExpression;
import com.artagon.xacml.v30.Policy;
import com.artagon.xacml.v30.PolicyCombinerParameters;
import com.artagon.xacml.v30.PolicyDefaults;
import com.artagon.xacml.v30.PolicyIDReference;
import com.artagon.xacml.v30.PolicyIssuer;
import com.artagon.xacml.v30.PolicySet;
import com.artagon.xacml.v30.PolicySetCombinerParameters;
import com.artagon.xacml.v30.PolicySetDefaults;
import com.artagon.xacml.v30.PolicySetIDReference;
import com.artagon.xacml.v30.Rule;
import com.artagon.xacml.v30.Target;
import com.artagon.xacml.v30.VariableDefinition;
import com.artagon.xacml.v30.VariableReference;
import com.artagon.xacml.v30.XacmlSyntaxException;
import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.core.Version;
import com.artagon.xacml.v30.marshall.PolicyUnmarshallerSupport;
import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v30.spi.function.FunctionProvider;
import com.artagon.xacml.v30.types.DataTypes;
import com.google.common.base.Preconditions;

public class Xacml30PolicyFromJaxbToObjectModelMapper 
	extends PolicyUnmarshallerSupport
{
	private final static Logger log = LoggerFactory.getLogger(Xacml30PolicyFromJaxbToObjectModelMapper.class);
	

	private final static Map<Effect, EffectType> nativeToJaxbEffectMappings = new HashMap<Effect, EffectType>();
	private final static Map<EffectType, Effect> jaxbToNativeEffectMappings = new HashMap<EffectType, Effect>();

	static {
		nativeToJaxbEffectMappings.put(Effect.DENY, EffectType.DENY);
		nativeToJaxbEffectMappings.put(Effect.PERMIT, EffectType.PERMIT);
		jaxbToNativeEffectMappings.put(EffectType.DENY, Effect.DENY);
		jaxbToNativeEffectMappings.put(EffectType.PERMIT, Effect.PERMIT);
	}

	public Xacml30PolicyFromJaxbToObjectModelMapper(
			FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms) throws Exception{
		super(functions, decisionAlgorithms);
	}

	/**
	 * Creates {@link Policy} instance from a giveb JAXB
	 * {@link PolicyType} object
	 * 
	 * @param p a JAXB policy representation
	 * @return {@link Policy} instance
	 * @throws XacmlSyntaxException if syntax error occurs
	 * while creating XACML policy
	 */
	public Policy createPolicy(PolicyType p) throws XacmlSyntaxException 
	{
		VariableManager<JAXBElement<?>> variableDef = getVariables(p);
		Collection<AdviceExpression> adviceExpressions = getExpressions(
				p.getAdviceExpressions(), variableDef);
		Collection<ObligationExpression> obligationExpressions = getExpressions(
				p.getObligationExpressions(), variableDef);
		Target target = create(p.getTarget());
		return new Policy(p.getPolicyId(), 
				Version.parse(p.getVersion()), 
				p.getDescription(), 
				createPolicyDefaults(p.getPolicyDefaults()), 
				target, 
				variableDef.getVariableDefinitions().values(),  
				createRuleCombingingAlgorithm(p.getRuleCombiningAlgId()), 
				createRules(p, variableDef), 
				adviceExpressions, obligationExpressions);
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
		VariableManager<JAXBElement<?>> variableDef = new VariableManager<JAXBElement<?>>(
				Collections.<String, JAXBElement<?>>emptyMap());
		Collection<AdviceExpression> adviceExpressions = getExpressions(p.getAdviceExpressions(), variableDef);
		Collection<ObligationExpression> obligationExpressions = getExpressions(p.getObligationExpressions(), variableDef);
		Target target = create(p.getTarget());
		Collection<CombinerParameters> combinerParameters = new LinkedList<CombinerParameters>();
		Collection<PolicyCombinerParameters> policyCombinerParameters = new LinkedList<PolicyCombinerParameters>();
		Collection<PolicySetCombinerParameters> policySetCombinerParameters = new LinkedList<PolicySetCombinerParameters>();
		return new PolicySet(
				p.getPolicySetId(), 
				Version.parse(p.getVersion()), 
				p.getDescription(), 
				createPolicySetDefaults(p.getPolicySetDefaults()), 
				createPolicyIssuer(p.getPolicyIssuer()),
				target,   
				combinerParameters,
				policyCombinerParameters,
				policySetCombinerParameters,
				createPolicyCombingingAlgorithm(p.getPolicyCombiningAlgId()), 
				createPolicies(p), 
				adviceExpressions, obligationExpressions); 
	}
	
	private Collection<CompositeDecisionRule> createPolicies(PolicySetType policySet) 
		throws XacmlSyntaxException
	{
		Collection<CompositeDecisionRule> all = new LinkedList<CompositeDecisionRule>();
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
									"PolicySet reference id can't be null");
						}
						PolicySetIDReference policySetRef = PolicySetIDReference.create(ref.getValue(),
										ref.getVersion(),
										ref.getEarliestVersion(),
										ref.getLatestVersion());
						all.add(policySetRef);
						continue;
					}
					if(e.getName().getLocalPart().equals("PolicyIdReference")) {
						if(ref.getValue() == null){
							throw new XacmlSyntaxException(
									"Policy reference id can't be null");
						}
						PolicyIDReference policySetRef = PolicyIDReference.create(ref.getValue(),
								ref.getVersion(),
								ref.getEarliestVersion(),
								ref.getLatestVersion());
						all.add(policySetRef);
						continue;
					}
				}
		}
		return all;
	}

	private Rule create(RuleType r, VariableManager<JAXBElement<?>> variables)
		throws XacmlSyntaxException 
	{
		Effect effect = r.getEffect() == EffectType.DENY ? Effect.DENY: Effect.PERMIT;
		return new Rule(r.getRuleId(), r.getDescription(), 
				create(r.getTarget()), create(r.getCondition(), variables), effect);
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
	
	private PolicyIssuer createPolicyIssuer(PolicyIssuerType issuer) 
		throws XacmlSyntaxException
	{
		if(issuer == null){
			return null;
		}
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		for(AttributeType a : issuer.getAttribute()){
			attributes.add(create(a));
		}
		return new PolicyIssuer(
				createDOMNode(issuer.getContent()), attributes);
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
		Collection<AttributeExp> values = new LinkedList<AttributeExp>();
		for(AttributeValueType v : a.getAttributeValue()){
			values.add(create(v));
		}
		return new Attribute(a.getAttributeId(), 
				a.getIssuer(), a.isIncludeInResult(), values);
	}
	
	private AttributeExp create(
			AttributeValueType value) 
		throws XacmlSyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new XacmlSyntaxException(
					"Attribute does not have content");
		}
		return createAttributeValue(
				value.getDataType(), 
				content.iterator().next(), 
				value.getOtherAttributes());
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
	 * @throws XacmlSyntaxException
	 *             if {@link ObligationExpression} can not be created from a
	 *             given JAXB object due syntactical error
	 */
	private ObligationExpression create(ObligationExpressionType exp, 
			VariableManager<JAXBElement<?>> m)
			throws XacmlSyntaxException {
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
	 * @throws XacmlSyntaxException
	 *             if {@link Target} can not be created from a given JAXB object
	 *             due syntactical error
	 */
	private Target create(TargetType target) throws XacmlSyntaxException {
		if (target == null) {
			return null;
		}
		Collection<MatchAnyOf> m = new LinkedList<MatchAnyOf>();
		for (AnyOfType anyOf : target.getAnyOf()) {
			m.add(create(anyOf));
		}
		return new Target(m);
	}

	private MatchAnyOf create(AnyOfType anyOf) throws XacmlSyntaxException {
		Preconditions.checkArgument(anyOf != null);
		Collection<MatchAllOf> m = new LinkedList<MatchAllOf>();
		for (AllOfType allOf : anyOf.getAllOf()) {
			m.add(create(allOf));
		}
		return new MatchAnyOf(m);
	}

	private MatchAllOf create(AllOfType allOf) throws XacmlSyntaxException {
		Preconditions.checkArgument(allOf != null);
		List<Match> m = new LinkedList<Match>();
		for (MatchType match : allOf.getMatch()) {
			m.add(createMatch(match));
		}
		return new MatchAllOf(m);
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
					"Match=\"%s\" attribute value must be specified");
		}
		return new Match(createFunction(m.getMatchId()), createValue(v.getDataType(),
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
	 * @throws XacmlSyntaxException
	 *             if given
	 * @exception IllegalArgumentException
	 *                if given argument is <code>null</code>
	 */
	private AttributeReference createAttributeReference(Object ref)
			throws XacmlSyntaxException {
		Preconditions.checkArgument(ref != null);
		if (ref instanceof AttributeSelectorType) {
			AttributeSelectorType selector = (AttributeSelectorType)ref;
			if(log.isDebugEnabled()){
				log.debug(selector.getPath());
			}
			return AttributeSelector.create(
					selector.getCategory(),
					selector.getPath(), 
					selector.getContextSelectorId(),
					selector.getDataType(), 
					selector.isMustBePresent());
		}
		if (ref instanceof AttributeDesignatorType) {
			AttributeDesignatorType desig = (AttributeDesignatorType) ref;
			return AttributeDesignator.create(
					desig.getCategory(), 
					desig.getAttributeId(), 
					desig.getIssuer(),
					desig.getDataType(), 
					desig.isMustBePresent());
		}
		throw new XacmlSyntaxException(
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
	 * @throws XacmlSyntaxException
	 */
	private AttributeExp createValue(String dataType, List<Object> content, 
			Map<QName, String> otherAttributes)
			throws XacmlSyntaxException {
		if (content == null || content.isEmpty()) {
			throw new XacmlSyntaxException("Attribute does not have content");
		}
		return DataTypes.createAttributeValue(dataType, 
				content.iterator().next(), otherAttributes);
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
		return new AttributeAssignmentExpression(
				exp.getAttributeId(),
				parseExpression(exp.getExpression(), m), 
				AttributeCategories.parse(exp.getCategory()),
				exp.getIssuer());
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
		return new Apply(createFunction(apply.getFunctionId()), arguments);
	}

	/**
	 * Creates instance of {@link Expression} from a given JAXB object
	 * 
	 * @param expression
	 * @return
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
			return createValue(t.getDataType(), t.getContent(), 
					t.getOtherAttributes());
		}
		if (e instanceof VariableReferenceType) {
			VariableReferenceType varRef = (VariableReferenceType)e;
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
						"defintion variableId=\"{}\", expression=\"{}\"", varId, expression);
			}
			m.resolveVariableDefinition(new VariableDefinition(varId, expression));
		}
	}
}
