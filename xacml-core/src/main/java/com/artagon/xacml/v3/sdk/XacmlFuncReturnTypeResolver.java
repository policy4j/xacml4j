package com.artagon.xacml.v3.sdk;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artagon.xacml.v3.spi.function.FunctionReturnTypeResolver;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlFuncReturnTypeResolver 
{
	Class<? extends FunctionReturnTypeResolver> resolverClass();
}
