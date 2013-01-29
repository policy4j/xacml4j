package org.xacml4j.spring.pip;

import com.google.common.base.Preconditions;

class ResolverRegistration 
{
	private String policyId;
	private Object resolver;
	
	public ResolverRegistration(
			String policyId, 
			Object resolver){
		Preconditions.checkNotNull(resolver);
		this.policyId = policyId;
		this.resolver = resolver;
	}
	
	/**
	 * Gets policy identifier
	 * 
	 * @return
	 */
	public String getPolicyId(){
		return policyId;
	}
	
	/**
	 * Gets resolvers
	 * 
	 * @return
	 */
	public Object getResolver(){
		return resolver;
	}
}
