package org.xacml4j.v20;

/*
 * #%L
 * Xacml4J Conformance Tests
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.types.XacmlTypes;

public class Xacml20ConformanceAttributeResolverTest
{
	@Test
	public void testIIA002ResolvesPhysicianRoleWithoutCaseSensitivity()
	{
		Map<String, BagOfValues> attributes = new Xacml20ConformanceAttributeResolver()
				.IIA002(XacmlTypes.STRING.ofAny("jULIUS hIBBERT").toBag());
		assertEquals(1, attributes.size());
		assertEquals(XacmlTypes.STRING.ofAny("Physician").toBag(),
				attributes.get("urn:oasis:names:tc:xacml:1.0:example:attribute:role"));
	}

	@Test
	public void testIIA002DoesNotResolveRoleForAnotherSubject()
	{
		assertTrue(new Xacml20ConformanceAttributeResolver()
				.IIA002(XacmlTypes.STRING.ofAny("another subject").toBag()).isEmpty());
	}
}
