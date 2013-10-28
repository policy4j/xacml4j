package org.xacml4j.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.pdp.PolicyIDReference;


public class PolicyIDReferenceFactoryBean extends 
	AbstractFactoryBean<PolicyIDReference>
{
	private PolicyIDReference.Builder ref = PolicyIDReference.builder();
	
	public void setId(String id){
		this.ref.id(id);
	}
	
	public void setEarliest(String earliest){
		this.ref.earliest(earliest);
	}
	
	public void setVersion(String version){
		this.ref.versionAsString(version);
	}
	
	public void setLatest(String latest){
		this.ref.latest(latest);
	}
	
	@Override
	public Class<PolicyIDReference> getObjectType() {
		return PolicyIDReference.class;
	}
	
	@Override
	protected PolicyIDReference createInstance() throws Exception 
	{
		return ref.build();
	}
}

