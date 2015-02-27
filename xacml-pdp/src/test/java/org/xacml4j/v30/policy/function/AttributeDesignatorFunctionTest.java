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

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.*;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.AnnotationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.*;
import org.xacml4j.v30.xpath.XPathProvider;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

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
	private XPathProvider xpathProvider;
	private FunctionProvider provider;
	private IMocksControl c;
	private Node content;
	private EntityExp entity;

	@Before
	public void init() throws Exception{
		this.c = createControl();
		this.context = c.createMock(EvaluationContext.class);
		this.content = DOMUtil.stringToNode(TEST_XML);
		this.provider = new AnnotationBasedFunctionProvider(AttributeDesignatorFunctions.class);
		this.entity = EntityExp.of(
				Entity.builder()
				.content(content)
				.attribute(Attribute
						.builder("testId")
						.value(StringExp.of("aaaa"))
						.build())
				.build());
	}

	@Test
	public void testDesignatorFunctionWithCategoryId(){
		FunctionSpec func = provider.getFunction("urn:oasis:names:tc:xacml:3.0:function:category-designator");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.resolve(AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.dataType(XacmlTypes.STRING)
				.attributeId("testId")
				.build())).andReturn(StringExp.of("aaaa").toBag());
		c.replay();
		BagOfAttributeExp v = func.invoke(context,
				AnyURIExp.of(Categories.SUBJECT_ACCESS.getName()),
				AnyURIExp.of("testId"),
				AnyURIExp.of(XacmlTypes.STRING.getDataTypeId()),
				BooleanExp.valueOf(false),
				null);
		assertEquals(StringExp.of("aaaa").toBag(), v);
		c.verify();
	}

	@Test
	public void testDesignatorFunctionWithEntity(){
		FunctionSpec func = provider.getFunction("urn:oasis:names:tc:xacml:3.0:function:category-designator");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		c.replay();
		BagOfAttributeExp v = func.invoke(context,
				entity,
				AnyURIExp.of("testId"),
				AnyURIExp.of(XacmlTypes.STRING.getDataTypeId()),
				BooleanExp.valueOf(false),
				null);
		assertEquals(StringExp.of("aaaa").toBag(), v);
		c.verify();
	}
}
