package org.xacml4j.v30.marshal.jaxb;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import org.w3c.dom.Node;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xml.sax.InputSource;

import com.google.common.base.Preconditions;

public abstract class BaseJAXBUnmarshaller <T>
	implements Unmarshaller<T>
{
	private JAXBContext context;

	protected BaseJAXBUnmarshaller(JAXBContext context){
		Preconditions.checkArgument(context != null);
		this.context = context;
	}

	@Override
	public final T unmarshal(Object source) throws XacmlSyntaxException, IOException
	{
		Preconditions.checkNotNull(source);
		try{
			javax.xml.bind.Unmarshaller u = context.createUnmarshaller();
			JAXBElement<?> jaxbInstance = null;
			if (source instanceof InputSource) {
				jaxbInstance = (JAXBElement<?>) u.unmarshal((InputSource) source);
			} else if (source instanceof InputStream) {
				jaxbInstance = (JAXBElement<?>) u.unmarshal((InputStream) source);
			} else if (source instanceof JAXBElement<?>) {
				jaxbInstance = (JAXBElement<?>) source;
			} else if (source instanceof XMLStreamReader) {
				jaxbInstance = (JAXBElement<?>) u.unmarshal((XMLStreamReader) source);
			} else if (source instanceof Node) {
				jaxbInstance = (JAXBElement<?>) u.unmarshal((Node) source);
			} else if (source instanceof Source) {
				jaxbInstance = (JAXBElement<?>) u.unmarshal((Source) source);
			} else if (source instanceof byte[]) {
				jaxbInstance = (JAXBElement<?>) u.unmarshal(new ByteArrayInputStream((byte[]) source));
			}
			if (jaxbInstance != null) {
				return create(jaxbInstance);
			}
			throw new IllegalArgumentException(
					String.format("Unsupported source=\"%s\"",
							source.getClass().getName()));
		}catch(JAXBException e){
			throw new XacmlSyntaxException(e);
		}
	}

	protected abstract T create(JAXBElement<?> jaxbInstance) throws XacmlSyntaxException;
}
