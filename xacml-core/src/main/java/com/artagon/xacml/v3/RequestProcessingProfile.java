package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.List;

public interface RequestProcessingProfile 
{
	/**
	 * Gets profile identifier
	 * 
	 * @return 
	 */
	String getId();
	
	Collection<Result> process(Request request, 
			List<RequestProcessingProfile> next, 
			PolicyDecisionPoint pdp);
}
