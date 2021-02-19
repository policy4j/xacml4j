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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import org.xacml4j.v30.AttributeValueType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A XACML attribute descriptor
 *
 * @author Giedrius Trumpickas
 */
public final class AttributeDescriptor
{
	private String attributeId;
	private AttributeValueType dataType;
	private Set<String> aliasSet;

	private AttributeDescriptor(String attributeId,
			AttributeValueType dataType, Collection<String> aliases)
	{
		Preconditions.checkNotNull(attributeId, "attributeId");
		Preconditions.checkNotNull(dataType, "dataType");
		Preconditions.checkNotNull(aliases, "aliases");
		this.attributeId = attributeId;
		this.dataType = dataType;
		this.aliasSet = ImmutableSortedSet
				.orderedBy(String.CASE_INSENSITIVE_ORDER)
				.addAll(aliases)
				.build();
	}

	public static AttributeDescriptor of(String attributeId,
										 AttributeValueType type, Collection<String> alias){
		return new AttributeDescriptor(attributeId, type, alias);
	}

	public static AttributeDescriptor of(String attributeId,
										 AttributeValueType type, String ...alias){
		return new AttributeDescriptor(attributeId, type,
				alias != null?Arrays.asList(alias):Collections.emptySet());
	}

	/**
	 * Gets an attribute identifier
	 *
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}

	/**
	 * Tests if given identifier is this attribute alias
	 *
	 * @param id an identifier
	 * @return true if given identifier is an alias
	 * for this attribute
	 */
	public boolean isAlias(String id){
		return aliasSet.contains(id);
	}

	/**
	 * Gets attribute id aliases, returned collection contains {@link #getAttributeId()}
	 *
	 * @return attribute id aliases, returned collection contains {@link #getAttributeId()}
	 */
	public Set<String> getAliases(){
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


