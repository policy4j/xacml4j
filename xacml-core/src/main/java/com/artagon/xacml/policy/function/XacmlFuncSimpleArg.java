package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.type.XacmlDataType;

public @interface XacmlFuncSimpleArg 
{
	XacmlDataType type();
	boolean isBag() default false;
}
