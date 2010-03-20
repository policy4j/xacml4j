package com.artagon.xacml.v3.profiles;

import java.util.Collection;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestContextException;

public interface RequestContextProfile 
{
	/**
	 * Gets profile identifier
	 * 
	 * @return 
	 */
	String getId();
	
	Collection<RequestContext> apply(RequestContext context) 
		throws RequestContextException;
}
