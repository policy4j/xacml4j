package com.artagon.xacml.v30.pdp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v30.Decision;

public class DecisionTest 
{
	@Test
	public void testParseValidValue()
	{
		assertEquals(Decision.DENY, Decision.parse(Decision.DENY.toString()));
		assertEquals(Decision.INDETERMINATE, Decision.parse(Decision.INDETERMINATE.toString()));
		assertEquals(Decision.NOT_APPLICABLE, Decision.parse(Decision.NOT_APPLICABLE.toString()));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParseNotValidValue()
	{
		Decision.parse("AAA");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParseNullValue()
	{
		Decision.parse(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParseEmptyValue()
	{
		Decision.parse("");
	}
}
