package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.XacmlAttributeCategory;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v3.types.DateTimeType;
import com.artagon.xacml.v3.types.DateType;
import com.artagon.xacml.v3.types.TimeType;

@XacmlAttributeResolverDescriptor(name="A Default XACML 2.0 & 3.0 Enviroment Attributes Resolver")
class DefaultEnviromentAttributeResolver 
{	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-time", 
				typeId="http://www.w3.org/2001/XMLSchema#time")
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
	public BagOfAttributeValues getCurrentTime(
			PolicyInformationPointContext context)
	{
		return TimeType.TIME.bagOf(context.getCurrentTime());
	}
	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-date", 
				typeId="http://www.w3.org/2001/XMLSchema#date")
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
	public BagOfAttributeValues getCurrentDate(
			PolicyInformationPointContext context)
	{
		return DateType.DATE.bagOf(context.getCurrentDate());
	}
	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-dateTime", 
			typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
	public BagOfAttributeValues getCurrentDateTime(
			PolicyInformationPointContext context)
	{
		return DateTimeType.DATETIME.bagOf(context.getCurrentDateTime());
	}
}
