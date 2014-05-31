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

import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;

import org.junit.Test;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.types.StringExp;


public class Xacml30ResponseContextUnmarshallerTest {

	@Test
	public void testResponseUnmarshal() throws Exception {
		Xacml30ResponseContextUnmarshaller u = new Xacml30ResponseContextUnmarshaller();
		Xacml30ResponseContextMarshaller m = new Xacml30ResponseContextMarshaller();

		ResponseContext resp = u.unmarshal(getClass().getClassLoader().getResourceAsStream("v30Response.xml"));
		
		JAXBElement<ResponseType> target = (JAXBElement<ResponseType>)m.marshal(resp);
		
		ResponseContext resp1 = u.unmarshal(target);
		assertEquals(resp,  resp1);
		
		assertNotNull(resp);
		assertEquals(1, resp.getResults().size());
		Result r1 = resp.getResults().iterator().next();

		// Test obligations
		assertEquals(2, r1.getObligations().size());
		Obligation o1 = r1.getObligation("urn:test:obligation1");
		assertNotNull(o1);
		assertEquals(1, o1.getAttributes().size());
		Collection<AttributeAssignment> oa1 = o1.getAttribute("urn:test:obligation1");
		assertEquals(1, oa1.size());
		assertEquals(StringExp.valueOf("oa-value"), oa1.iterator().next().getAttribute());

		assertNotNull(r1.getObligation("urn:test:obligation2"));

		// test advises
		assertEquals(2, r1.getAssociatedAdvice().size());
		Advice a1 = r1.getAssociatedAdvice("urn:test:advice1");
		assertNotNull(a1);
		assertEquals(1, a1.getAttributes().size());
		Collection<AttributeAssignment> aa1 = a1.getAttribute("urn:test:advice1:attr1");
		assertEquals(1, aa1.size());
		assertEquals(StringExp.valueOf("aa-value"), aa1.iterator().next().getAttribute());

		assertNotNull(r1.getAssociatedAdvice("urn:test:advice2"));

		// test attributes
		assertEquals(1, r1.getIncludeInResultAttributes().size());
		Entity attrs = r1.getAttribute(Categories.SUBJECT_ACCESS).getEntity();
		assertEquals(1, attrs.getAttributes().size());
		Collection<Attribute> attr1 = attrs.getAttributes("urn:test:attribute1");
		assertEquals(1, attr1.size());
		assertEquals(1, attr1.iterator().next().getValues().size());
		assertEquals(StringExp.valueOf("value"), attr1.iterator().next().getValues().iterator().next());

		// Test policy references
		assertEquals(2, r1.getPolicyIdentifiers().size());
		Iterator<CompositeDecisionRuleIDReference> refIterator = r1.getPolicyIdentifiers().iterator();
		CompositeDecisionRuleIDReference pi1 = refIterator.next();
		assertEquals("1.0", pi1.getVersion().toString());
		assertNull(pi1.getEarliestVersion());
		assertNull(pi1.getLatestVersion());

		CompositeDecisionRuleIDReference pi2 = refIterator.next();
		assertEquals("2.0",  pi2.getVersion().toString());
		assertNull(pi2.getLatestVersion());
	}
}
