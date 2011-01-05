package com.artagon.xacml.v3;


public interface CompositeDecisionRuleIDReference 
	extends CompositeDecisionRule
{
	VersionMatch getVersionMatch();	
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();
}
