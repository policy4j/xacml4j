package org.xacml4j.v30.spi.pdp;

public interface PolicyDecisionCacheMBean
{
	long getCacheHitCount();
	long getCacheMissCount();
	void resetCount();
}
