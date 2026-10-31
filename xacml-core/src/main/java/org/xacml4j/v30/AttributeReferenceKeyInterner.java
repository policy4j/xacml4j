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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Interners;

/**
 * Interns the keys that the attribute reference key builders build (design D21).
 * Implementations are thread-safe and register with
 * {@code @AutoService(AttributeReferenceKeyInterner.class)}; with none registered,
 * the builders use {@link #weak()}.
 */
@FunctionalInterface
public interface AttributeReferenceKeyInterner
{
	/**
	 * Returns the given key or an equal key of the same class
	 *
	 * @param key a key
	 * @return the given key or an equal key of the same class
	 */
	AttributeReferenceKey intern(AttributeReferenceKey key);

	/**
	 * Creates an interner that keeps one live instance per equal key and holds keys weakly
	 *
	 * @return Guava's weak interner
	 */
	static AttributeReferenceKeyInterner weak(){
		return Interners.<AttributeReferenceKey>newWeakInterner()::intern;
	}

	/**
	 * Creates an interner that returns its argument
	 *
	 * @return a pass-through interner
	 */
	static AttributeReferenceKeyInterner none(){
		return key -> key;
	}

	/**
	 * Gets the interner the key builders use
	 *
	 * @return the registered interner, else {@link #weak()}
	 */
	static AttributeReferenceKeyInterner get(){
		return AttributeReferenceKey.INTERNER;
	}
}
