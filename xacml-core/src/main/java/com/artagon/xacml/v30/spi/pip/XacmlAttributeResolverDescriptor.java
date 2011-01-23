package com.artagon.xacml.v30.spi.pip;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlAttributeResolverDescriptor 
{
	String id();
	String name();
	String category();
	String issuer() default "";
	int cacheTTL() default 0;
	XacmlAttributeDescriptor[] attributes();
}
