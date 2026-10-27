package org.xacml4j.jmh.harness;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import java.util.Objects;
import java.util.Optional;

/**
 * A benchmark parameter value of the form {@code name} or
 * {@code name:argument}.
 *
 * @param name the text before the first colon
 * @param argument the text after the first colon, if there is a colon
 */
public record Selection(String name, Optional<String> argument)
{
	public Selection {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(argument, "argument");
	}

	/**
	 * @param value {@code name} or {@code name:argument}; the argument may hold colons
	 * @return the parsed selection
	 * @throws IllegalArgumentException if the name is blank
	 */
	public static Selection parse(String value){
		Objects.requireNonNull(value, "value");
		int colon = value.indexOf(':');
		String name = (colon < 0 ? value : value.substring(0, colon)).trim();
		if(name.isEmpty()){
			throw new IllegalArgumentException("No name in \"" + value + "\"; expected name or name:argument");
		}
		return new Selection(name, colon < 0 ? Optional.empty() : Optional.of(value.substring(colon + 1)));
	}
}
