package org.xacml4j.v30.spi.pip;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlAttributeDescriptor
{
	/**
	 * An attribute identifier
	 *
	 * @return attribute identifier
	 */
	String id();

	/**
	 * Attribute XACML data type identifier
	 *
	 * @return data type identifier
	 */
	String dataType();

	/**
	 * A default values for the attribute
	 *
	 * @return an array of default values for
	 * the attribute
	 */
	String[] defaultValue() default {};
}
