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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.w3c.dom.Node;
import org.xacml4j.v30.marshal.Marshaller;

import com.google.common.base.Preconditions;

public abstract class BaseJAXBMarshaller<T> implements Marshaller<T>
{
	private JAXBContext context;

	public BaseJAXBMarshaller(JAXBContext context){
		Preconditions.checkArgument(context != null);
		this.context = context;
	}

	@Override
	public final void marshal(T source, Object target) throws IOException
	{
		Preconditions.checkNotNull(source);
		Preconditions.checkNotNull(target);
		try{
			jakarta.xml.bind.Marshaller m = context.createMarshaller();
			JAXBElement<?> jaxbElement = (JAXBElement<?>)marshal(source);
			if(target instanceof OutputStream){
				m.marshal(jaxbElement, (OutputStream)target);
				return;
			}
			if(target instanceof Result){
				m.marshal(jaxbElement, (javax.xml.transform.Result)target);
				return;
			}
			if(target instanceof XMLStreamWriter){
				m.marshal(jaxbElement, (XMLStreamWriter)target);
				return;
			}
			if(target instanceof Writer){
				m.marshal(jaxbElement, (Writer)target);
				return;
			}
			if(target instanceof Node){
				m.marshal(jaxbElement, (Node)target);
				return;
			}
			if(target instanceof OutputStream){
				m.marshal(jaxbElement, (OutputStream)target);
				return;
			}
			throw new IllegalArgumentException(
					String.format("Unsupported marshalling target=\"%s\"",
							target.getClass().getName()));
		}catch(JAXBException e){
			throw new IllegalStateException(e);
		}
	}
}
