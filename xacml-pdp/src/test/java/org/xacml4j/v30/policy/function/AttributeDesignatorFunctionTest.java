package org.xacml4j.v30.policy.function;

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

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.EntityValue;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Optional;

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
	private static FunctionProvider provider = FunctionProviderBuilder.builder().withStandardFunctions().build();
	private IMocksControl c;
	private EntityValue entity;
	private FunctionSpec func;

	@Before
	public void init() throws Exception{
		this.c = createControl();
		this.func = provider.getFunction("urn:oasis:names:tc:xacml:3.0:function:attribute-designator").orElse(null);
		assertNotNull(func);
		this.context = c.createMock(EvaluationContext.class);

		this.entity = XacmlTypes.ENTITY.of(
				Entity.builder()
				      .content(XmlContent.of(XmlContent.fromString(TEST_XML)))
				      .attribute(Attribute
						.builder("testId")
						.value(XacmlTypes.STRING.of("aaaa"))
						.build())
				      .build());
	}

	@Test
	public void testDesignatorFunctionWithCategoryId(){

		AttributeDesignatorKey key = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.dataType(XacmlTypes.STRING)
				.attributeId("testId")
				.build();
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		Capture<AttributeDesignatorKey> keyCapture = Capture.newInstance();
		expect(context.resolve(capture(keyCapture)))
				.andReturn(Optional.of(XacmlTypes.STRING.of("aaaa")
				                                        .toBag()));
		c.replay();
		BagOfValues v = func.invoke(context,
		                            XacmlTypes.ANYURI.of(CategoryId.SUBJECT_ACCESS.getId()),
		                            XacmlTypes.ANYURI.of("testId"),
		                            XacmlTypes.ANYURI.of(XacmlTypes.STRING.getDataTypeId()),
		                            XacmlTypes.BOOLEAN.of(false),
		                            null);
		assertEquals(XacmlTypes.STRING.of("aaaa").toBag(), v);
		assertEquals(key, keyCapture.getValue());
		c.verify();
	}

	@Test
	public void testDesignatorFunctionWithEntity(){
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		c.replay();
		BagOfValues v = func.invoke(context,
		                            entity,
		                            XacmlTypes.ANYURI.of("testId"),
		                            XacmlTypes.ANYURI.of(XacmlTypes.STRING.getDataTypeId()),
		                            XacmlTypes.BOOLEAN.of(false),
		                            null);
		assertEquals(XacmlTypes.STRING.of("aaaa").toBag(), v);
		c.verify();
	}
}
