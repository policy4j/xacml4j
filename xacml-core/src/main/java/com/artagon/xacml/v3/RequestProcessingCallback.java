package com.artagon.xacml.v3;

import java.util.Collection;

public interface RequestProcessingCallback 
{
	Collection<Result> invokeNext(Request request);
}
