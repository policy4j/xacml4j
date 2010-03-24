package com.artagon.xacml.v3;

import java.util.Collection;

public interface RequestProcessingPipelineCallback 
{
	Collection<Result> invokeNext(Request request);
}
