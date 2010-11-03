package com.artagon.xacml.v3.spi.pip;


import static com.artagon.xacml.v3.types.DateTimeType.DATETIME;
import static com.artagon.xacml.v3.types.DateType.DATE;
import static com.artagon.xacml.v3.types.TimeType.TIME;

import java.util.Calendar;
import java.util.Map;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.BagOfAttributeValues;

class DefaultEnviromentAttributeResolver extends BaseAttributeResolver
{	
	public DefaultEnviromentAttributeResolver() {
		super(AttributeResolverDescriptorBuilder.create(
				"urn:oasis:names:tc:xacml:1.0:environment:resolver", 
				"XACML Enviroment Attributes Resolver", AttributeCategories.ENVIRONMENT)
				.noCache()
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-time",TIME)
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-date",DATE)
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime",DATETIME).build());
	}
	
	@Override
	protected Map<String, BagOfAttributeValues> doResolve(
			PolicyInformationPointContext context) {
		Calendar currentDateTime = context.getCurrentDateTime();
		return newResult()
			.value("urn:oasis:names:tc:xacml:1.0:environment:current-time", TIME.bagOf(TIME.create(currentDateTime)))
			.value("urn:oasis:names:tc:xacml:1.0:environment:current-date", DATE.bagOf(DATE.create(currentDateTime)))
			.value("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime", DATETIME.bagOf(DATETIME.create(currentDateTime)))
			.build();
	}
}
