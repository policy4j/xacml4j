package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pip.DefaultPolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.ResolverRegistry;
import com.google.common.base.Preconditions;

public class PolicyInformationPointFactoryBean extends AbstractFactoryBean<PolicyInformationPoint>
{
	private ResolverRegistry registry;
	
	public PolicyInformationPointFactoryBean(ResolverRegistry registry){
		Preconditions.checkNotNull(registry);
		this.registry = registry;
	}
	
	@Override
	public Class<PolicyInformationPoint> getObjectType() {
		return PolicyInformationPoint.class;
	}

	@Override
	protected PolicyInformationPoint createInstance() throws Exception 
	{
		return new DefaultPolicyInformationPoint(registry);
	}
}
