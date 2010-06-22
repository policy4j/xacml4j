package com.artagon.xacml.v20;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;

import org.oasis.xacml.v20.context.ObjectFactory;
import org.oasis.xacml.v20.context.ResponseType;

import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.marshall.ResponseMarshaller;
import com.google.common.base.Preconditions;

/**
 * Marshals XACML 3.0 {@link Response} to the XACML 2.0 response
 * 
 * @author Giedrius Trumpickas
 */
public class Xacml20ResponseMarshaller implements ResponseMarshaller
{
	private Xacml20ContextMapper mapper;
	private ObjectFactory contextJaxbFactory;
	
	public Xacml20ResponseMarshaller(){
		this.mapper = new Xacml20ContextMapper();
		this.contextJaxbFactory = new ObjectFactory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void marshall(Response response, Object output) throws IOException 
	{
		Preconditions.checkArgument(response != null);
		Preconditions.checkArgument(output != null);
		try{
			Marshaller m = Xacml20ContextMapper.getJaxbContext().createMarshaller();
			JAXBElement<ResponseType> jaxbElement = (JAXBElement<ResponseType>)marshall(response);
			if(output instanceof OutputStream){
				m.marshal(jaxbElement, (OutputStream)output);
				return;
			}
			if(output instanceof Result){
				m.marshal(jaxbElement, (javax.xml.transform.Result)output);
				return;
			}
			if(output instanceof XMLStreamWriter){
				m.marshal(jaxbElement, (XMLStreamWriter)output);
				return;
			}
			if(output instanceof Writer){
				m.marshal(jaxbElement, (Writer)output);
				return;
			}
			throw new IllegalArgumentException(
					String.format("Unsupported marshalling target=\"%s\"", 
							output.getClass().getName()));
		}catch(JAXBException e){
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Object marshall(Response response) throws IOException 
	{
		ResponseType responseType = mapper.create(response);
		return contextJaxbFactory.createResponse(responseType);
	}
	
}
