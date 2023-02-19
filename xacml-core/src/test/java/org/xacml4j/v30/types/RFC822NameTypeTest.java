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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.Value;


public class RFC822NameTypeTest
{

	@Test
	public void testEquals()
	{
		Value n0 = XacmlTypes.RFC822NAME.of("test0@test.org");
		Value n1 = XacmlTypes.RFC822NAME.of("test1@test.org");
		Value n2 = XacmlTypes.RFC822NAME.of("test0@TEST.org");
		Value n3 = XacmlTypes.RFC822NAME.of("TEST0@test.org");
		assertFalse(n0.equals(n1));
		assertTrue(n0.equals(n2));
		assertFalse(n0.equals(n3));
	}
}
