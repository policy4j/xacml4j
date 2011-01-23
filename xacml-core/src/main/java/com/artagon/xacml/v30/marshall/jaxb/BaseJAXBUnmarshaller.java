package com.artagon.xacml.v30.marshall.jaxb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v30.XacmlSyntaxException;
import com.artagon.xacml.v30.marshall.Unmarshaller;
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
		try{
			javax.xml.bind.Unmarshaller u = context.createUnmarshaller();
			JAXBElement<?> jaxbInstance = null;
			if(source instanceof InputSource){
				jaxbInstance = (JAXBElement<?>)u.unmarshal((InputSource)source);
			}
			if(source instanceof InputStream){
				jaxbInstance = (JAXBElement<?>)u.unmarshal((InputStream)source);
			}
			if(source instanceof JAXBElement<?>){
				jaxbInstance =  (JAXBElement<?>)source;
			}
			if(source instanceof XMLStreamReader){
				jaxbInstance = (JAXBElement<?>)u.unmarshal((XMLStreamReader)source);
			}
			if(source instanceof Node){
				jaxbInstance = (JAXBElement<?>)u.unmarshal((Node)source);
			}
			if(source instanceof Source){
				jaxbInstance =  (JAXBElement<?>)u.unmarshal((Source)source);
			}
			if(source instanceof byte[]){
				jaxbInstance = (JAXBElement<?>)u.unmarshal(new ByteArrayInputStream((byte[])source));
			}
			if(jaxbInstance != null){
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
