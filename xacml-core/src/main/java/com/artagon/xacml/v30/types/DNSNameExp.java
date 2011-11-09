package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.DNSName;


public final class DNSNameExp extends BaseAttributeExp<DNSName>
{
	private static final long serialVersionUID = -1729624624549215684L;
	
	public DNSNameExp(DNSNameType type, DNSName value){
		super(type, value);
	}
}

