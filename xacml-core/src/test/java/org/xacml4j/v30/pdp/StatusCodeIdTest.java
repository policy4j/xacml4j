package org.xacml4j.v30.pdp;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.StatusCodeIds;


public class StatusCodeIdTest
{
	@Test
	public void testParseStatusCodeId()
	{
		StatusCodeId c = StatusCodeIds.parse(StatusCodeIds.OK.getId());
		assertSame(StatusCodeIds.OK, c);
		c = StatusCodeIds.parse("AAAA");
		assertEquals("AAAA", c.getId());
		assertEquals("AAAA", c.toString());

		StatusCodeId c1 = StatusCodeIds.parse("AAAA");
		StatusCodeId c2 = StatusCodeIds.parse("AAAA");
		assertEquals(c1, c2);
	}
}
