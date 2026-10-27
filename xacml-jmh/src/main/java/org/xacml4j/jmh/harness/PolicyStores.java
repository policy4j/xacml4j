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

import java.util.SortedSet;

/** Finds {@link PolicyStore} implementations on the classpath by name. */
public final class PolicyStores
{
	private PolicyStores(){
	}

	/**
	 * @param value the {@code store} parameter: {@code name} or {@code name:argument}
	 * @return the store with that name and the argument
	 */
	public static SelectedStore select(String value){
		Selection selection = Selection.parse(value);
		return new SelectedStore(find(selection.name()), selection.argument());
	}

	/**
	 * @param name a store name
	 * @return the store with that name
	 * @throws IllegalArgumentException if no store has that name
	 * @throws IllegalStateException if several stores have that name
	 */
	public static PolicyStore find(String name){
		return Providers.find(PolicyStore.class, PolicyStore::name, "store", name);
	}

	/** @return the sorted names of the stores on the classpath */
	public static SortedSet<String> names(){
		return Providers.names(PolicyStore.class, PolicyStore::name);
	}
}
