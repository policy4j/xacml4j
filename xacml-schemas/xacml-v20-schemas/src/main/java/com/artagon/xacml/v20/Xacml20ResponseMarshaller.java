package com.artagon.xacml.v20;

import java.io.IOException;

import javax.xml.bind.JAXBContext;

import org.oasis.xacml.v20.jaxb.context.ObjectFactory;
import org.oasis.xacml.v20.jaxb.context.ResponseType;

import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.marshall.BaseJAXBMarshaller;
import com.artagon.xacml.v3.marshall.ResponseMarshaller;

/**
 * Marshals XACML 3.0 {@link ResponseContext} to the XACML 2.0 response
 * 
 * @author Giedrius Trumpickas
 */
public class Xacml20ResponseMarshaller 
	extends BaseJAXBMarshaller<ResponseContext> 
	implements ResponseMarshaller
{
	private Xacml20ContextMapper mapper;
	private ObjectFactory factory;
	
	public Xacml20ResponseMarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml20ContextMapper();
	}
	
	@Override
	public Object marshal(ResponseContext source) throws IOException {
		ResponseType response = mapper.create(source);
		return factory.createResponse(response);
	}
}
