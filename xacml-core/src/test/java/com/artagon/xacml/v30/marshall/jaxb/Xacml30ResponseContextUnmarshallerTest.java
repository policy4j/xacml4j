package com.artagon.xacml.v30.marshall.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import com.artagon.xacml.v30.Advice;
import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.AttributeAssignment;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v30.Obligation;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.core.AttributeCategories;

public class Xacml30ResponseContextUnmarshallerTest {

	@Test
	public void testResponseUnmarshal() throws Exception {
		Xacml30ResponseContextUnmarshaller u = new Xacml30ResponseContextUnmarshaller();
		
		ResponseContext resp = u.unmarshal(getClass().getClassLoader().getResourceAsStream("v30Response.xml"));
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
		assertEquals("oa-value", oa1.iterator().next().getAttribute().toXacmlString());
		
		assertNotNull(r1.getObligation("urn:test:obligation2"));

		// test advises
		assertEquals(2, r1.getAssociatedAdvice().size());
		Advice a1 = r1.getAssociatedAdvice("urn:test:advice1");
		assertNotNull(a1);
		assertEquals(1, a1.getAttributes().size());
		Collection<AttributeAssignment> aa1 = a1.getAttribute("urn:test:advice1:attr1");
		assertEquals(1, aa1.size());
		assertEquals("aa-value", aa1.iterator().next().getAttribute().toXacmlString());

		assertNotNull(r1.getAssociatedAdvice("urn:test:advice2"));

		// test attributes
		assertEquals(1, r1.getIncludeInResultAttributes().size());
		Attributes attrs = r1.getAttribute(AttributeCategories.SUBJECT_ACCESS);
		assertEquals(1, attrs.getAttributes().size());
		Collection<Attribute> attr1 = attrs.getAttributes("urn:test:attribute1");
		assertEquals(1, attr1.size());
		assertEquals(1, attr1.iterator().next().getValues().size());
		assertEquals("value", attr1.iterator().next().getValues().iterator().next().toXacmlString());

		// Test policy references
		assertEquals(2, r1.getPolicyIdentifiers().size());
		Iterator<CompositeDecisionRuleIDReference> refIterator = r1.getPolicyIdentifiers().iterator();
		CompositeDecisionRuleIDReference pi1 = refIterator.next();
		assertEquals("1.0", pi1.getVersion().getPattern());
		assertNull(pi1.getEarliestVersion());
		assertNull(pi1.getLatestVersion());
		
		CompositeDecisionRuleIDReference pi2 = refIterator.next();
		assertNull(pi2.getVersion());
		assertEquals("2.0", pi2.getEarliestVersion().getPattern());
		assertNull(pi2.getLatestVersion());
	}
}
