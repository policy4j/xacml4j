package org.xacml4j.v30;



public interface CompositeDecisionRuleIDReference
	extends CompositeDecisionRule
{
	VersionMatch getVersion();
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();

	boolean isReferenceTo(CompositeDecisionRule r);
}
