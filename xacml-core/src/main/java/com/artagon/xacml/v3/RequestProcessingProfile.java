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
	
	boolean isApplicable(Request request);
	
	Collection<Result> process(Request context, RequestProcessingPipelineCallback callback);
}
