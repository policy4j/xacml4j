package com.artagon.xacml.v3;

public interface CompositeDecisionRuleIDReference extends PolicyElement
{
	String getId();
	VersionMatch getVersion();	
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();
}
