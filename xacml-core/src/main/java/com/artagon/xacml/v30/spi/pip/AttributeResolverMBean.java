package com.artagon.xacml.v30.spi.pip;

public interface AttributeResolverMBean 
{
	/**
	 * Gets resolver invocations
	 * count
	 * 
	 * @return resolver invocations count
	 */
	long getInvocationCount();
	
	/**
	 * Gets resolver failures count
	 * 
	 * @return resolver failures count
	 */
	long getFailuresCount();
	
	/**
	 * Gets last invocation duration
	 * in milliseconds
	 * 
	 * @return last invocation duration
	 */
	long getLastInvocationDuration();
	
	/**
	 * Gets preferred attributes 
	 * cache duration in seconds
	 * 
	 * @return cache duration in
	 * seconds
	 */
	long getPreferredCacheTTL();
	
	/**
	 * Sets preferred attributes 
	 * cache duration in seconds
	 * 
	 * @param ttl a preferred cache duration in
	 * seconds
	 */
	void setPreferredCacheTTL(long ttl);
}
