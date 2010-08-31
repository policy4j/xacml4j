package com.artagon.xacml.spring.pip;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.pip.AttributeResolver;
import com.artagon.xacml.v3.spi.pip.DefaultPolicyInformationPoint;

public class PolicyInformationPointFactoryBean extends AbstractFactoryBean
{
	private Collection<AttributeResolver> resolvers = Collections.emptyList();
	
	@Override
	public Class<PolicyInformationPoint> getObjectType() {
		return PolicyInformationPoint.class;
	}
	
	public void setAttributeResolvers(Collection<AttributeResolver> resolvers){
		this.resolvers = resolvers;
	}

	@Override
	protected Object createInstance() throws Exception 
	{
		DefaultPolicyInformationPoint pip = new DefaultPolicyInformationPoint();
		for(AttributeResolver r : resolvers){
			pip.addResolver(r);
		}
		return pip;
	}
}
