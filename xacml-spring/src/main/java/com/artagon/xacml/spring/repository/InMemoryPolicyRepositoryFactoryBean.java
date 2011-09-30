package com.artagon.xacml.spring.repository;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import com.artagon.xacml.v30.spi.function.FunctionProvider;
import com.artagon.xacml.v30.spi.function.FunctionProviderBuilder;
import com.artagon.xacml.v30.spi.repository.InMemoryPolicyRepository;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.google.common.base.Preconditions;

public class InMemoryPolicyRepositoryFactoryBean extends AbstractFactoryBean<PolicyRepository>
{
	private String id;
	private Resource[] resources;
	private FunctionProvider extensionFuctions;
	private DecisionCombiningAlgorithmProvider extensionDecisionCombiningAlgorithms;
	
	public InMemoryPolicyRepositoryFactoryBean(String id){
		Preconditions.checkNotNull(id);
		this.id = id;
	}
	
	@Override
	public Class<PolicyRepository> getObjectType() {
		return PolicyRepository.class;
	}
	
	public void setExtensionFunctions(FunctionProvider functions){
		this.extensionFuctions = functions;
	}
	
	public void setExtensionCombiningAlgorithms(
			DecisionCombiningAlgorithmProvider algorithms){
		this.extensionDecisionCombiningAlgorithms = algorithms;
	}
	
	public void setPolicies(Resource[] policies){
		this.resources = policies;
	}
	
	@Override
	protected PolicyRepository createInstance() throws Exception 
	{
		FunctionProviderBuilder functionProviderBuilder = 
			FunctionProviderBuilder.builder()
			.withDefaultFunctions();
		if(extensionFuctions != null){
			functionProviderBuilder.withFunctions(extensionFuctions);
		}
		DecisionCombiningAlgorithmProviderBuilder decisionAlgorithmProviderBuilder = 
			DecisionCombiningAlgorithmProviderBuilder.builder()
			.withDefaultAlgorithms();
		if(extensionDecisionCombiningAlgorithms != null){
			decisionAlgorithmProviderBuilder.withAlgorithmProvider(extensionDecisionCombiningAlgorithms);
		}
		Preconditions.checkState(resources != null, "Policy resources must be specified");
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository(
				id, functionProviderBuilder.build(), decisionAlgorithmProviderBuilder.build());
		for(Resource r : resources){
			repository.importPolicy(r.getInputStream());
		}
		return repository;
	}
}
