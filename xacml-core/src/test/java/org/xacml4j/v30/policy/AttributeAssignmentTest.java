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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.XacmlTypes;


public class AttributeAssignmentTest
{
	@Test
	public void hashKeepsTheFieldOrderWithAndWithoutOptionalFields()
	{
		for(CategoryId category : new CategoryId[]{null, CategoryId.ACTION}){
			for(String issuer : new String[]{null, "issuer"}){
				AttributeAssignment assignment = AttributeAssignment.builder("testId")
						.category(category).issuer(issuer).value(XacmlTypes.INTEGER.ofAny(10)).build();
				AttributeAssignment copy = AttributeAssignment.builder("testId")
						.category(category).issuer(issuer).value(XacmlTypes.INTEGER.ofAny(10)).build();
				assertEquals(assignment, copy);
				assertEquals(Arrays.hashCode(new Object[]{"testId", category, XacmlTypes.INTEGER.ofAny(10), issuer}),
						assignment.hashCode());
				assertEquals(assignment.hashCode(), copy.hashCode());
				assertNotEquals(assignment, AttributeAssignment.builder("testId")
						.category(category).issuer(issuer).value(XacmlTypes.INTEGER.ofAny(11)).build());
			}
		}
	}

	@Test
	public void testCreateAndEquals()
	{
		AttributeAssignment a0 =  AttributeAssignment.builder()
				.attributeId("testId")
				.category(CategoryId.ACTION)
				.value(XacmlTypes.INTEGER.ofAny(10))
				.build();
		assertEquals("testId", a0.getAttributeId());
		assertEquals(CategoryId.ACTION, a0.getCategory().get());
		assertEquals(XacmlTypes.INTEGER.ofAny(10), a0.getAttribute());
		AttributeAssignment a1 =  AttributeAssignment.builder()
				.attributeId("testId")
				.category(CategoryId.ACTION)
				.value(XacmlTypes.INTEGER.ofAny(10))
				.build();
		assertEquals(a0, a1);
	}
}
