package com.artagon.xacml.v3.spi;

import java.util.Collection;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;

public interface PolicyRepository 
{
	Policy findPolicy(String id, Version version);
	Collection<Policy> findPolicy(String id, VersionMatch earlierst, VersionMatch latest);
}
