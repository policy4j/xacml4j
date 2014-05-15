package org.xacml4j.v30.marshal.jaxb;

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

import javax.xml.bind.JAXBElement;

import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.ResponseUnmarshaller;

import com.google.common.base.Preconditions;

public class Xacml30ResponseContextUnmarshaller extends
	BaseJAXBUnmarshaller<ResponseContext>
implements ResponseUnmarshaller
{
	private Xacml30RequestContextFromJaxbToObjectModelMapper mapper;

	public Xacml30ResponseContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	protected ResponseContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue()
				instanceof org.oasis.xacml.v30.jaxb.ResponseType));
		return mapper.create((org.oasis.xacml.v30.jaxb.ResponseType)jaxbInstance.getValue());
	}
}
