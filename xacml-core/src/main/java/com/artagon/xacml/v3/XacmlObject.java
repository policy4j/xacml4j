package com.artagon.xacml.v3;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class XacmlObject 
{
	@Override
	public boolean equals(Object o){
		return EqualsBuilder.reflectionEquals(this, o);
	}
	
	@Override
	public int hashCode(){
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
}
