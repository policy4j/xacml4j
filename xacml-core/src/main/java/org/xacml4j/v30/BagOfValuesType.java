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
 * Represents a XACML bag of attributes type.
 *
 * @author Giedrius Trumpickas
 */
public final class BagOfValuesType implements ValueTypeInfo
{
	private static final long serialVersionUID = 1317103379388105997L;

	private ValueType type;

	/**
	 * Constructs bag of attributes types with a given
	 * attribute type.
	 *
	 * @param type an attribute type
	 */
	public BagOfValuesType(ValueType type){
		Preconditions.checkNotNull(type);
		this.type = type;
	}

	/**
	 * Gets bag attribute type.
	 *
	 * @return bag attribute type
	 */
	public ValueType getDataType(){
		return type;
	}

	@Override
	public boolean isBag() {
		return true;
	}

	public ValueTypeInfo toBag(){
		return this;
	}

	public BagOfValues.Builder builder(){
		return new BagOfValues.Builder(this);
	}

	public BagOfValues emptyBag(){
		// TODO: optimized version, we need to cache empty bag defaultProvider
		return builder().build();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof BagOfValuesType)){
			return false;
		}
		BagOfValuesType bt = (BagOfValuesType)o;
		return type.equals(bt.type);
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(type);
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("TypeId", type.getDataTypeId())
		.toString();
	}
}
