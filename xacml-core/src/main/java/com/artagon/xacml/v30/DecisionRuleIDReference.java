package com.artagon.xacml.v30;

/**
 * An interface for a references to a decision rule
 * 
 * @author Giedrius Trumpickas
 */
public interface DecisionRuleIDReference extends DecisionRule
{
	VersionMatch getVersion();	
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();
}
