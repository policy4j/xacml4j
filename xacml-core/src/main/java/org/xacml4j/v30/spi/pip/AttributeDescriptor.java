package org.xacml4j.v30.spi.pip;

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

import org.xacml4j.v30.AttributeValueType;
import org.xacml4j.v30.BagOfAttributeValues;

import com.google.common.base.Preconditions;

import java.util.*;

public final class AttributeDescriptor
{
	private String attributeId;
	private AttributeValueType dataType;
	private Optional<BagOfAttributeValues> defaultValue = Optional.empty();
	private Set<String> aliasSet;
	/**
	 * Constructs attribute descriptor
	 *
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 */
	public AttributeDescriptor(String attributeId,
			AttributeValueType dataType, String ...aliases)
	{
		Preconditions.checkArgument(attributeId != null);
		Preconditions.checkArgument(dataType != null);
		this.attributeId = attributeId;
		this.dataType = dataType;
		this.aliasSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		this.aliasSet.addAll(Arrays.asList(aliases));
		this.aliasSet = Collections.unmodifiableSet(aliasSet);
	}

	/**
	 * Gets an attribute identifier
	 *
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}

	public boolean isAlias(String id){
		return aliasSet.contains(id);
	}

	/**
	 * Gets attribute id aliases, returned collection contains {@link #getAttributeId()}
	 *
	 * @return attribute id aliases, returned collection contains {@link #getAttributeId()}
	 */
	public Collection<String> getAliases(){
		return aliasSet;
	}

	/**
	 * Gets expected attribute data type
	 *
	 * @return {@link AttributeValueType} an
	 * attribute data type
	 */
	public AttributeValueType getDataType(){
		return dataType;
	}
}


