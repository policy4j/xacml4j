package org.xacml4j.v30.policy;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.request.RequestContext;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.types.XacmlTypes;


public class RequestTest
{
	private Category resource0;
	private Category resource1;
	private Category subject0;
	private Category subject1;

	@Before
	public void init()
	{
			this.resource0 = Category.builder(CategoryId.RESOURCE)
				.entity(
						Entity
						.builder()
						.attribute(
						Attribute.builder("testId10").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId11").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();
		this.resource1 = Category.builder(CategoryId.RESOURCE)
				.entity(
						Entity
						.builder()
						.attribute(
						Attribute.builder("testId11").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId22").value(XacmlTypes.STRING.of("value1")).build(),
						Attribute.builder("testId23").includeInResult(true).value(XacmlTypes.STRING.of("value2")).build(),
						Attribute.builder("testId24").issuer("testIssuer").includeInResult(true).value(XacmlTypes.STRING.of("value2")).build()).build())
				.build();
		this.subject0 =  Category.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity
						.builder()
						.attribute(
						Attribute.builder("testId31").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId32").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();
		this.subject1 = Category.builder(CategoryId.SUBJECT_CODEBASE)
				.entity(
						Entity
						.builder()
						.attribute(
						Attribute.builder("testId11").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId22").value(XacmlTypes.STRING.of("value1")).build(),
						Attribute.builder("testId23").includeInResult(true).value(XacmlTypes.STRING.of("value2")).build(),
						Attribute.builder("testId24").includeInResult(true).issuer("testIssuer").value(XacmlTypes.STRING.of("value2")).build()).build())
				.build();

	}

	@Test
	public void testHasRepeatingCategories()
	{
		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0)
				.build();
		assertFalse(request.containsRepeatingCategories());
		request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0, resource1)
				.build();
		assertTrue(request.containsRepeatingCategories());
	}


	@Test
	public void testCreateRequest()
	{

		RequestContext request1 = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0, resource1)
				.build();
		assertFalse(request1.isReturnPolicyIdList());
		assertEquals(3, request1.getCategories().size());
		assertTrue(request1.getCategory(CategoryId.RESOURCE).contains(resource0));
		assertTrue(request1.getCategory(CategoryId.RESOURCE).contains(resource1));
		assertTrue(request1.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject0));

		RequestContext request2 = RequestContext
				.builder()
				.returnPolicyIdList(true)
				.attributes(subject0, resource0, resource1)
				.build();

		assertTrue(request2.isReturnPolicyIdList());
		assertTrue(request1.getCategory(CategoryId.RESOURCE).contains(resource0));
		assertTrue(request1.getCategory(CategoryId.RESOURCE).contains(resource1));
		assertTrue(request1.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject0));
	}

	@Test
	public void testGetAttributesByCategory()
	{

		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0, resource1)
				.build();
		Collection<Category> attr = request.getCategory(CategoryId.RESOURCE);
		assertEquals(2, attr.size());
		assertTrue(attr.contains(resource0));
		assertTrue(attr.contains(resource1));
		attr = request.getCategory(CategoryId.ENVIRONMENT);
		assertEquals(0, attr.size());
	}

	@Test
	public void testGetAttributeByCategory()
	{

		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0)
				.build();
		Collection<Category> attr = request.getCategory(CategoryId.ACTION);
		assertNotNull(attr);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetOnlyAttributesMultipleInstancesOfTheSameCategory()
	{
		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0, resource1)
				.build();
		request.getOnlyAttributes(CategoryId.RESOURCE);
	}

	@Test
	public void testGetOnlyAttributeSingleInstanceOfTheSameCategory()
	{
		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0)
				.build();
		Category attr = request.getOnlyAttributes(CategoryId.RESOURCE);
		assertEquals(resource0, attr);
	}

	@Test
	public void testGetRequestDefaults(){
		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0, resource1)
				.build();
		assertNotNull(request.getRequestDefaults());
		assertEquals(XPathVersion.XPATH1, request.getRequestDefaults().getXPathVersion());
	}

	@Test
	public void testGetIncludeInResult()
	{

		RequestContext request0 = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, resource0)
				.build();

		assertEquals(0, request0.getIncludeInResultAttributes().size());

		RequestContext request1 = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, subject1, resource0, resource1)
				.build();

		assertEquals(2, request1.getIncludeInResultAttributes().size());

		RequestContext request2 = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject0, subject1, resource0, resource1)
				.build();
		assertEquals(2, request2.getIncludeInResultAttributes().size());
	}
}
