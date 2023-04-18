package org.xacml4j.v30.xml;

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

import javax.xml.bind.JAXBElement;

import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.RequestContext;

import com.google.common.base.Preconditions;

public final class Xacml30RequestContextUnmarshaller extends
	BaseJAXBUnmarshaller<RequestContext>
		implements RequestUnmarshaller {
	private Xacml30RequestContextFromJaxbToObjectModelMapper mapper;

	public Xacml30RequestContextUnmarshaller(){
		super(JAXBUtils.getInstance());
		this.mapper = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws SyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue()
				instanceof org.oasis.xacml.v30.jaxb.RequestType));
		return mapper.create((org.oasis.xacml.v30.jaxb.RequestType)jaxbInstance.getValue());
	}
}
