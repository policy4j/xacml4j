package org.xacml4j.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.pdp.PolicySetIDReference;


public class PolicySetIDReferenceFactoryBean extends AbstractFactoryBean<PolicySetIDReference>
{
	private PolicySetIDReference.Builder ref = PolicySetIDReference.builder();
	
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
	public Class<PolicySetIDReference> getObjectType() {
		return PolicySetIDReference.class;
	}
	
	@Override
	protected PolicySetIDReference createInstance() throws Exception 
	{
		return ref.build();
	}
	
	
}
