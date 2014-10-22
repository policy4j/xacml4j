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

public interface CategoryId
{
	/**
	 * Gets XACML category identifier
	 *
	 * @return a XACML category identifier
	 */
	String getId();

	/**
	 * Gets category short name if its available,
	 * if short name is not available, value
	 * {@link this#getId()} is returned
	 * @return
	 */
	String getShortName();

	/**
	 * Test if this category is a default category.
	 * A default category is a category defined by
	 * XACML 3.0 specification
	 *
	 * @return <code>true<code/> if this
	 * category is a default category
	 */
	boolean isDefault();

	/**
	 * Tests if this category is delegated
	 *
	 * @return {@code true} if this
	 * category is delegated
	 */
	boolean isDelegated();

	/**
	 * Converts this category to
	 * XACML delegated category.
	 *
	 * @return {@link CategoryId}
	 */
	CategoryId toDelegatedCategory();
}
