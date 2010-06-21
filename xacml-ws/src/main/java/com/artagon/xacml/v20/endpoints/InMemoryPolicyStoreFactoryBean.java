package com.artagon.xacml.v20.endpoints;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.v20.Xacml20PolicyMapper;
import com.artagon.xacml.v3.DefaultPolicyFactory;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.repostory.InMemoryPolicyStore;
import com.google.common.base.Preconditions;

public class InMemoryPolicyStoreFactoryBean extends AbstractFactoryBean
{
	private InMemoryPolicyStore policyStore;
	private Resource policySetResource;
	private JAXBContext context;
	private Xacml20PolicyMapper policyMapper;
	
	public InMemoryPolicyStoreFactoryBean() 
		throws JAXBException
	{
		this.policyStore = new InMemoryPolicyStore();
		this.context = JAXBContext.newInstance("org.oasis.xacml.v20.policy");
		this.policyMapper = new Xacml20PolicyMapper(new DefaultPolicyFactory());
	}
	
	public void setPolicySet(Resource resource)
	{
		this.policySetResource = resource;
	}
	
	@Override
	protected Object createInstance() throws Exception 
	{
		Preconditions.checkState(policySetResource != null, "PolicySet is not specified");
		JAXBElement<Object> policy = (JAXBElement<Object>)context.createUnmarshaller().unmarshal(policySetResource.getInputStream());
		policyStore.add(policyMapper.create(policy.getValue()));
		return policyStore;
	}

	@Override
	public Class<PolicyRepository> getObjectType() {
		return PolicyRepository.class;
	}
	
}
