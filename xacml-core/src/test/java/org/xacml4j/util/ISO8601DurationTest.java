package org.xacml4j.util;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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


import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;
import org.xacml4j.v30.types.ISO8601YearMonthDuration;

import javax.xml.datatype.Duration;

import java.time.Period;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ISO8601DurationTest
{
    @Test
    public void testParseISO8601YearsMonths(){
        ISO8601YearMonthDuration p = ISO8601YearMonthDuration.of("P10Y13M");
        assertEquals(11, p.getYears());
        assertEquals(1, p.getMonths());
        assertEquals("P11Y1M", p.toXacmlString());

        ISO8601YearMonthDuration pn = ISO8601YearMonthDuration.of("-P10Y13M");
        assertEquals(11, pn.getYears());
        assertEquals(1, pn.getMonths());
        assertTrue(pn.isNegative());
        assertEquals("-P11Y1M", pn.toXacmlString());
        assertTrue(p.plus(pn).isZero());
    }

    @Test
    public void testParseISO8601DayTime(){
        ISO8601DayTimeDuration p = ISO8601DayTimeDuration.of("P10DT25H");
        assertEquals(1, p.getHours());
        assertEquals(11, p.getDays());
        assertEquals("P11DT1H0M0S", p.toXacmlString());

        ISO8601DayTimeDuration pn = ISO8601DayTimeDuration.of("-P10DT25H");
        assertEquals(1, pn.getHours());
        assertEquals(11, pn.getDays());
        assertEquals("-P11DT1H0M0S", pn.toXacmlString());
        assertTrue(p.plus(pn).isZero());
    }
}
