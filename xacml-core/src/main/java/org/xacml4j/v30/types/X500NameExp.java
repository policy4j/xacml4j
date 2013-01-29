package org.xacml4j.v30.types;

import javax.security.auth.x500.X500Principal;

public final class X500NameExp extends BaseAttributeExp<X500Principal>
{
	private static final long serialVersionUID = -609417077475809404L;

	X500NameExp(X500NameType type,
			X500Principal value) {
		super(type, value);
	}	
}
