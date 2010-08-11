package com.artagon.xacml.v20;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.ResponseSyntaxException;
import com.artagon.xacml.v3.marshall.ResponseUnmarshaller;
import com.google.common.base.Preconditions;

public class Xacml20ResponseUnmarshaller implements ResponseUnmarshaller
{
private Xacml20ContextMapper mapper;
	
	public Xacml20ResponseUnmarshaller(){
		this.mapper = new Xacml20ContextMapper();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseContext unmarshal(Object source)
			throws ResponseSyntaxException, IOException {
		Preconditions.checkNotNull(source);
		try{
			Unmarshaller u = Xacml20ContextMapper.getJaxbContext().createUnmarshaller();
			JAXBElement<ResponseType> request = null;
			if(source instanceof InputSource){
				request =  (JAXBElement<ResponseType>)u.unmarshal((InputSource)source);
			}
			if(source instanceof InputStream){
				request =  (JAXBElement<ResponseType>)u.unmarshal((InputStream)source);
			}
			if(source instanceof JAXBElement<?>){
				request =  (JAXBElement<ResponseType>)source;
			}
			if(source instanceof byte[]){
				request =  (JAXBElement<ResponseType>)u.unmarshal(new ByteArrayInputStream((byte[])source));
			}
			if(request != null){
				return mapper.create(request.getValue());
			}
			throw new IllegalArgumentException(
					String.format("Unsupported XACML 2.0 Response source=\"%s\"", 
							source.getClass().getName()));
		}catch(JAXBException e){
			throw new ResponseSyntaxException(e);
		}
	}
}
