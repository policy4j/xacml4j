package org.xacml4j.v30.spi.combine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlPolicyDecisionCombingingAlgorithm
{
	String value();
	boolean legacy() default false;
}
