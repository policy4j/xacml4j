package com.artagon.xacml.spring;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.v20.Xacml20PolicyUnmarshaller;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.PolicyStore;
import com.artagon.xacml.v3.spi.store.DefaultPolicyStore;
import com.google.common.base.Preconditions;

public class PolicyStoreFactoryBean extends AbstractFactoryBean
{
	private DefaultPolicyStore policyStore;
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
		PolicyUnmarshaller unmarshaller = new Xacml20PolicyUnmarshaller();
		Preconditions.checkState(policySetResources != null);
		for(Resource r : policySetResources){
			policyStore.addPolicy(unmarshaller.unmarshall(r.getInputStream()));
		}
		for(Resource r : referencedPolicySetResources){
			policyStore.addReferencedPolicy(unmarshaller.unmarshall(r.getInputStream()));
		}
		return policyStore;
	}

	@Override
	public Class<PolicyStore> getObjectType() {
		return PolicyStore.class;
	}
}

