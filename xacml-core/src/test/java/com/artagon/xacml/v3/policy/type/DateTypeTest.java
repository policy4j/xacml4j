package com.artagon.xacml.v3.policy.type;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.DateType;
import com.artagon.xacml.v3.types.DateType.DateValue;

public class DateTypeTest 
{
	private DateType t1;
	
	@Before
	public void init() throws Exception{
		this.t1 = XacmlDataTypes.DATE.getType();
	}
	
	@Test
	public void testFromXacmlString(){
		DateValue value = t1.fromXacmlString("2002-09-24Z");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(9, value.getValue().getMonth());
		assertEquals(24, value.getValue().getDay());
		assertEquals("2002-09-24Z", value.toXacmlString());
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		t1.fromXacmlString("2002-05-30T09:30:10-06:00");
	}
}

