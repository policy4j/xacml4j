package com.artagon.xacml.v3;

import java.util.Collection;

public interface RequestProcessingProfile 
{
	/**
	 * Gets profile identifier
	 * 
	 * @return 
	 */
	String getId();
	
	Collection<Result> process(Request context);
}
