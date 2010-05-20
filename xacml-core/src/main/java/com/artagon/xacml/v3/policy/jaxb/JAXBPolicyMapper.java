package com.artagon.xacml.v3.policy.jaxb;

import java.util.Collection;
import java.util.Collections;

import org.oasis.xacml.v20.policy.DefaultsType;
import org.oasis.xacml.v20.policy.ObligationsType;
import org.oasis.xacml.v20.policy.PolicySetType;
import org.oasis.xacml.v20.policy.PolicyType;
import org.oasis.xacml.v20.policy.TargetType;

import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyDefaults;
import com.artagon.xacml.v3.policy.PolicyFactory;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySyntaxException;
import com.artagon.xacml.v3.policy.Rule;
import com.artagon.xacml.v3.policy.Target;
import com.artagon.xacml.v3.policy.VariableDefinition;

public class JAXBPolicyMapper 
{

	private PolicyFactory factory;
	
	public JAXBPolicyMapper(PolicyFactory factory){
		this.factory = factory;
	}
	
	public Policy create(PolicyType p) throws PolicySyntaxException
	{
		return null;
	}
	
	public PolicySet create(PolicySetType xmlPolicy) throws PolicySyntaxException
	{
		return null;
	}
	
	private PolicyDefaults create(DefaultsType defaults)
	{
		return null;
	}
	
	private Target create(TargetType target)
	{
		target.getActions();
		return null;
	}
	
	private Collection<VariableDefinition> getVariables(PolicyType p)
	{
		return Collections.emptyList();
	}
	
	private Collection<Rule> getRules(PolicyType p)
	{
		return Collections.emptyList();
	}
	
	private Collection<Obligation> getObligations(ObligationsType obligations)
	{
		return Collections.emptyList();
	}
}
