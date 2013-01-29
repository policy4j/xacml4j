package org.xacml4j.spring.repository;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

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
		FunctionProviderBuilder functionProviderBuilder = FunctionProviderBuilder.builder()
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
				id, functionProviderBuilder.create(), decisionAlgorithmProviderBuilder.create());
		for(Resource r : resources){
			repository.importPolicy(r.getInputStream());
		}
		return repository;
	}
}
