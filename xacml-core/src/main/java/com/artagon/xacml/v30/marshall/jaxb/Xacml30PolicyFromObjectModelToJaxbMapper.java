package com.artagon.xacml.v30.marshall.jaxb;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.AdviceExpressionsType;
import org.oasis.xacml.v30.jaxb.AllOfType;
import org.oasis.xacml.v30.jaxb.AnyOfType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.ExpressionType;
import org.oasis.xacml.v30.jaxb.MatchType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.ObligationExpressionsType;
import org.oasis.xacml.v30.jaxb.PolicySetType;
import org.oasis.xacml.v30.jaxb.PolicyType;
import org.oasis.xacml.v30.jaxb.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.AdviceExpression;
import com.artagon.xacml.v30.Apply;
import com.artagon.xacml.v30.AttributeAssignmentExpression;
import com.artagon.xacml.v30.AttributeDesignator;
import com.artagon.xacml.v30.AttributeSelector;
import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.CombinerParameter;
import com.artagon.xacml.v30.CombinerParameters;
import com.artagon.xacml.v30.Condition;
import com.artagon.xacml.v30.FunctionReference;
import com.artagon.xacml.v30.Match;
import com.artagon.xacml.v30.MatchAllOf;
import com.artagon.xacml.v30.MatchAnyOf;
import com.artagon.xacml.v30.ObligationExpression;
import com.artagon.xacml.v30.Policy;
import com.artagon.xacml.v30.PolicyCombinerParameters;
import com.artagon.xacml.v30.PolicyDefaults;
import com.artagon.xacml.v30.PolicyIDReference;
import com.artagon.xacml.v30.PolicySet;
import com.artagon.xacml.v30.PolicySetCombinerParameters;
import com.artagon.xacml.v30.PolicySetDefaults;
import com.artagon.xacml.v30.PolicySetIDReference;
import com.artagon.xacml.v30.PolicyVisitorSupport;
import com.artagon.xacml.v30.Rule;
import com.artagon.xacml.v30.RuleCombinerParameters;
import com.artagon.xacml.v30.Target;
import com.artagon.xacml.v30.VariableDefinition;
import com.artagon.xacml.v30.VariableReference;
import com.google.common.base.Preconditions;

class Xacml30PolicyFromObjectModelToJaxbMapper extends PolicyVisitorSupport 
{
	private final static Logger log = LoggerFactory.getLogger(Xacml30PolicyFromObjectModelToJaxbMapper.class);
	
	private Stack<PolicyType> policies;
	private Stack<PolicySetType> policySets;

	private Collection<JAXBElement<?>> policySetOrPoliciesOrCombinerParams;
	private Collection<Object> policyRulesOrVarDefinitionsOrCombinerParams;
	private AdviceExpressionsType adviceExpressions;
	private ObligationExpressionsType obligationExpressions;

	// target state
	private TargetType target;
	private List<MatchType> match;
	private List<AnyOfType> anyOf;
	private List<AllOfType> allOf;
	private ExpressionType expressionType;
	
	private Collection<VariableDefinition> variableDefinitions;
	private ObjectFactory jaxbFactory;

	public Xacml30PolicyFromObjectModelToJaxbMapper(ObjectFactory jaxbFactory) 
	{
		Preconditions.checkNotNull(jaxbFactory);
		this.jaxbFactory = jaxbFactory;
		this.policies = new Stack<PolicyType>();
		this.policySets = new Stack<PolicySetType>();
		this.policySetOrPoliciesOrCombinerParams = new LinkedList<JAXBElement<?>>();
		this.policyRulesOrVarDefinitionsOrCombinerParams = new LinkedList<Object>();
		this.anyOf = new LinkedList<AnyOfType>();
		this.allOf = new LinkedList<AllOfType>();
		this.variableDefinitions = new LinkedList<VariableDefinition>();
	}

	@Override
	public void visitEnter(AttributeValue attr) {
	}

	@Override
	public void visitLeave(AttributeValue attr) {
	}

	@Override
	public void visitEnter(AttributeDesignator designator) {
	}

	@Override
	public void visitLeave(AttributeDesignator designator) {

	}

	@Override
	public void visitEnter(AttributeSelector selector) {

	}

	@Override
	public void visitLeave(AttributeSelector selector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(FunctionReference function) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(FunctionReference function) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(BagOfAttributeValues bag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(BagOfAttributeValues bag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(VariableReference var) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(VariableReference var) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(VariableDefinition var) {

	}



	@Override
	public void visitEnter(Apply apply) {

	}

	@Override
	public void visitLeave(Apply apply) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(Condition condition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(Condition condition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(Target target) {
		this.target = new TargetType();
	}

	@Override
	public void visitLeave(Target target) {
		this.target.getAnyOf().addAll(anyOf);
		this.anyOf.clear();
	}

	@Override
	public void visitEnter(Match m) {
		MatchType jm = jaxbFactory.createMatchType();
		jm.setMatchId(m.getMatchId());
		AttributeValueType jav = new AttributeValueType();
		jav.setDataType(m.getAttributeValue().getType().getDataTypeId());
		jav.setDataType(m.getAttributeValue().toXacmlString());
		jm.setAttributeValue(jav);
		this.match.add(jm);
	}

	@Override
	public void visitEnter(MatchAllOf match) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(MatchAllOf match) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(MatchAnyOf match) {
	}

	@Override
	public void visitLeave(MatchAnyOf match) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(Rule rule) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(Rule rule) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(PolicyIDReference ref) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(PolicyIDReference ref) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(Policy p) 
	{
		if(log.isDebugEnabled()){
			log.debug("Mapping policy id=\"{}\" " +
					"version=\"{}\"", p.getId(), p.getVersion());
		}
		PolicyType jp = jaxbFactory.createPolicyType();
		jp.setPolicyId(p.getId());
		jp.setDescription(p.getDescription());
		jp.setVersion(p.getVersion().getValue());
		jp.setRuleCombiningAlgId(p.getRuleCombiningAlgorithm().getId());
		policies.push(jp);
	}

	@Override
	public void visitLeave(Policy p) 
	{
		PolicyType ps = policies.pop();
		ps.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition()
				.addAll(policyRulesOrVarDefinitionsOrCombinerParams);
		ps.setAdviceExpressions(adviceExpressions);
		ps.setObligationExpressions(obligationExpressions);

		// clear the state
		this.adviceExpressions = null;
		this.obligationExpressions = null;
		this.policyRulesOrVarDefinitionsOrCombinerParams.clear();
		this.policySetOrPoliciesOrCombinerParams.add(jaxbFactory.createPolicy(ps));
		if(log.isDebugEnabled()){
			log.debug("Done mapping policy id=\"{}\" " +
					"version=\"{}\"", p.getId(), p.getVersion());
		}
	}

	@Override
	public void visitEnter(PolicySet p) {
		PolicySetType jps = jaxbFactory.createPolicySetType();
		jps.setPolicySetId(p.getId());
		jps.setDescription(p.getDescription());
		jps.setVersion(p.getVersion().getValue());
		jps.setPolicyCombiningAlgId(p.getPolicyDecisionCombiningAlgorithm()
				.getId());
		policySets.push(jps);
	}

	@Override
	public void visitLeave(PolicySet policySet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(PolicySetIDReference ref) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(PolicySetIDReference ref) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(ObligationExpression obligation) {
		
	}

	@Override
	public void visitEnter(AdviceExpression advice) {
	}

	@Override
	public void visitEnter(AttributeAssignmentExpression attribute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(AttributeAssignmentExpression attribute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(PolicyDefaults policyDefaults) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(PolicyDefaults policyDefaults) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(PolicySetDefaults policySetDefaults) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(PolicySetDefaults policySetDefaults) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(CombinerParameter p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(CombinerParameter p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(CombinerParameters p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(CombinerParameters p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(RuleCombinerParameters p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(RuleCombinerParameters p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(PolicyCombinerParameters p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(PolicyCombinerParameters p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnter(PolicySetCombinerParameters p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLeave(PolicySetCombinerParameters p) {
	}
}
