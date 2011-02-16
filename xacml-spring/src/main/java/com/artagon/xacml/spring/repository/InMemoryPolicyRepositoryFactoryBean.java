package com.artagon.xacml.spring.repository;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.spring.ResourceCollection;
import com.artagon.xacml.v30.policy.combine.DefaultXacml30DecisionCombiningAlgorithms;
import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v30.spi.function.FunctionProvider;
import com.artagon.xacml.v30.spi.function.FunctionProviderBuilder;
import com.artagon.xacml.v30.spi.repository.InMemoryPolicyRepository;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.google.common.base.Preconditions;

public class InMemoryPolicyRepositoryFactoryBean extends AbstractFactoryBean<PolicyRepository>
{
	private ResourceCollection resources;
	private FunctionProviderBuilder functionProviderBuilder;
	private DecisionCombiningAlgorithmProvider extensionDecisionCombiningAlgorithms;
	
	public InMemoryPolicyRepositoryFactoryBean() throws Exception
	{
		this.extensionDecisionCombiningAlgorithms = new DefaultXacml30DecisionCombiningAlgorithms();
		this.functionProviderBuilder = FunctionProviderBuilder.builder().withDefaultFunctions();
	}
	
	@Override
	public Class<PolicyRepository> getObjectType() {
		return PolicyRepository.class;
	}
	
	public void setExtensionFunctions(FunctionProvider functions){
		this.functionProviderBuilder.withFunctions(functions);
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
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository(
				functionProviderBuilder.build(), 
				extensionDecisionCombiningAlgorithms);
		for(Resource r : resources.getResources()){
			repository.importPolicy(r.getInputStream());
		}
		return repository;
	}
}
