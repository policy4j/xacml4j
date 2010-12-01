package com.artagon.xacml.spring.domain;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.combine.DefaultDecisionCombiningAlgorithms;
import com.artagon.xacml.v3.spi.DefaultPolicyDomain;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.PolicyDomain.Type;
import com.google.common.base.Preconditions;

public class PolicyDomainFactoryBean extends AbstractFactoryBean<PolicyDomain>
{
	private String name;
	private Collection<CompositeDecisionRule> policyReferences;
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setReferences(Collection<CompositeDecisionRule> policyReferences){
		this.policyReferences = policyReferences;
	}
	
	@Override
	protected PolicyDomain createInstance() throws Exception 
	{
		Preconditions.checkArgument(name != null, 
				"Policy domain name must be specified");
		Preconditions.checkArgument(policyReferences != null, 
				"At least on policy reference must be specified");
		Preconditions.checkArgument(policyReferences.size() > 0, 
				"At least on policy reference must be specified");
		return new DefaultPolicyDomain(name, Type.FIRST_APPLICABLE, new DefaultDecisionCombiningAlgorithms(), policyReferences);
	}

	@Override
	public Class<PolicyDomain> getObjectType() {
		return PolicyDomain.class;
	}
}

