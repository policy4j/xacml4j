package org.xacml4j.evidence;

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

import java.nio.file.Files;
import java.nio.file.Path;
import org.xacml4j.util.DateTimeDurationUtils;
import org.xacml4j.v30.policy.function.impl.DateTimeArithmeticFunctions;
import org.xacml4j.v30.types.ISO8601Date;
import org.xacml4j.v30.types.ISO8601DateTime;
import org.xacml4j.v30.types.ISO8601Duration;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;
import org.xacml4j.v30.types.ISO8601YearMonthDuration;

/** Executes the seven unmodified a8a90d46 arithmetic classes in isolation. */
public final class HistoricalArithmeticProbe
{
    public static void main(String[] args) throws Exception
    {
        for (Class<?> type : new Class<?>[] {DateTimeDurationUtils.class,
                DateTimeArithmeticFunctions.class, ISO8601Date.class, ISO8601DateTime.class,
                ISO8601Duration.class, ISO8601DayTimeDuration.class, ISO8601YearMonthDuration.class}) {
            Path loadedFrom = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!loadedFrom.equals(Path.of(args[1]))) {
                throw new IllegalStateException("Historical class loaded from the wrong classpath entry: " + type.getName());
            }
        }
        System.out.println("case_row,function,input,duration,current_expected,zone,a8a90d46_actual");
        for (String line : Files.readAllLines(Path.of(args[0])).subList(1, 185)) {
            String[] row = line.split(",", -1);
            String result;
            try {
                String operation = row[1];
                if (operation.startsWith("dateTime-")) {
                    ISO8601DateTime input = ISO8601DateTime.of(row[2]);
                    if (operation.endsWith("dayTimeDuration")) {
                        ISO8601DayTimeDuration duration = ISO8601DayTimeDuration.of(row[3]);
                        result = (operation.contains("-add-")
                                ? DateTimeArithmeticFunctions.add(input, duration)
                                : DateTimeArithmeticFunctions.subtract(input, duration)).toXacmlString();
                    } else {
                        ISO8601YearMonthDuration duration = ISO8601YearMonthDuration.of(row[3]);
                        result = (operation.contains("-add-")
                                ? DateTimeArithmeticFunctions.add(input, duration)
                                : DateTimeArithmeticFunctions.subtract(input, duration)).toXacmlString();
                    }
                } else {
                    ISO8601Date input = ISO8601Date.of(row[2]);
                    ISO8601YearMonthDuration duration = ISO8601YearMonthDuration.of(row[3]);
                    result = (operation.contains("-add-")
                            ? DateTimeArithmeticFunctions.add(input, duration)
                            : DateTimeArithmeticFunctions.subtract(input, duration)).toXacmlString();
                }
            } catch (RuntimeException exception) {
                result = "ERROR:" + exception.getClass().getName();
            }
            System.out.println(line + "," + result);
        }
    }
}
