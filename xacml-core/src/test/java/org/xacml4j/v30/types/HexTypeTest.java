package org.xacml4j.v30.types;

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

import org.junit.Test;
import org.xacml4j.v30.BagOfAttributeExp;


public class HexTypeTest
{
	
	@Test
	public void testCreateValueFromBinaryArray()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		HexBinaryExp value1 = HexBinaryExp.valueOf(v0);
		HexBinaryExp value2 = HexBinaryExp.valueOf(v1);
		assertEquals(value1, value2);
	}

	@Test
	public void testCreateValueFromString()
	{
		byte[] data = {0, 1, 3, 5};
		HexBinaryExp value1 = HexBinaryExp.valueOf("00010305");
		HexBinaryExp value2 = HexBinaryExp.valueOf(data);
		assertEquals(value1, value2);
		BagOfAttributeExp bag = HexBinaryExp.bag().value(data, "00010305").build();
		assertEquals(XacmlTypes.HEXBINARY, bag.getDataType());
	}
}
