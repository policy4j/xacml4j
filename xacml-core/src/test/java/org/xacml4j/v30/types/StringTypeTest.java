package org.xacml4j.v30.types;

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

import org.junit.Test;
import org.xacml4j.v30.Value;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class StringTypeTest
{

	@Test
	public void testEquals()
	{
		Value v0 = XacmlTypes.STRING.of("v0");
		Value v1 = XacmlTypes.STRING.of("v1");
		assertFalse(v0.equals(v1));
		Value v2 = XacmlTypes.STRING.of("v0");
		assertFalse(v1.equals(v2));
		assertTrue(v0.equals(v2));
	}
}
