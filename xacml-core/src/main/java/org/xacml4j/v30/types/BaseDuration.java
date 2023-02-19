package org.xacml4j.v30.types;

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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.google.common.base.Preconditions;

public abstract class BaseDuration <T extends BaseDuration<?>>
	implements Comparable<T>, Externalizable
{
	private static final long serialVersionUID = 6573551346951236604L;

	private Duration value;

	protected static DatatypeFactory df = null;

	static{
		try{
			df = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException ignored){
		}
	}

	protected BaseDuration(Duration value){
		Preconditions.checkNotNull(value);
		this.value = value;
	}

	protected static Duration parseDuration(Object any)
	{
		if(String.class.isInstance(any)){
			return df.newDuration((String)any);
		}
		return (Duration)any;
	}

	public final boolean isPositive(){
		return value.getSign() == 1;
	}

	public final boolean isNegative(){
		return value.getSign() == -1;
	}

	public final boolean isZero(){
		return value.getSign() == 0;
	}

	final Duration getDuration(){
		return value;
	}

	@Override
	public final String toString(){
		return value.toString();
	}

	public final String toXacmlString(){
		return value.toString();
	}

	@Override
	public final int hashCode(){
		return value.hashCode();
	}

	@Override
	public final boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof BaseDuration)){
			return false;
		}
		BaseDuration<?> d = (BaseDuration<?>)o;
		return value.equals(d.value);
	}

	@Override
	public final int compareTo(T o) {
		return value.compare(o.getDuration());
	}

	public final T add(T d){
		return makeDuration(getDuration().add(d.getDuration()));
	}

	public final T subtract(T d){
		return makeDuration(getDuration().subtract(d.getDuration()));
	}

	public final T negate(){
		return makeDuration(getDuration().negate());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(value);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.value = (Duration) in.readObject();
	}

	protected abstract T makeDuration(Duration d);



}
