package com.artagon.xacml.v3.sdk.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface XacmlFuncParam 
{
	String typeId();
	boolean isBag() default false;
}
