package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.XacmlAttributeCategory;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v3.types.DateTimeType;
import com.artagon.xacml.v3.types.DateType;
import com.artagon.xacml.v3.types.TimeType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@XacmlAttributeResolverDescriptor(name="A Default XACML 2.0 & 3.0 Enviroment Attributes Resolver")
class DefaultEnviromentAttributeResolver 
{	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-time", 
				dataType=XacmlDataTypes.TIME)
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
	public BagOfAttributeValues<TimeType.TimeValue> getCurrentTime(
			PolicyInformationPointContext context)
	{
		return TimeType.Factory.bagOf(context.getCurrentTime());
	}
	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-date", 
				dataType=XacmlDataTypes.DATE)
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
	public BagOfAttributeValues<DateType.DateValue> getCurrentDate(
			PolicyInformationPointContext context)
	{
		return DateType.Factory.bagOf(context.getCurrentDate());
	}
	
	@XacmlAttributeDescriptor(id="urn:oasis:names:tc:xacml:1.0:environment:current-dateTime", 
			dataType=XacmlDataTypes.DATETIME)
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
	public BagOfAttributeValues<DateTimeType.DateTimeValue> getCurrentDateTime(
			PolicyInformationPointContext context)
	{
		return DateTimeType.Factory.bagOf(context.getCurrentDateTime());
	}
}
