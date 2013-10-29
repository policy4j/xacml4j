package org.xacml4j.v30.spi.pip;

public interface ResolverMBean
{
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
