package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;

public final class AttributeDescriptor
{
	private String attributeId;
	private AttributeExpType dataType;
	private BagOfAttributeExp defaultValue;

	/**
	 * Constructs attribute descriptor
	 *
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 */
	public AttributeDescriptor(String attributeId,
			AttributeExpType dataType)
	{
		Preconditions.checkArgument(attributeId != null);
		Preconditions.checkArgument(dataType != null);
		this.attributeId = attributeId;
		this.dataType = dataType;
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
	 * Gets attribute default value
	 * @return attribute default value
	 */
	public BagOfAttributeExp getDefaultValue(){
		return defaultValue;
	}

	/**
	 * Gets expected attribute data type
	 *
	 * @return {@link AttributeExpType} an
	 * attribute data type
	 */
	public AttributeExpType getDataType(){
		return dataType;
	}
}


