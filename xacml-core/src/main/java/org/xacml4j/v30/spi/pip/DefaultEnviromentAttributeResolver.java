package org.xacml4j.v30.spi.pip;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.xacml4j.v30.Categories;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.types.DateExp;
import org.xacml4j.v30.types.DateTimeExp;
import org.xacml4j.v30.types.TimeExp;
import org.xacml4j.v30.types.XacmlTypes;


/**
 * A default XACML enviroment attributes resolver
 *
 * @author Giedrius Trumpickas
 */
class DefaultEnviromentAttributeResolver extends BaseAttributeResolver
{
	public DefaultEnviromentAttributeResolver() {
		super(AttributeResolverDescriptorBuilder.builder(
				"XacmlEnvironmentResolver",
				"XACML Enviroment Attributes Resolver",
				Categories.ENVIRONMENT)
				.noCache()
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-time", XacmlTypes.TIME)
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-date", XacmlTypes.DATE)
				.attribute("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime",XacmlTypes.DATETIME)
				.build());
	}

	@Override
	protected Map<String, BagOfAttributeExp> doResolve(
			ResolverContext context) throws Exception {
		Calendar currentDateTime = context.getCurrentDateTime();
		Map<String, BagOfAttributeExp> v = new HashMap<String, BagOfAttributeExp>();
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-time",
					TimeExp.valueOf(currentDateTime).toBag());
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-date",
					DateExp.valueOf(currentDateTime).toBag());
		v.put("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime",
					DateTimeExp.valueOf(currentDateTime).toBag());
		return v;
	}
}
