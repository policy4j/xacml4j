package com.artagon.xacml.v20.endpoints;

import org.springframework.beans.factory.FactoryBean;

import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;

public class PolicyDecisionPointFactoryBean implements FactoryBean
{

	@Override
	public Object getObject() throws Exception 
	{
		return null;
	}
	
	@Override
	public Class<?> getObjectType() {
		return PolicyDecisionPoint.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
}
