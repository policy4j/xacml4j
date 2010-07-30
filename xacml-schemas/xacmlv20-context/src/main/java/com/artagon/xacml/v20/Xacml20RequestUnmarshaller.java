package com.artagon.xacml.v20;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.oasis.xacml.v20.context.RequestType;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.XacmlFactory;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.google.common.base.Preconditions;

public class Xacml20RequestUnmarshaller implements RequestUnmarshaller 
{
	private Xacml20ContextMapper mapper;
	
	public Xacml20RequestUnmarshaller(XacmlFactory factory){
		this.mapper = new Xacml20ContextMapper(factory);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Request unmarshalRequest(Object source)
			throws XacmlSyntaxException, IOException {
		Preconditions.checkNotNull(source);
		try{
			Unmarshaller u = Xacml20ContextMapper.getJaxbContext().createUnmarshaller();
			JAXBElement<RequestType> request = null;
			if(source instanceof InputSource){
				request =  (JAXBElement<RequestType>)u.unmarshal((InputSource)source);
			}
			if(source instanceof InputStream){
				request =  (JAXBElement<RequestType>)u.unmarshal((InputStream)source);
			}
			if(source instanceof JAXBElement<?>){
				request =  (JAXBElement<RequestType>)source;
			}
			if(source instanceof byte[]){
				request =  (JAXBElement<RequestType>)u.unmarshal(new ByteArrayInputStream((byte[])source));
			}
			if(request != null){
				return mapper.create(request.getValue());
			}
			throw new IllegalArgumentException(
					String.format("Unsupported XACML Request source=\"%s\"", 
							source.getClass().getName()));
		}catch(JAXBException e){
			throw new RequestSyntaxException(e);
		}
	}
	
}
