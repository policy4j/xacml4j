package org.xacml4j.jmh.testing;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The lines of a fixture failure message: a header, then one line per
 * failing request ({@code request <id>: ...}) or lookup
 * ({@code lookup <label>: ...}).
 */
public final class FailureLines
{
	private final List<String> lines;

	private FailureLines(List<String> lines){
		this.lines = lines;
	}

	public static FailureLines of(Throwable failure){
		List<String> lines = new ArrayList<>();
		for(String line : String.valueOf(failure.getMessage()).split("\n")){
			lines.add(line.trim());
		}
		return new FailureLines(List.copyOf(lines));
	}

	/** @return the line about the request with this id */
	public Optional<String> request(String id){
		return first("request " + id + ":");
	}

	/** @return the lines about requests */
	public List<String> requests(){
		return starting("request ");
	}

	/** @return the lines about lookups */
	public List<String> lookups(){
		return starting("lookup ");
	}

	public List<String> all(){
		return lines;
	}

	private Optional<String> first(String prefix){
		for(String line : lines){
			if(line.startsWith(prefix)){
				return Optional.of(line);
			}
		}
		return Optional.empty();
	}

	private List<String> starting(String prefix){
		List<String> found = new ArrayList<>();
		for(String line : lines){
			if(line.startsWith(prefix)){
				found.add(line);
			}
		}
		return found;
	}
}
