package org.xacml4j.v30.policy.function.regex;

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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;

/**
 * Finds the regular-expression engines on the class path by name (change add-regex-engine-spi,
 * design D1): the {@link RegexEngineProvider} implementations that {@link ServiceLoader} lists.
 */
public final class RegexEngines
{
	private RegexEngines(){
	}

	/**
	 * Resolves an engine by name
	 *
	 * @param name the name of the engine
	 * @return a new engine of that name
	 * @throws IllegalArgumentException when no provider has the name; the message lists the known names
	 * @throws IllegalStateException when more than one provider has the name; the message names them
	 */
	public static RegexEngine engine(String name){
		Preconditions.checkNotNull(name, "name");
		List<RegexEngineProvider> matching = new ArrayList<>();
		Set<String> names = new TreeSet<>();
		for(RegexEngineProvider provider : providers()){
			names.add(provider.name());
			if(name.equals(provider.name())){
				matching.add(provider);
			}
		}
		if(matching.isEmpty()){
			throw new IllegalArgumentException(
					String.format("Unknown regular expression engine \"%s\"; the known engines are %s", name, names));
		}
		if(matching.size() > 1){
			List<String> providers = matching.stream().map(p -> p.getClass().getName()).toList();
			throw new IllegalStateException(
					String.format("More than one provider of regular expression engine \"%s\": %s", name, providers));
		}
		return matching.get(0).create();
	}

	/** The names of the engines on the class path, in their order */
	public static Set<String> names(){
		Set<String> names = new TreeSet<>();
		for(RegexEngineProvider provider : providers()){
			names.add(provider.name());
		}
		return names;
	}

	private static Iterable<RegexEngineProvider> providers(){
		return ServiceLoader.load(RegexEngineProvider.class, RegexEngines.class.getClassLoader());
	}
}
