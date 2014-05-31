package org.xacml4j.v30.marshal.jaxb;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeIds;
import org.xacml4j.v30.marshal.ResponseUnmarshaller;
import org.xacml4j.v30.types.StringExp;

import com.google.common.collect.Iterables;

public class Xacml20ResponseContextUnmarshallerTest
{
	private ResponseUnmarshaller unmarshaller;

	@Before
	public void init() throws Exception
	{
		this.unmarshaller = new Xacml20ResponseContextUnmarshaller();
	}

	@Test
	public void testResponseTest1Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ResponseContext res = unmarshaller.unmarshal(cl.getResourceAsStream("test-response.xml"));
		Result r = Iterables.getOnlyElement(res.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
		assertEquals(r.getStatus().getStatusCode().getValue(), StatusCodeIds.OK);
		assertNull(r.getStatus().getMessage());
		assertNull(r.getStatus().getDetail());
		assertNull(r.getStatus().getStatusCode().getMinorStatus());
		Obligation o1 = r.getObligation("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:obligation-1");
		assertNotNull(o1);
		assertEquals(StringExp.valueOf("assignment1"),
				Iterables.getOnlyElement(
						o1.getAttribute(
								"urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:assignment1")).getAttribute());
		assertEquals(StringExp.valueOf("assignment2"),
				Iterables.getOnlyElement(
						o1.getAttribute(
								"urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:assignment2")).getAttribute());
		Obligation o2 = r.getObligation("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:obligation-2");
		assertNotNull(o2);
		assertEquals(StringExp.valueOf("assignment1"),
				Iterables.getOnlyElement(
						o2.getAttribute(
								"urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:assignment1")).getAttribute());
		assertEquals(StringExp.valueOf("assignment2"),
				Iterables.getOnlyElement(
						o2.getAttribute(
								"urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:assignment2")).getAttribute());

	}
}
