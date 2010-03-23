package com.artagon.xacml.v3.profiles;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;

public interface RequestContextProfile 
{
	/**
	 * Gets profile identifier
	 * 
	 * @return 
	 */
	String getId();
	
	Response process(Request context);
}
