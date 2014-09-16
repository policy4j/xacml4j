package org.xacml4j.v30;

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


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.XacmlSyntaxException;


public class VersionMatchTest
{
	@Test
	public void testMatchAnySubsequentialVersions() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.+");
		assertThat(m.getPattern(), is("1.+"));
		assertThat(m.match(Version.parse("1.2.1")), is(true));
		assertThat(m.match(Version.parse("1.1")), is(true));
		assertThat(m.match(Version.parse("2.1")), is(false));
		m = new VersionMatch("1.*.+");
		assertThat(m.match(Version.parse("1.0")), is(true));
	}

	@Test
	public void testMatchAnySingleNumber() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.*.1");
		assertThat(m.getPattern(), is("1.*.1"));
		assertThat(m.match(Version.parse("1.2.1")), is(true));
		assertThat(m.match(Version.parse("1.0.1")), is(true));
		assertThat(m.match(Version.parse("2.1.1")), is(false));
	}

	@Test(expected=XacmlSyntaxException.class)
	public void testCreateWithSubsquentialTwoTimes() throws XacmlSyntaxException
	{
		new VersionMatch("1.+.+");
	}

	@Test
	public void testMatchAnySingleNumberTwoTimesInTheRow() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.*.*.1");
		assertThat(m.getPattern(), is("1.*.*.1"));
		assertThat(m.match(Version.parse("1.2.1.1")), is(true));
		assertThat(m.match(Version.parse("1.2.1.1")), is(true));
		assertThat(m.match(Version.parse("1.0.0.1")), is(true));
		assertThat(m.match(Version.parse("1.0.1")), is(false));
	}

	@Test
	public void testCreateWithAnySingleDigitAndSubseq() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.*.+");
		assertThat(m.match(Version.parse("1.2.1")), is(true));
		assertThat(m.match(Version.parse("1.2.1.2")), is(true));
		assertThat(m.match(Version.parse("1.0.1")), is(true));
		assertThat(m.match(Version.parse("2.1.1")), is(false));
	}
}
