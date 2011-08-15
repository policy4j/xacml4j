package com.artagon.xacml.v30.marshall.jaxb;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.AdviceExpressionsType;
import org.oasis.xacml.v30.jaxb.AnyOfType;
import org.oasis.xacml.v30.jaxb.CombinerParametersType;
import org.oasis.xacml.v30.jaxb.ConditionType;
import org.oasis.xacml.v30.jaxb.DefaultsType;
import org.oasis.xacml.v30.jaxb.EffectType;
import org.oasis.xacml.v30.jaxb.IdReferenceType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.ObligationExpressionsType;
import org.oasis.xacml.v30.jaxb.PolicyIssuerType;
import org.oasis.xacml.v30.jaxb.PolicySetType;
import org.oasis.xacml.v30.jaxb.PolicyType;
import org.oasis.xacml.v30.jaxb.RuleType;
import org.oasis.xacml.v30.jaxb.TargetType;
import org.oasis.xacml.v30.jaxb.VariableDefinitionType;

import com.artagon.xacml.v30.Condition;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.MatchAnyOf;
import com.artagon.xacml.v30.Policy;
import com.artagon.xacml.v30.PolicyIDReference;
import com.artagon.xacml.v30.PolicySetIDReference;
import com.artagon.xacml.v30.PolicyVisitorSupport;
import com.artagon.xacml.v30.Rule;
import com.artagon.xacml.v30.Target;
import com.artagon.xacml.v30.VariableDefinition;
import com.google.common.base.Preconditions;

public class Xacml30PolicyFromObjectModelToJaxbMapper extends PolicyVisitorSupport
{
	private ObjectFactory objectFactory;
	private Stack<JAXBElement<?>> policyNodes;
	private Stack<PolicyNodeState> policyNodeState;
	
	private final static Map<Effect, EffectType> nativeToJaxbEffectMappings = new HashMap<Effect, EffectType>();

	static {
		nativeToJaxbEffectMappings.put(Effect.DENY, EffectType.DENY);
		nativeToJaxbEffectMappings.put(Effect.PERMIT, EffectType.PERMIT);
	}

	
	public Xacml30PolicyFromObjectModelToJaxbMapper(){
		this.objectFactory = new ObjectFactory();
		this.policyNodes = new Stack<JAXBElement<?>>();
	}
	
	@Override
	public void visitEnter(Policy policy) 
	{
		PolicyType p = objectFactory.createPolicyType();
		JAXBElement<?> jaxb = objectFactory.createPolicy(p);
		p.setPolicyId(policy.getId());
		p.setMaxDelegationDepth(policy.getMaxDelegationDepth());
		p.setVersion(policy.getVersion().getValue());
		p.setDescription(policy.getDescription());
		p.setRuleCombiningAlgId(policy.getRuleCombiningAlgorithm().getId());
		policyNodes.push(jaxb);
		policyNodeState.push(new PolicyNodeState());
	}
	
	@Override
	public void visitLeave(Policy policy) 
	{
		JAXBElement<?> jaxb = policyNodes.peek();
		PolicyType p =  (PolicyType)jaxb.getValue(); 
		PolicyNodeState s = policyNodeState.pop();
		s.setStateTo(p);
	}
	
	@Override
	public void visitEnter(Rule policy) 
	{
		RuleType p = objectFactory.createRuleType();
		JAXBElement<?> jaxb = objectFactory.createRule(p);
		p.setRuleId(policy.getId());
		p.setDescription(policy.getDescription());
		EffectType effect = nativeToJaxbEffectMappings.get(policy.getEffect());
		Preconditions.checkState(effect != null);
		p.setEffect(effect);
		policyNodes.push(jaxb);
		policyNodeState.push(new PolicyNodeState());
	}
	
	@Override
	public void visitLeave(Rule rule)
	{
		JAXBElement<?> jaxb = policyNodes.peek();
		RuleType p =  (RuleType)jaxb.getValue(); 
		PolicyNodeState s = policyNodeState.pop();
		s.setStateTo(p);
	}
	

	
	
	@Override
	public void visitEnter(PolicyIDReference ref) {
		IdReferenceType jaxbRef = objectFactory.createIdReferenceType(); 
		if(ref.getEarliestVersion() != null){
			jaxbRef.setEarliestVersion(ref.getEarliestVersion().getPattern());
		}
		if(ref.getLatestVersion() != null){
			jaxbRef.setLatestVersion(ref.getLatestVersion().getPattern());
		}
		if(ref.getVersion() != null){
			jaxbRef.setVersion(ref.getVersion().getPattern());
		}
		jaxbRef.setValue(ref.getId());
		PolicyNodeState s = policyNodeState.peek();
		s.addPolicyIdRef(jaxbRef);
	}
	
	@Override
	public void visitEnter(PolicySetIDReference ref) {
		IdReferenceType jaxbRef = objectFactory.createIdReferenceType(); 
		if(ref.getEarliestVersion() != null){
			jaxbRef.setEarliestVersion(ref.getEarliestVersion().getPattern());
		}
		if(ref.getLatestVersion() != null){
			jaxbRef.setLatestVersion(ref.getLatestVersion().getPattern());
		}
		if(ref.getVersion() != null){
			jaxbRef.setVersion(ref.getVersion().getPattern());
		}
		jaxbRef.setValue(ref.getId());
		PolicyNodeState s = policyNodeState.peek();
		s.addPolicySetIdRef(jaxbRef);
	}
	
	private class PolicyNodeState
	{
		private TargetType target;
		private ConditionType condition;
		private AdviceExpressionsType adviceExp;
		private ObligationExpressionsType obligationExp;
		private DefaultsType defaultsType;
		private PolicyIssuerType policyIssuer;
		private Collection<JAXBElement<?>> combinerParams = new LinkedList<JAXBElement<?>>();
		private Collection<JAXBElement<RuleType>> rules = new LinkedList<JAXBElement<RuleType>>();
		private Collection<JAXBElement<VariableDefinitionType>> vars = new LinkedList<JAXBElement<VariableDefinitionType>>();
		private Collection<JAXBElement<?>> policies = new LinkedList<JAXBElement<?>>();
		
		public void setStateTo(PolicyType p)
		{
			p.setTarget(target);
			p.setPolicyIssuer(policyIssuer);
			p.setAdviceExpressions(adviceExp);
			p.setObligationExpressions(obligationExp);
			p.setPolicyDefaults(defaultsType);
			p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().addAll(combinerParams);
			p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().addAll(vars);
			p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().addAll(rules);
		}
		
		public void setStateTo(PolicySetType p)
		{
			p.setTarget(target);
			p.setPolicyIssuer(policyIssuer);
			p.setAdviceExpressions(adviceExp);
			p.setObligationExpressions(obligationExp);
			p.setPolicySetDefaults(defaultsType);
		}
		
		public void setStateTo(RuleType p)
		{
			p.setTarget(target);
			p.setCondition(condition);
			p.setAdviceExpressions(adviceExp);
			p.setObligationExpressions(obligationExp);
		}
		
		public void setTarget(TargetType target){
			this.target = target;
		}
		
		public void add(VariableDefinitionType var){
			this.vars.add(objectFactory.createVariableDefinition(var));
		}
		
		public void add(CombinerParametersType params){
			this.combinerParams.add(objectFactory.createCombinerParameters(params));
		}
		
		public void add(RuleType rule){
			this.rules.add(objectFactory.createRule(rule));
		}
		
		public void add(PolicySetType p){
			this.policies.add(objectFactory.createPolicySet(p));
		}
		
		public void add(PolicyType p){
			this.policies.add(objectFactory.createPolicy(p));
		}
		
		public void addPolicySetIdRef(IdReferenceType ref){
			this.policies.add(objectFactory.createPolicySetIdReference(ref));
		}
		
		public void addPolicyIdRef(IdReferenceType ref){
			this.policies.add(objectFactory.createPolicyIdReference(ref));
		}
		
		public void setCondition(ConditionType cond){
			this.condition = cond;
		}
	}
	
	public interface PolicyTreeNodeState
	{
		void setState(DecisionRule rule);
	}
}
