package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.core.DNSName;


public final class DNSNameValueExp extends BaseAttributeExpression<DNSName>
{
	private static final long serialVersionUID = -1729624624549215684L;
	
	public DNSNameValueExp(DNSNameType type, DNSName value){
		super(type, value);
	}
}

