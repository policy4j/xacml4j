package com.artagon.xacml.policy.function.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artagon.xacml.policy.type.XacmlDataType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface XacmlFuncVarArg 
{
	XacmlDataType type();
	int min() default 2;
	int max() default Integer.MAX_VALUE;
	boolean isBag() default false;
}
