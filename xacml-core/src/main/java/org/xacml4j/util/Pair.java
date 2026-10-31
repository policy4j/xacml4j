package org.xacml4j.util;

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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents pair of objects
 *
 * @author Giedrius Trumpickas
 *
 * @param <A> a type of first object
 * @param <B> a type of second object
 */
public final class Pair <A, B>
{
	private A first;
	private B second;

	public Pair(A first, B second){
		Preconditions.checkNotNull(first);
		Preconditions.checkNotNull(second);
		this.first = first;
		this.second = second;
	}

	public static <F, S>  Pair<F, S> of(F a, S b) {
		return new Pair<>(a, b);
	}

	public A first(){
		return first;
	}

	public B second(){
		return second;
	}

	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("first", first)
				.add("second", second)
				.toString();
	}

	public int hashCode(){
		return Objects.hashCode(first, second);
	}

	public boolean equals(Object a){
		if(this == a){
			return true;
		}
		if(a == null ||
				!(a instanceof Pair)){
			return false;
		}
		Pair<A, B> pair = (Pair<A, B>)a;
		return java.util.Objects.equals(first, pair.first) &&
				java.util.Objects.equals(second, pair.second);
	}
}
