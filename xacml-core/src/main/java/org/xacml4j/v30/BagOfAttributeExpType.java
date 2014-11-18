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

import java.util.Arrays;
import java.util.Collections;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Represents a XACML bag of category type.
 *
 * @author Giedrius Trumpickas
 */
public final class BagOfAttributeExpType implements ValueType
{
	private static final long serialVersionUID = 1317103379388105997L;



	private AttributeExpType type;
    private BagOfAttributeExp emptyBag;
	/**
	 * Constructs bag of category types with a given
	 * category type.
	 *
	 * @param type an category type
	 */
	public BagOfAttributeExpType(AttributeExpType type){
		Preconditions.checkNotNull(type);
		this.type = type;
        this.emptyBag = new BagOfAttributeExp(this,
                ImmutableList.<AttributeExp>of());
	}

	/**
	 * Gets bag category type.
	 *
	 * @return bag category type
	 */
	public AttributeExpType getDataType(){
		return type;
	}

	@Override
	public boolean isBag() {
		return true;
	}

	public BagOfAttributeExp.Builder builder(){
		return new BagOfAttributeExp.Builder(type);
	}


	/**
	 * Creates an empty bag.
	 *
	 * @return instance of {@link BagOfAttributeExp} with
	 * no {@link BagOfAttributeExp} instances
	 */
	public  BagOfAttributeExp emptyBag(){
		return emptyBag;
	}

	/**
	 * Creates bag from given array of category.
	 *
	 * @param attr an array of category
	 * @return {@link BagOfAttributeExp} containing given category
	 */
	public BagOfAttributeExp value(AttributeExp... attr){
		return new BagOfAttributeExp(this, Arrays.asList(attr));
	}

    /**
     * Creates bag from given collection of category.
     *
     * @param attr a collection of category
     * @return {@link BagOfAttributeExp} containing given category
     */
    public BagOfAttributeExp of(
            Iterable<? extends AttributeExp> attr){
        return new BagOfAttributeExp(this, attr);
    }


    @Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof BagOfAttributeExpType)){
			return false;
		}
		BagOfAttributeExpType bt = (BagOfAttributeExpType)o;
		return type.equals(bt.type);
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(type);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("DataType", type.getDataTypeId())
		.toString();
	}
}
