package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Policy Unit Test Support
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

public class AttributeResolverTestSupport {

	protected <T extends AttributeExpType> void checkHasAttribute(
			Map<String, BagOfAttributeExp> v, String attributeName,
			AttributeExp ...values) {
		BagOfAttributeExp bag = v.get(attributeName);
		checkHasValues(bag, values);
	}

	protected <T extends AttributeExpType> void checkHasValues(BagOfAttributeExp bag,
			AttributeExp ...values) {
		assertNotNull(bag);
		assertEquals(values.length, bag.size());
		for(AttributeExp value: values) {
			assertTrue(bag.contains(value));
		}
	}
}
