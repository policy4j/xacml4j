package com.artagon.xacml.v30;


public interface CompositeDecisionRuleIDReference 
	extends CompositeDecisionRule
{
	VersionMatch getVersion();	
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();
}
