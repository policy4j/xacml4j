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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

/** Finds service implementations by name. */
final class Providers
{
	private Providers(){
	}

	/**
	 * @return the implementation of {@code type} whose name is {@code name}
	 * @throws IllegalArgumentException if none has that name; the message lists the known names
	 * @throws IllegalStateException if several have that name; the message names their classes
	 */
	static <T> T find(Class<T> type, Function<T, String> nameOf, String kind, String name){
		List<T> matches = new ArrayList<>();
		for(T provider : ServiceLoader.load(type)){
			if(nameOf.apply(provider).equals(name)){
				matches.add(provider);
			}
		}
		if(matches.isEmpty()){
			throw new IllegalArgumentException(String.format(
					"Unknown %s \"%s\"; known %ss: %s", kind, name, kind, names(type, nameOf)));
		}
		if(matches.size() > 1){
			List<String> classes = new ArrayList<>();
			for(T match : matches){
				classes.add(match.getClass().getName());
			}
			Collections.sort(classes);
			throw new IllegalStateException(String.format(
					"Several %ss are named \"%s\": %s", kind, name, classes));
		}
		return matches.get(0);
	}

	/** @return the sorted names of all implementations of {@code type} */
	static <T> SortedSet<String> names(Class<T> type, Function<T, String> nameOf){
		SortedSet<String> names = new TreeSet<>();
		for(T provider : ServiceLoader.load(type)){
			names.add(nameOf.apply(provider));
		}
		return Collections.unmodifiableSortedSet(names);
	}
}
