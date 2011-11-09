package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.core.RFC822Name;

public final class RFC822NameExp extends BaseAttributeExp<RFC822Name>
{
	private static final long serialVersionUID = -1983511364298319436L;

	RFC822NameExp(RFC822NameType type, RFC822Name value) {
		super(type, value);
	}
}
