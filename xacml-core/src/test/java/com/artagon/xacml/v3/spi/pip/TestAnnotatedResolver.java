package com.artagon.xacml.v3.spi.pip;

import org.junit.Ignore;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.XacmlAttributeCategory;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeIssuer;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@XacmlAttributeResolverDescriptor(name="Test Resolver")
@XacmlAttributeIssuer("testIssuer")
@Ignore
public class TestAnnotatedResolver 
{
	@XacmlAttributeDescriptor(id="testId1", typeId="http://www.w3.org/2001/XMLSchema#string")
	@XacmlAttributeCategory({"urn:oasis:names:tc:xacml:3.0:attribute-category:resource", 
		"urn:oasis:names:tc:xacml:3.0:attribute-category:action"})
	public BagOfAttributeValues<? extends AttributeValue> getTestId1Attribute(
			PolicyInformationPointContext context)
	{
		return XacmlDataTypes.STRING.emptyBag();
	}
	
	@XacmlAttributeDescriptor(id="testId2", typeId="http://www.w3.org/2001/XMLSchema#integer")
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject")
	public BagOfAttributeValues<AttributeValue> getTestId2Attribute(
			PolicyInformationPointContext context)
	{
		return XacmlDataTypes.INTEGER.bagOf(XacmlDataTypes.INTEGER.create(1));
	}
}
