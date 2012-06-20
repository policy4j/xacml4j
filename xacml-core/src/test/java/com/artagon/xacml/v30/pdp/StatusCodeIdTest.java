package com.artagon.xacml.v30.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.artagon.xacml.v30.StatusCodeId;
import com.artagon.xacml.v30.StatusCodeIds;

public class StatusCodeIdTest 
{
	@Test
	public void testParseStatusCodeId()
	{
		StatusCodeId c = StatusCodeIds.parse(StatusCodeIds.OK.getId());
		assertSame(StatusCodeIds.OK, c);
		c = StatusCodeIds.parse("AAAA");
		assertEquals("AAAA", c.getId());
		assertEquals("AAAA", c.toString());
		
		StatusCodeId c1 = StatusCodeIds.parse("AAAA");
		StatusCodeId c2 = StatusCodeIds.parse("AAAA");
		assertEquals(c1, c2);
	}
}
