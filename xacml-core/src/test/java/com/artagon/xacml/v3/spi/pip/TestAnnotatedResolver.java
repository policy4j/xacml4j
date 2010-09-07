package com.artagon.xacml.v3.spi.pip;

import java.util.Collection;

import org.junit.Ignore;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@XacmlAttributeResolverDesc(
	name="Test Resolver", 
	categories={
		@XacmlAttributeCategory(AttributeCategoryId.SUBJECT_ACCESS), 
		@XacmlAttributeCategory(AttributeCategoryId.SUBJECT_INTERMEDIARY),
		@XacmlAttributeCategory(AttributeCategoryId.SUBJECT_RECIPIENT)
	}
)
@Ignore
public class TestAnnotatedResolver 
{
	@XacmlAttributeDescriptor(id="testId", dataType=XacmlDataTypes.STRING)
	public Collection<String> getUserDevices(
			PolicyInformationPointContext context, String username)
	{
		return null;
	}
}
