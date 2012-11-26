package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPointBuilder;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPointCacheProvider;
import com.artagon.xacml.v30.spi.pip.ResolverRegistry;
import com.google.common.base.Preconditions;

public class PolicyInformationPointFactoryBean
	extends AbstractFactoryBean<PolicyInformationPoint>
{
	private ResolverRegistry registry;
	private PolicyInformationPointBuilder pipBuilder;

	public PolicyInformationPointFactoryBean(String id){
		this.pipBuilder = PolicyInformationPointBuilder.builder(id);
	}
	public void setCache(PolicyInformationPointCacheProvider cache){
		pipBuilder.withCacheProvider(cache);
	}

	public void setResolvers(ResolverRegistry registry){
		Preconditions.checkNotNull(registry);
		this.registry = registry;
	}

	@Override
	public Class<PolicyInformationPoint> getObjectType() {
		return PolicyInformationPoint.class;
	}

	@Override
	protected PolicyInformationPoint createInstance()
		throws Exception
	{
		Preconditions.checkState(registry != null);
		return pipBuilder.build(registry);
	}
}
