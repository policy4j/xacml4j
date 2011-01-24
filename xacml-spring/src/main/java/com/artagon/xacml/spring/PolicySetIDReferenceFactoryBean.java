package com.artagon.xacml.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.PolicySetIDReference;

public class PolicySetIDReferenceFactoryBean extends AbstractFactoryBean<PolicySetIDReference>
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
	public Class<PolicySetIDReference> getObjectType() {
		return PolicySetIDReference.class;
	}
	
	@Override
	protected PolicySetIDReference createInstance() throws Exception 
	{
		return PolicySetIDReference.create(id, version, earliest, latest);
	}
	
	
}
