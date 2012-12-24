package org.xacml4j.v30.pdp;

public interface PolicyDecisionPointMBean 
{
	boolean isDecisionAuditEnabled();
	void setDecisionAuditEnabled(boolean enabled);
	boolean isDecisionCacheEnabled();
	void setDecisionCacheEnabled(boolean enabled);
}
