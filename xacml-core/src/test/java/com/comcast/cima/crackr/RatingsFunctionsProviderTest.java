package com.comcast.cima.crackr;


import static com.comcast.cima.crackr.RatingsFunctionsProvider.isGreaterThanMPAA;
import static com.comcast.cima.crackr.RatingsFunctionsProvider.isGreaterThanVChip;
import static com.comcast.cima.crackr.RatingsFunctionsProvider.MPAARatings.NC17;
import static com.comcast.cima.crackr.RatingsFunctionsProvider.MPAARatings.PG;
import static com.comcast.cima.crackr.RatingsFunctionsProvider.VChipRatings.TV_G;
import static com.comcast.cima.crackr.RatingsFunctionsProvider.VChipRatings.TV_MA;
import static com.comcast.cima.crackr.RatingsFunctionsProvider.VChipRatings.TV_Y;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class RatingsFunctionsProviderTest 
{
	@Test
	public void testMPAALessThanOrEquals()
	{
		StringValue a = XacmlDataTypes.STRING.create(PG.toString());
		StringValue b = XacmlDataTypes.STRING.create(NC17.toString());
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), isGreaterThanMPAA(a, b));
		
		a = XacmlDataTypes.STRING.create(NC17.toString());
		b = XacmlDataTypes.STRING.create(PG.toString());
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), isGreaterThanMPAA(a, b));
		
		a = XacmlDataTypes.STRING.create("pg");
		b = XacmlDataTypes.STRING.create("Pg");
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), isGreaterThanMPAA(a, b));
	}
	
	@Test
	public void testVChipLessThanOrEquals()
	{
		StringValue a = XacmlDataTypes.STRING.create(TV_Y.toString());
		StringValue b = XacmlDataTypes.STRING.create(TV_MA.toString());
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), isGreaterThanVChip(a, b));
		
		a = XacmlDataTypes.STRING.create(TV_MA.toString());
		b = XacmlDataTypes.STRING.create(TV_G.toString());
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), isGreaterThanVChip(a, b));
		
		a = XacmlDataTypes.STRING.create("Tv-Y");
		b = XacmlDataTypes.STRING.create("tv-ma");
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), isGreaterThanVChip(a, b));
	}
}
