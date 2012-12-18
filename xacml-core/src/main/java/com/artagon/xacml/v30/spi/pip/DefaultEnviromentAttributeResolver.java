package com.artagon.xacml.v30.spi.pip;


import static com.artagon.xacml.v30.types.DateTimeType.DATETIME;
import static com.artagon.xacml.v30.types.DateType.DATE;
import static com.artagon.xacml.v30.types.TimeType.TIME;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.BagOfAttributeExp;

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
	protected Map<String, BagOfAttributeExp> doResolve(
			ResolverContext context) throws Exception {
		Calendar currentDateTime = context.getCurrentDateTime();
		Map<String, BagOfAttributeExp> v = new HashMap<String, BagOfAttributeExp>();
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-time",
					TIME.bagOf(currentDateTime));
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-date",
					DATE.bagOf(currentDateTime));
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime",
					DATETIME.bagOf(currentDateTime));
		return v;
	}
}
