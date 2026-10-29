package org.xacml4j.v30.spi.observe;

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

/**
 * Where the outcome of one attribute resolution comes from, as
 * {@link DecisionObserver#attributeResolved} reports it (design D34)
 */
public enum AttributeSource
{
	/** The request has the attribute, at its first test */
	REQUEST,
	/** A later test gets the bag that the request gave first */
	REQUEST_MEMO,
	/** An attempt of the information point, or the outcome of an attempt in flight that the resolution shared */
	INFORMATION_POINT,
	/** A bag that the information point gave earlier in the decision */
	INFORMATION_POINT_MEMO,
	/** The key is already on the way down, which gives absence */
	CYCLE
}
