package com.artagon.xacml.v20.endpoints;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.artagon.xacml.v20.Xacml20PolicyUnmarshaller;
import com.artagon.xacml.v3.XacmlFactory;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.PolicyStore;
import com.artagon.xacml.v3.spi.store.DefaultPolicyStore;
import com.google.common.base.Preconditions;

public class InMemoryPolicyStoreFactoryBean extends AbstractFactoryBean
{
	private DefaultPolicyStore policyStore;
	private Collection<Resource> policySetResources;
	private Collection<Resource> referencedPolicySetResources;
	private PolicyUnmarshaller policyMapper;
	
	public InMemoryPolicyStoreFactoryBean(XacmlFactory factory) 
		throws JAXBException
	{
		Preconditions.checkNotNull(factory);
		this.policyStore = new DefaultPolicyStore();
		this.policyMapper = new Xacml20PolicyUnmarshaller(factory);
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
	protected Object createInstance() throws Exception 
	{
		Preconditions.checkState(policySetResources != null);
		for(Resource r : policySetResources){
			policyStore.addPolicy(policyMapper.unmarshall(r.getInputStream()));
		}
		for(Resource r : referencedPolicySetResources){
			policyStore.addReferencedPolicy(policyMapper.unmarshall(r.getInputStream()));
		}
		return policyStore;
	}

	@Override
	public Class<PolicyStore> getObjectType() {
		return PolicyStore.class;
	}
	
}
