package com.artagon.xacml.v3.policy;

public interface CompositeDecisionRuleIDReference extends CompositeDecisionRule
{
	VersionMatch getVersion();
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();
}
