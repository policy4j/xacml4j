package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.core.RFC822Name;

public final class RFC822NameValueExp extends BaseAttributeExpression<RFC822Name>
{
	private static final long serialVersionUID = -1983511364298319436L;

	RFC822NameValueExp(RFC822NameType type, RFC822Name value) {
		super(type, value);
	}
}
