package com.artagon.xacml.v3.types;

public final class RFC822NameValue extends SimpleAttributeValue<RFC822Name>
{
	private static final long serialVersionUID = -1983511364298319436L;

	RFC822NameValue(RFC822NameType type, RFC822Name value) {
		super(type, value);
	}
}
