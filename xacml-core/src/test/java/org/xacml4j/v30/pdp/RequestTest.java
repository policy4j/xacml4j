package org.xacml4j.v30.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.types.StringExp;



public class RequestTest
{
	private Category resource0;
	private Category resource1;
	private Category subject0;
	private Category subject1;

	@Before
	public void init()
	{
			this.resource0 = Category.builder(Categories.RESOURCE)
				.entity(
						Entity
						.builder()
						.attribute(
						Attribute.builder("testId10").value(StringExp.valueOf("value0")).build(),
						Attribute.builder("testId11").value(StringExp.valueOf("value1")).build()).build())
				.build();
		this.resource1 = Category.builder(Categories.RESOURCE)
				.entity(
						Entity
						.builder()
						.attribute(
						Attribute.builder("testId11").value(StringExp.valueOf("value0")).build(),
						Attribute.builder("testId22").value(StringExp.valueOf("value1")).build(),
						Attribute.builder("testId23").includeInResult(true).value(StringExp.valueOf("value2")).build(),
						Attribute.builder("testId24").issuer("testIssuer").includeInResult(true).value(StringExp.valueOf("value2")).build()).build())
				.build();
		this.subject0 =  Category.builder(Categories.SUBJECT_ACCESS)
				.entity(
						Entity
						.builder()
						.attribute(
						Attribute.builder("testId31").value(StringExp.valueOf("value0")).build(),
						Attribute.builder("testId32").value(StringExp.valueOf("value1")).build()).build())
				.build();
		this.subject1 = Category.builder(Categories.SUBJECT_CODEBASE)
				.entity(
						Entity
						.builder()
						.attribute(
						Attribute.builder("testId11").value(StringExp.valueOf("value0")).build(),
						Attribute.builder("testId22").value(StringExp.valueOf("value1")).build(),
						Attribute.builder("testId23").includeInResult(true).value(StringExp.valueOf("value2")).build(),
						Attribute.builder("testId24").includeInResult(true).issuer("testIssuer").value(StringExp.valueOf("value2")).build()).build())
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
		assertTrue(request1.getAttributes(Categories.RESOURCE).contains(resource0));
		assertTrue(request1.getAttributes(Categories.RESOURCE).contains(resource1));
		assertTrue(request1.getAttributes(Categories.SUBJECT_ACCESS).contains(subject0));

		RequestContext request2 = new RequestContext(true,
				Arrays.asList(subject0, resource0, resource1));

		assertTrue(request2.isReturnPolicyIdList());
		assertTrue(request1.getAttributes(Categories.RESOURCE).contains(resource0));
		assertTrue(request1.getAttributes(Categories.RESOURCE).contains(resource1));
		assertTrue(request1.getAttributes(Categories.SUBJECT_ACCESS).contains(subject0));
	}

	@Test
	public void testGetAttributesByCategory()
	{

		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0, resource1));
		Collection<Category> attr = request.getAttributes(Categories.RESOURCE);
		assertEquals(2, attr.size());
		assertTrue(attr.contains(resource0));
		assertTrue(attr.contains(resource1));
		attr = request.getAttributes(Categories.ENVIRONMENT);
		assertEquals(0, attr.size());
	}

	@Test
	public void testGetAttributeByCategory()
	{

		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0));
		Collection<Category> attr = request.getAttributes(Categories.ACTION);
		assertNotNull(attr);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetOnlyAttributesMultipleInstancesOfTheSameCategory()
	{
		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0, resource1));
		request.getOnlyAttributes(Categories.RESOURCE);
	}

	@Test
	public void testGetOnlyAttributeSingleInstanceOfTheSameCategory()
	{
		RequestContext request = new RequestContext(false,
				Arrays.asList(subject0, resource0));
		Category attr = request.getOnlyAttributes(Categories.RESOURCE);
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
