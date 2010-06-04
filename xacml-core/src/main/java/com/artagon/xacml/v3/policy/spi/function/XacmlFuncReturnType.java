package com.artagon.xacml.v3.policy.spi.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artagon.xacml.v3.types.XacmlDataTypes;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlFuncReturnType 
{
	XacmlDataTypes type();
	boolean isBag() default false;
}
