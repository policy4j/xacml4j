package com.artagon.xacml.v3.policy;


public interface PolicyResolver 
{
	Policy resolvePolicy(String id,
			VersionMatch version, VersionMatch earliest, VersionMatch latest);
	PolicySet resolvePolicySet(String id, 
			VersionMatch version, VersionMatch earliest, VersionMatch latest);
}
