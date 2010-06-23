package com.artagon.xacml.v20.endpoints;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.v20.Xacml20PolicyUnmarshaller;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.repository.InMemoryPolicyStore;
import com.google.common.base.Preconditions;

public class InMemoryPolicyStoreFactoryBean extends AbstractFactoryBean
{
	private InMemoryPolicyStore policyStore;
	private Collection<Resource> policySetResources;
	private PolicyUnmarshaller policyMapper;
	
	public InMemoryPolicyStoreFactoryBean(PolicyFactory factory) 
		throws JAXBException
	{
		Preconditions.checkNotNull(factory);
		this.policyStore = new InMemoryPolicyStore();
		this.policyMapper = new Xacml20PolicyUnmarshaller(factory);
		this.policySetResources = new LinkedList<Resource>();
	}
	
	public void setPolicies(Collection<Resource> resources){
		this.policySetResources = resources;
	}
	
	@Override
	protected Object createInstance() throws Exception 
	{
		Preconditions.checkState(policySetResources != null);
		for(Resource r : policySetResources){
			policyStore.addTopLevelPolicy(policyMapper.getPolicy(r.getInputStream()));
		}
		return policyStore;
	}

	@Override
	public Class<PolicyRepository> getObjectType() {
		return PolicyRepository.class;
	}
	
}
