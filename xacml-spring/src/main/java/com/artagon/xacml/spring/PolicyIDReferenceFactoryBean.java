package com.artagon.xacml.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.policy.PolicyIDReference;

public class PolicyIDReferenceFactoryBean extends AbstractFactoryBean<PolicyIDReference>
{
	private String id;
	private String version;
	private String earliest;
	private String latest;
	
	public void setId(String id){
		this.id  = id;
	}
	
	public void setEarliest(String earliest){
		this.earliest = earliest;
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public void setLatest(String latest){
		this.latest = latest;
	}
	
	@Override
	public Class<PolicyIDReference> getObjectType() {
		return PolicyIDReference.class;
	}
	
	@Override
	protected PolicyIDReference createInstance() throws Exception 
	{
		return PolicyIDReference.create(id, version, earliest, latest);
	}
	
	
}

