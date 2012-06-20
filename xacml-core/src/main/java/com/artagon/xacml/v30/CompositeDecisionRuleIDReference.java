package com.artagon.xacml.v30;

import com.artagon.xacml.v30.pdp.VersionMatch;


public interface CompositeDecisionRuleIDReference 
	extends CompositeDecisionRule
{
	VersionMatch getVersion();	
	VersionMatch getEarliestVersion();
	VersionMatch getLatestVersion();
}
