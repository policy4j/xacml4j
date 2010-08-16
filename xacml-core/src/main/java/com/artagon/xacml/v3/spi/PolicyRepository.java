package com.artagon.xacml.v3.spi;


import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.VersionMatch;

public interface PolicyRepository 
{

	Iterable<Policy> getPolicies(String id, VersionMatch version);
	
	Iterable<PolicySet> getPolicySets(String id, VersionMatch version);
	
	Iterable<Policy> getPolicies(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
	
	Iterable<PolicySet> getPolicySets(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
}
