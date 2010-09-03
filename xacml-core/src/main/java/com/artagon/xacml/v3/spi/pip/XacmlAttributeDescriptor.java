package com.artagon.xacml.v3.spi.pip;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artagon.xacml.v3.types.XacmlDataTypes;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlAttributeDescriptor 
{
	String id();
	XacmlDataTypes dataType() default XacmlDataTypes.STRING;
}
