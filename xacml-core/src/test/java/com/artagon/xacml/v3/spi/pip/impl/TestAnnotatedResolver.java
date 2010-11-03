package com.artagon.xacml.v3.spi.pip.impl;

import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;


@XacmlAttributeResolverDescriptor(
		id="testId", name="AAAAAAAA", category="Aaaaaaaa", 
		attributes={
				@XacmlAttributeDescriptor(typeId="aaa", id="aaaaaaaaaaaaaaaaaa"),
				@XacmlAttributeDescriptor(typeId="aaa", id="aaaaaaaaaaaaaaaaaa"),
				@XacmlAttributeDescriptor(typeId="aaa", id="aaaaaaaaaaaaaaaaaa"),
})
public class TestAnnotatedResolver 
{
	
}
