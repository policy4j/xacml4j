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

import java.io.Serializable;

/**
 * A marker interface for XACML value types
 *
 * @author Giedrius Trumpickas
 */
public interface ValueExpTypeInfo extends Serializable
{
	/**
	 * Test if given type represents a XACML bag type
	 *
	 * @return {@code true} if this type represents XACML bag type
	 */
	boolean isBag();

	/**
	 * Gets XACML data type for
	 * values of this type
	 */
	ValueType getValueType();

	/**
	 * Converts this value type to bag type
	 *
	 * @param <T>
	 * @return bag type
	 */
	<T extends ValueExpTypeInfo> T toBagType();
}
