package org.xacml4j.v30.spi.pip;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface XacmlAttributeSelector
{
	String xpath();
	String category();
	String dataType();
	String contextAttributeId() default "";
}
