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
import static org.junit.Assert.*;

import org.junit.Test;


public class VersionMatchTest
{
	@Test
	public void testMatchEquals() throws XacmlSyntaxException
	{
		VersionMatch m =  VersionMatch.parse("1.2.*.2.+");
		assertThat(m.isEqual(Version.parse("1.2.1")), is(false));
        assertThat(m.isEqual(Version.parse("1.2.1.2")), is(true));
        assertThat(m.isEqual(Version.parse("1.2.1.2.1.2")), is(true));
	}

    @Test
    public void testIsLaterWildCards() throws XacmlSyntaxException
    {
        VersionMatch m = VersionMatch.parse("1.2.*.2.+");

        assertThat(m.isLaterThan(Version.parse("1.1.2")), is(true));
        assertThat(m.isLaterThan(Version.parse("1.1.2.1.2.3")), is(true));

        assertThat(m.isLaterThan(Version.parse("1.3.2")), is(false));
        assertThat(m.isLaterThan(Version.parse("1.3.2.2.3.4.5")), is(false));
    }

    @Test
    public void testIsEarlierWildCards() throws XacmlSyntaxException
    {
        VersionMatch m = VersionMatch.parse("1.2.*.2.+");

        assertThat(m.isEarlierThan(Version.parse("1.2.2.3")), is(true));
        assertThat(m.isEarlierThan(Version.parse("1.1.2")), is(false));
    }

    @Test
    public void testIsLaterNoWildcards() throws XacmlSyntaxException
    {
        VersionMatch m = VersionMatch.parse("1.2.3.2");

        assertThat(m.isLaterThan(Version.parse("1.1.2")), is(true));

        assertThat(m.isLaterThan(Version.parse("1.2.3.2.1")), is(false));
        assertThat(m.isLaterThan(Version.parse("1.2.3.2.0.0")), is(true));

    }

    @Test
    public void testIssueInPolicyIndexTestCase() throws XacmlSyntaxException
    {
        VersionMatch earlier = VersionMatch.parse("1.1.0");
        VersionMatch later = VersionMatch.parse("1.3.0");
        Version v = Version.parse("1.2.1");
        assertTrue(earlier.isEarlierThan(v));
        assertTrue(later.isLaterThan(v));

    }
}
