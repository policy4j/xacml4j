package com.artagon.xacml.v3.spi.pip;

import java.util.Collection;

import org.junit.Ignore;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@XacmlAttributeResolverDescriptor(name="Test Resolver")
@XacmlAttributeIssuer("testIssuer")
@Ignore
public class TestAnnotatedResolver 
{
	@XacmlAttributeDescriptor(id="testId1", dataType=XacmlDataTypes.STRING)
	@XacmlAttributeCategory(AttributeCategoryId.RESOURCE)
	public Collection<String> getUserDevices(
			PolicyInformationPointContext context, 
			RequestContextAttributesCallback callback)
	{
		return null;
	}
	
	@XacmlAttributeDescriptor(id="testId2", dataType=XacmlDataTypes.INTEGER)
	@XacmlAttributeCategory(AttributeCategoryId.SUBJECT_ACCESS)
	public Collection<String> getOtherStuff(
			PolicyInformationPointContext context, 
			RequestContextAttributesCallback callback)
	{
		return null;
	}
}
