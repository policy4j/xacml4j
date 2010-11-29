package com.artagon.xacml.spring.repository;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.spring.ResourceCollection;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.repository.InMemoryPolicyRepository;
import com.artagon.xacml.v30.Xacml30PolicyUnmarshaller;
import com.google.common.base.Preconditions;

public class InMemoryPolicyRepositoryFactoryBean extends AbstractFactoryBean<PolicyRepository>
{
	private ResourceCollection resources;
	private FunctionProvider extensionFunctions;
	private DecisionCombiningAlgorithmProvider extensionDecisionCombiningAlgorithms;
	
	@Override
	public Class<PolicyRepository> getObjectType() {
		return PolicyRepository.class;
	}
	
	public void setExtensionFunctions(FunctionProvider functions){
		this.extensionFunctions = functions;
	}
	
	public void setExtensionDecisionCombiningAlgorithms(
			DecisionCombiningAlgorithmProvider algorithms){
		this.extensionDecisionCombiningAlgorithms = algorithms;
	}
	
	public void setPolicies(ResourceCollection resources){
		this.resources = resources;
	}
	
	@Override
	protected PolicyRepository createInstance() throws Exception 
	{
		Preconditions.checkState(resources != null, "Policy resources must be specified");
		PolicyUnmarshaller unmarshaler = new Xacml30PolicyUnmarshaller(extensionFunctions, 
				extensionDecisionCombiningAlgorithms);
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository();
		for(Resource r : resources.getResources()){
			repository.add(unmarshaler.unmarshal(r.getInputStream()));
		}
		return repository;
	}
}
