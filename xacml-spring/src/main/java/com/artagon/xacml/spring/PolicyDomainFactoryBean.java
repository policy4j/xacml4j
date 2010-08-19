package com.artagon.xacml.spring;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v3.spi.DefaultPolicyDomain;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.google.common.base.Preconditions;

public class PolicyDomainFactoryBean extends AbstractFactoryBean
{
	private String name;
	private Collection<CompositeDecisionRuleIDReference> policyReferences;
	
	public PolicyDomainFactoryBean(String name) 
		throws Exception
	{
		this.name = name;
	}
	
	public void setPolicyReferences(Collection<CompositeDecisionRuleIDReference> policyReferences){
		this.policyReferences = policyReferences;
	}
	
	@Override
	protected PolicyDomain createInstance() throws Exception 
	{
		Preconditions.checkArgument(policyReferences != null, 
				"At least on policy reference must be specified");
		Preconditions.checkArgument(policyReferences.size() > 0, 
				"At least on policy reference must be specified");
		PolicyDomain policyDomain = new DefaultPolicyDomain(name);
		for(CompositeDecisionRuleIDReference p : policyReferences){
			policyDomain.add(p);
		}
		return policyDomain;
	}

	@Override
	public Class<PolicyDomain> getObjectType() {
		return PolicyDomain.class;
	}
}

