package com.artagon.xacml.v3.policy.spi;

import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.UnsupportedFunctionException;

public interface FunctionProvidersRegistry {

	public abstract FunctionSpec getFunction(String functionId)
			throws UnsupportedFunctionException;

}