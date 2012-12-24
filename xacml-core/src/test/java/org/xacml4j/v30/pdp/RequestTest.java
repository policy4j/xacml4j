package org.xacml4j.v30.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.xacml4j.v30.types.StringType.STRING;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.XPathVersion;



public class RequestTest
{
	private Attributes resource0;
	private Attributes resource1;
	private Attributes subject0;
	private Attributes subject1;

	@Before
	public void init()
	{
			this.resource0 = Attributes.builder(AttributeCategories.RESOURCE)
				.attributes(
						Attribute.builder("testId10").value(STRING.create("value0")).build(),
						Attribute.builder("testId11").value(STRING.create("value1")).build())
				.build();
		this.resource1 = Attributes.builder(AttributeCategories.RESOURCE)
				.attributes(
						Attribute.builder("testId11").value(STRING.create("value0")).build(),
						Attribute.builder("testId22").value(STRING.create("value1")).build(),
						Attribute.builder("testId23").includeInResult(true).value(STRING.create("value2")).build(),
						Attribute.builder("testId24").issuer("testIssuer").includeInResult(true).value(STRING.create("value2")).build())
				.build();
		this.subject0 =  Attributes.builder(AttributeCategories.SUBJECT_ACCESS)
				.attributes(
						Attribute.builder("testId31").value(STRING.create("value0")).build(),
						Attribute.builder("testId32").value(STRING.create("value1")).build())
				.build();
		this.subject1 = Attributes.builder(AttributeCategories.SUBJECT_CODEBASE)
				.attributes(
						Attribute.builder("testId11").value(STRING.create("value0")).build(),
						Attribute.builder("testId22").value(STRING.create("value1")).build(),
						Attribute.builder("testId23").includeInResult(true).value(STRING.create("value2")).build(),
						Attribute.builder("testId24").includeInResult(true).issuer("testIssuer").value(STRING.create("value2")).build())
				.build();

	}

	@Test
	public void testHasRepeatingCategories()
	{
		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0));
		assertFalse(request.containsRepeatingCategories());
		request = new RequestContext(false,
				Arrays.asList(subject0, resource0, resource1));
		assertTrue(request.containsRepeatingCategories());
	}


	@Test
	public void testCreateRequest()
	{

		RequestContext request1 = new RequestContext(false,
				Arrays.asList(subject0, resource0, resource1));
		assertFalse(request1.isReturnPolicyIdList());
		assertEquals(3, request1.getAttributes().size());
		assertTrue(request1.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		assertTrue(request1.getAttributes(AttributeCategories.RESOURCE).contains(resource1));
		assertTrue(request1.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject0));

		RequestContext request2 = new RequestContext(true,
				Arrays.asList(subject0, resource0, resource1));

		assertTrue(request2.isReturnPolicyIdList());
		assertTrue(request1.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		assertTrue(request1.getAttributes(AttributeCategories.RESOURCE).contains(resource1));
		assertTrue(request1.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject0));
	}

	@Test
	public void testGetAttributesByCategory()
	{

		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0, resource1));
		Collection<Attributes> attr = request.getAttributes(AttributeCategories.RESOURCE);
		assertEquals(2, attr.size());
		assertTrue(attr.contains(resource0));
		assertTrue(attr.contains(resource1));
		attr = request.getAttributes(AttributeCategories.ENVIRONMENT);
		assertEquals(0, attr.size());
	}

	@Test
	public void testGetAttributeByCategory()
	{

		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0));
		Collection<Attributes> attr = request.getAttributes(AttributeCategories.ACTION);
		assertNotNull(attr);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetOnlyAttributesMultipleInstancesOfTheSameCategory()
	{
		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0, resource1));
		request.getOnlyAttributes(AttributeCategories.RESOURCE);
	}

	@Test
	public void testGetOnlyAttributeSingleInstanceOfTheSameCategory()
	{
		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0));
		Attributes attr = request.getOnlyAttributes(AttributeCategories.RESOURCE);
		assertEquals(resource0, attr);
	}

	@Test
	public void testGetRequestDefaults(){
		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0, resource1));
		assertNotNull(request.getRequestDefaults());
		assertEquals(XPathVersion.XPATH1, request.getRequestDefaults().getXPathVersion());
	}

	@Test
	public void testGetIncludeInResult()
	{

		RequestContext request0 = new RequestContext(false,
				Arrays.asList(subject0, resource0));

		assertEquals(0, request0.getIncludeInResultAttributes().size());

		RequestContext request1 = new RequestContext(false,
				Arrays.asList(subject0, subject1, resource0, resource1));

		assertEquals(2, request1.getIncludeInResultAttributes().size());

		RequestContext request2 = new RequestContext(false,
				Arrays.asList(subject0, subject1, resource0, resource1));
		assertEquals(2, request2.getIncludeInResultAttributes().size());
	}
}
