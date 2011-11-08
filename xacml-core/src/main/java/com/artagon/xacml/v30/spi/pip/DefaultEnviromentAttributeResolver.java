package com.artagon.xacml.v30.spi.pip;


import static com.artagon.xacml.v30.types.DateTimeType.DATETIME;
import static com.artagon.xacml.v30.types.DateType.DATE;
import static com.artagon.xacml.v30.types.TimeType.TIME;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.core.AttributeCategories;

/**
 * A default XACML enviroment attributes resolver
 * 
 * @author Giedrius Trumpickas
 */
class DefaultEnviromentAttributeResolver extends BaseAttributeResolver
{	
	public DefaultEnviromentAttributeResolver() {
		super(AttributeResolverDescriptorBuilder.builder(
				"urn:oasis:names:tc:xacml:1.0:environment:resolver", 
				"XACML Enviroment Attributes Resolver", 
				AttributeCategories.ENVIRONMENT)
				.noCache()
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-time",TIME)
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-date",DATE)
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime",DATETIME).build());
	}
	
	@Override
	protected Map<String, BagOfAttributesExp> doResolve(
			ResolverContext context) throws Exception {
		Calendar currentDateTime = context.getCurrentDateTime();
		Map<String, BagOfAttributesExp> v = new HashMap<String, BagOfAttributesExp>();	
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-time", 
					TIME.bagOf(TIME.create(currentDateTime)));
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-date", 
					DATE.bagOf(DATE.create(currentDateTime)));
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime", 
					DATETIME.bagOf(DATETIME.create(currentDateTime)));
		return v;
	}
}
