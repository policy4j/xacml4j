package com.artagon.xacml.v3.types;

import java.net.URI;

public final class AnyURIValue extends BaseAttributeValue<URI> 
{
	public AnyURIValue(URI value){
		super(AnyURIType.ANYURI, value);
	}
}

