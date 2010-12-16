package com.artagon.xacml.v3;


public interface CompositeDecisionRuleIDReference 
	extends ReferencableDecisionRule
{
	VersionMatch getVersionMatch();	
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();
}
