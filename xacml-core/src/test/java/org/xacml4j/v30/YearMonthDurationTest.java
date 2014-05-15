package org.xacml4j.v30;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Before;
import org.junit.Test;

public class YearMonthDurationTest
{
	private DatatypeFactory df;

	@Before
	public void init() throws Exception{
		this.df = DatatypeFactory.newInstance();
	}

	@Test
	public void testFromXacmlString()
	{
		YearMonthDuration v1 = YearMonthDuration.create("-P1Y2M");
		YearMonthDuration v2 = YearMonthDuration.create("-P1Y2M");
		assertEquals("-P1Y2M", v1.toString());
		assertEquals(v1, v2);
		assertEquals(1, v1.getYears());
		assertEquals(2, v1.getMonths());
	}

	@Test
	public void createFromJavaDuration()
	{
		Duration d = df.newDuration("-P1Y2M");
		YearMonthDuration v1 = YearMonthDuration.create(d);
		YearMonthDuration v2 = YearMonthDuration.create("-P1Y2M");
		assertEquals(v1, v2);
		assertEquals(1, v1.getYears());
		assertEquals(2, v1.getMonths());
	}
}

