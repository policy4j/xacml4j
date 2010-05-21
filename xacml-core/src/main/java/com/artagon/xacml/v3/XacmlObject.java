package com.artagon.xacml.v3;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class XacmlObject {
	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public static void checkNotNull(Object v, String template, Object... args)
			throws PolicySyntaxException {
		if (v == null) {
			throw new PolicySyntaxException(template, args);
		}
	}
	
	/**
	 * Checks if given condition is <code>true</code>
	 * @param condition
	 * @param template
	 * @param args
	 * @throws PolicySyntaxException
	 */
	protected static void checkSyntaxCondition(boolean condition, String template,
			Object... args) throws PolicySyntaxException {
		if (!condition) {
			throw new PolicySyntaxException(template, args);
		}
	}
}
