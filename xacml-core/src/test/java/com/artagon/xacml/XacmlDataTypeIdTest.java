package com.artagon.xacml;

import org.junit.Test;

import com.artagon.xacml.DataTypeId;
import com.artagon.xacml.DataTypes;

import static org.junit.Assert.*;

public class XacmlDataTypeIdTest 
{
	@Test
	public void testEqualsDefaultXacmlTypes()
	{
		assertEquals(DataTypes.ANYURI, DataTypes.ANYURI);
		DataTypeId typeId = DataTypes.parse("http://www.w3.org/2001/XMLSchema#anyURI");
		assertEquals(DataTypes.ANYURI, typeId);
	}
	
	@Test
	public void testEqualsCustomXacmlTypes()
	{
		DataTypeId typeId1 = DataTypes.parse("testTypeId");
		DataTypeId typeId2 = DataTypes.parse("testTypeId");
		assertEquals(typeId1, typeId2);
	}
}
