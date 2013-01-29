package org.xacml4j.v30.spi.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface XacmlFuncParamVarArg 
{
	String typeId();
	int min() default 2;
	int max() default Integer.MAX_VALUE;
	boolean isBag() default false;
}
