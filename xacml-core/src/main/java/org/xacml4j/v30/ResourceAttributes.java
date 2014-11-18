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


/**
 * A well known XACML resource category category category
 *
 * @author Giedrius Trumpickas

 */
public enum ResourceAttributes
{

	/**
	 * This category identifies the resource to which access is requested.
	 */
	RESOURCE_ID("urn:oasis:names:tc:xacml:1.0:resource:resource-attributeId"),

	/**
	 * This category identifies the namespace of the top element(s) of the
	 * contents of the {@link org.xacml4j.v30.Entity#getContent()}.
	 * In the case where the resource content is supplied in
	 * the request context and the resource namespaces are defined in the resource,
	 * the PEP MAY provide this category in the request to
	 * indicate the namespaces of the resource content. In this case
	 * there SHALL be one value of this category for each unique namespace
	 * of the top level elements in the {@link org.xacml4j.v30.Entity#getContent()}.
	 * The type of the corresponding category SHALL be {@link org.xacml4j.v30.types.XacmlTypes#ANYURI}
	 */
	TARGET_NAMESPACE("urn:oasis:names:tc:xacml:2.0:resource:target-namespace");


	private final String id;

	private ResourceAttributes(String id){
		this.id = id;
	}

	@Override
	public String toString(){
		return id;
	}
}


