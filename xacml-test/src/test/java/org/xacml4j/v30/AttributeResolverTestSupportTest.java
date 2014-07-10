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

import static org.junit.Assert.fail;
import static org.xacml4j.v30.types.XacmlTypes.STRING;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class AttributeResolverTestSupportTest extends AttributeResolverTestSupport {

	@Test
	public void testCheckHasAttribute() throws Exception {
		Map<String, BagOfAttributeExp> attributeBag = ImmutableMap.of(
				"key1", STRING.bagOf(STRING.create("value1"), STRING.create("value2")));

		checkHasAttribute(attributeBag, "key1", STRING, "value1", "value2");

		try {
			checkHasAttribute(attributeBag, "key2", STRING);
			fail("Should fail, as 'key2' does not exist");
		} catch (AssertionError ignored) { }

		try {
			checkHasAttribute(attributeBag, "key1", STRING, "value1");
			fail("Should fail, as number of values is not the same");
		} catch (AssertionError ignored) { }

		try {
			checkHasAttribute(attributeBag, "key1", STRING, "value1", "value3");
			fail("Should fail, as the values are not the same");
		} catch (AssertionError ignored) { }
	}
}