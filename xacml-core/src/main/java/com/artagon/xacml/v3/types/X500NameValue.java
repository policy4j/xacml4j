package com.artagon.xacml.v3.types;

import javax.security.auth.x500.X500Principal;

public final class X500NameValue extends SimpleAttributeValue<X500Principal>
{
	X500NameValue(X500NameType type,
			X500Principal value) {
		super(type, value);
	}	
}
