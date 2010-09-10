package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.XacmlAttributeCategory;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@XacmlAttributeResolverDescriptor(name="A Default XACML 2.0 & 3.0 Enviroment Attributes Resolver")
class DefaultEnviromentAttributeResolver 
{	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-time", dataType=XacmlDataTypes.TIME)
	@XacmlAttributeCategory(AttributeCategoryId.ENVIRONMENT)
	public BagOfAttributeValues<AttributeValue> getCurrentTime(
			PolicyInformationPointContext context)
	{
		return XacmlDataTypes.TIME.bag(context.getCurrentTime());
	}
	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-date", dataType=XacmlDataTypes.DATE)
	@XacmlAttributeCategory(AttributeCategoryId.ENVIRONMENT)
	public BagOfAttributeValues<AttributeValue> getCurrentDate(
			PolicyInformationPointContext context)
	{
		return XacmlDataTypes.DATE.bag(context.getCurrentDate());
	}
	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-dateTime", dataType=XacmlDataTypes.DATETIME)
	@XacmlAttributeCategory(AttributeCategoryId.ENVIRONMENT)
	public BagOfAttributeValues<AttributeValue> getCurrentDateTime(
			PolicyInformationPointContext context)
	{
		return XacmlDataTypes.DATETIME.bag(context.getCurrentDateTime());
	}
}
