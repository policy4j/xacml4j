package com.artagon.xacml.spring;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.InMemoryPolicyRepository;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v30.Xacml30PolicyUnmarshaller;

public class PolicyRepositoryFactoryBean extends AbstractFactoryBean
{
	private Collection<Resource> resources;
	private FunctionProvider extensionFunctions;
	private DecisionCombiningAlgorithmProvider extensionDecisionCombiningAlgorithms;
	
	@Override
	public Class<PolicyRepository> getObjectType() {
		return PolicyRepository.class;
	}
	
	public void setPolicies(Collection<Resource> policies){
		this.resources = policies;
	}
	
	@Override
	protected Object createInstance() throws Exception 
	{
		PolicyUnmarshaller unmarshaler = new Xacml30PolicyUnmarshaller(extensionFunctions, 
				extensionDecisionCombiningAlgorithms);
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository();
		for(Resource r : resources){
			repository.add(unmarshaler.unmarshal(r.getInputStream()));
		}
		return repository;
	}
}
