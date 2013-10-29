package org.xacml4j.v30;


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
