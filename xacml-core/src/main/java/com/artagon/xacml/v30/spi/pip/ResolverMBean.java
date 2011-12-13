package com.artagon.xacml.v30.spi.pip;

public interface ResolverMBean 
{
	/**
	 * Gets resolver identifier
	 * 
	 * @return resolver identifier
	 */
	String getId();
	
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
	 * Gets succesful invocation count
	 * 
	 * @return succesfull invocation count
	 */
	long getSuccessCount();
	
	/**
	 * Gets last invocation duration
	 * in milliseconds
	 * 
	 * @return last invocation duration
	 */
	long getSuccessInvocationTimeCMA();
	
	/**
	 * Gets preferred attributes 
	 * cache duration in seconds
	 * 
	 * @return cache duration in
	 * seconds
	 */
	int getPreferredCacheTTL();
	
	/**
	 * Sets preferred attributes 
	 * cache duration in seconds
	 * 
	 * @param ttl a preferred cache duration in
	 * seconds
	 */
	void setPreferredCacheTTL(int ttl);
}
