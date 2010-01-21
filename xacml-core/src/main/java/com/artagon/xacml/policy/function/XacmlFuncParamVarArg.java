package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.type.XacmlDataType;

public @interface XacmlFuncParamVarArg 
{
	XacmlDataType type();
	int min() default 1;
	int max() default 2;
	boolean isBag() default false;
}
