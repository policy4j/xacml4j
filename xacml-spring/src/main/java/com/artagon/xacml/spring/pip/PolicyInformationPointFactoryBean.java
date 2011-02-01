package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pip.DefaultPolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.DefaultResolverRegistry;
import com.artagon.xacml.v30.spi.pip.NoCachePolicyInformationPointCacheProvider;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.ResolverRegistry;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPointCacheProvider;
import com.google.common.base.Preconditions;

public class PolicyInformationPointFactoryBean 
	extends AbstractFactoryBean<PolicyInformationPoint>
{ 
	private ResolverRegistry registry;
	private PolicyInformationPointCacheProvider cache;
	
	public PolicyInformationPointFactoryBean(){
		this.registry = new DefaultResolverRegistry();
		this.cache = new NoCachePolicyInformationPointCacheProvider();
	}
	public void setCache(PolicyInformationPointCacheProvider cache){
		Preconditions.checkNotNull(cache);
		this.cache = cache;
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
		return new DefaultPolicyInformationPoint(registry, cache);
	}
}
