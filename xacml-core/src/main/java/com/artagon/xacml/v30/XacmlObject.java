package com.artagon.xacml.v30;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.base.Preconditions;

public class XacmlObject 
{
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
				ToStringStyle.SIMPLE_STYLE);
	}

	protected static void checkNotNull(Object v, String template, Object... args)
			throws NullPointerException {
		Preconditions.checkNotNull(v, template, args);
	}
	
	protected static void checkArgument(boolean condition, String template,
			Object... args) throws IllegalArgumentException {
		Preconditions.checkArgument(condition, template, args);
	}
}
