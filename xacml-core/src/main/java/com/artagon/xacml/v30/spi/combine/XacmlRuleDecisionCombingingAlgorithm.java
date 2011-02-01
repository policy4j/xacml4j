package com.artagon.xacml.v30.spi.combine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlRuleDecisionCombingingAlgorithm 
{
	String algorithmId();
	boolean legacy() default false;
}
