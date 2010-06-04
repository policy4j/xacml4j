package com.artagon.xacml.v3.policy.spi;

import com.artagon.xacml.v3.policy.FunctionSpec;

public interface FunctionProvidersRegistry 
{
	FunctionSpec getFunction(String functionId);
}