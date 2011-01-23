package com.artagon.xacml.v30;


public interface CompositeDecisionRuleIDReference 
	extends CompositeDecisionRule
{
	VersionMatch getVersionMatch();	
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();
}
