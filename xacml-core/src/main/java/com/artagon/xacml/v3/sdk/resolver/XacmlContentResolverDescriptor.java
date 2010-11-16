package com.artagon.xacml.v3.sdk.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlContentResolverDescriptor 
{
	String id();
	String name();
	String category();
	int cacheTTL() default 0;
}
