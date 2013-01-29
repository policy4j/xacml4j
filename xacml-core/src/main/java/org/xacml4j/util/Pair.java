package org.xacml4j.util;

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
	
	public A getFirst(){
		return first;
	}
	
	public B getSecond(){
		return second;
	}
}
