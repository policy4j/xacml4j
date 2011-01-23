package com.artagon.xacml.v30.types;

import java.net.URI;

public final class AnyURIValue extends SimpleAttributeValue<URI> 
{
	private static final long serialVersionUID = -1279561638068756670L;

	public AnyURIValue(URI value){
		super(AnyURIType.ANYURI, value);
	}
}

