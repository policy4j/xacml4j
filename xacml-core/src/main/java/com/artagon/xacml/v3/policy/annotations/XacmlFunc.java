package com.artagon.xacml.v3.policy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlFunc 
{
	String id();
	boolean evaluateArguments() default true;
}
