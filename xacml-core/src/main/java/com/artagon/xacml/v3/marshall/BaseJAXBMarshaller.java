package com.artagon.xacml.v3.marshall;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.w3c.dom.Node;

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
		Preconditions.checkArgument(source != null);
		Preconditions.checkArgument(target != null);
		try{
			javax.xml.bind.Marshaller m = context.createMarshaller();
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
			throw new IllegalArgumentException(
					String.format("Unsupported marshalling target=\"%s\"", 
							target.getClass().getName()));
		}catch(JAXBException e){
			throw new IllegalStateException(e);
		}
	}
}
