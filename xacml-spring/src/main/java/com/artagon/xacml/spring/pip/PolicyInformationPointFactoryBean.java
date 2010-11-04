package com.artagon.xacml.spring.pip;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.pip.AttributeResolver;
import com.artagon.xacml.v3.spi.pip.DefaultPolicyInformationPoint;
import com.artagon.xacml.v3.spi.pip.DefaultResolverRegistry;
import com.artagon.xacml.v3.spi.pip.ResolverRegistry;

public class PolicyInformationPointFactoryBean extends AbstractFactoryBean<PolicyInformationPoint>
{
	private ResolverRegistry registry;
	
	public PolicyInformationPointFactoryBean(){
		this.registry = new DefaultResolverRegistry();
	}
	
	@Override
	public Class<PolicyInformationPoint> getObjectType() {
		return PolicyInformationPoint.class;
	}
	
	public void setAttributeResolvers(Collection<AttributeResolver> resolvers){
		for(AttributeResolver r : resolvers){
			registry.addResolver(r);
		}
	}

	@Override
	protected PolicyInformationPoint createInstance() throws Exception 
	{
		return new DefaultPolicyInformationPoint(registry);
	}
}
