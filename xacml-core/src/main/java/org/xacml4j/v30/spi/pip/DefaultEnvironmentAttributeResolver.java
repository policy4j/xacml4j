package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.types.DateExp;
import org.xacml4j.v30.types.DateTimeExp;
import org.xacml4j.v30.types.TimeExp;
import org.xacml4j.v30.types.XacmlTypes;


/**
 * A default XACML environment attributes resolver
 *
 * @author Giedrius Trumpickas
 */
class DefaultEnvironmentAttributeResolver extends BaseAttributeResolver
{
	public DefaultEnvironmentAttributeResolver() {
		super(AttributeResolverDescriptorBuilder.builder(
				"XacmlEnvironmentResolver",
				"XACML Environment Attributes Resolver",
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
