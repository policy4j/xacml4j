package com.artagon.xacml.spring;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.PolicyStore;
import com.artagon.xacml.v30.Xacml30PolicyUnmarshaller;
import com.google.common.base.Preconditions;

public class PolicyStoreFactoryBean extends AbstractFactoryBean
{
	private PolicyStore policyStore;
	private Collection<Resource> policySetResources;
	private Collection<Resource> referencedPolicySetResources;
	
	private FunctionProvider extensionFunctions;
	private DecisionCombiningAlgorithmProvider combiningAlgorithmProvider;
	
	public PolicyStoreFactoryBean() 
		throws Exception
	{
		this.policySetResources = new LinkedList<Resource>();
		this.referencedPolicySetResources = new LinkedList<Resource>();
	}
	
	public void setPolicies(Collection<Resource> resources){
		this.policySetResources = resources;
	}
	
	public void setReferencedPolicies(Collection<Resource> resources){
		this.referencedPolicySetResources = resources;
	}
	
	@Override
	protected PolicyStore createInstance() throws Exception 
	{
		PolicyUnmarshaller unmarshaller = new Xacml30PolicyUnmarshaller(extensionFunctions, 
				combiningAlgorithmProvider);
		Preconditions.checkState(policySetResources != null);
		for(Resource r : policySetResources){
			policyStore.add(unmarshaller.unmarshal(r.getInputStream()));
		}
		for(Resource r : referencedPolicySetResources){
			policyStore.add(unmarshaller.unmarshal(r.getInputStream()), false);
		}
		return policyStore;
	}

	@Override
	public Class<PolicyStore> getObjectType() {
		return PolicyStore.class;
	}
}

