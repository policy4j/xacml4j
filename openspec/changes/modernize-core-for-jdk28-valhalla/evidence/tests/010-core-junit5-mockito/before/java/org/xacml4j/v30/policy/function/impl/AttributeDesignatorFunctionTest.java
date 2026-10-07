package org.xacml4j.v30.policy.function.impl;

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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import com.google.common.truth.Truth8;
import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

public class AttributeDesignatorFunctionTest
{


	private final static String TEST_XML = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"xmlns:md=\"urn:example:med:schemas:record\">" +
			"<md:patient>" +
			"<md:patientDoB>1992-03-21</md:patientDoB>" +
			"<md:patient-number>555555</md:patient-number>" +
			"</md:patient>" +
			"</md:record>";

	private EvaluationContext context;
	private IMocksControl c;
	private Entity entity;

	@Before
	public void init() throws Exception{
		this.c = createControl();
		this.context = c.createMock(EvaluationContext.class);

		this.entity = Entity.builder()
				      .content(XmlContent.of(XmlContent.fromString(TEST_XML)))
				      .attribute(Attribute
						.builder("testId")
						.value(XacmlTypes.STRING.ofAny("aaaa"))
						.build()).build();
	}

	@Test
	public void testDesignatorFunctionWithCategoryId()
	{
		expect(context.resolveEntity(CategoryId.SUBJECT_ACCESS)).andReturn(Optional.of(entity));
		c.replay();
		BagOfValues v = AttributeDesignatorFunctions.designator(context,
		                            XacmlTypes.ANYURI.ofAny(CategoryId.SUBJECT_ACCESS.getId()),
		                            XacmlTypes.ANYURI.ofAny("testId"),
		                            XacmlTypes.ANYURI.ofAny(XacmlTypes.STRING.getTypeId()),
		                            XacmlTypes.BOOLEAN.ofAny(false),
		                            null);
		assertEquals(XacmlTypes.STRING.ofAny("aaaa").toBag(), v);
		c.verify();
	}

	@Test
	public void testDesignatorFunctionWithEntity(){
		c.replay();
		Truth8.assertThat(entity.resolve("testId", XacmlTypes.STRING, null)).hasValue(StringVal.of("aaaa").toBag());
		BagOfValues v = AttributeDesignatorFunctions.designator(context,
		                            entity,
		                            XacmlTypes.ANYURI.ofAny("testId"),
		                            XacmlTypes.ANYURI.ofAny(XacmlTypes.STRING.getTypeId()),
		                            XacmlTypes.BOOLEAN.ofAny(false),
		                            null);
		assertEquals(XacmlTypes.STRING.ofAny("aaaa").toBag(), v);
		c.verify();
	}
}
