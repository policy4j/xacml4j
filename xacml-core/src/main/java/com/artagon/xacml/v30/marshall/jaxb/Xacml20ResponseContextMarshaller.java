package com.artagon.xacml.v30.marshall.jaxb;

import java.io.IOException;

import org.oasis.xacml.v20.jaxb.context.ObjectFactory;
import org.oasis.xacml.v20.jaxb.context.ResponseType;

import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.marshall.ResponseMarshaller;

/**
 * Marshals XACML 3.0 {@link ResponseContext} to the XACML 2.0 response
 * 
 * @author Giedrius Trumpickas
 */
public class Xacml20ResponseContextMarshaller 
	extends BaseJAXBMarshaller<ResponseContext> 
	implements ResponseMarshaller
{
	private Xacml20RequestContextFromJaxbToObjectModelMapper mapper;
	private ObjectFactory factory;
	
	public Xacml20ResponseContextMarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml20RequestContextFromJaxbToObjectModelMapper();
		this.factory = new ObjectFactory();
	}
	
	@Override
	public Object marshal(ResponseContext source) throws IOException {
		ResponseType response = mapper.create(source);
		return factory.createResponse(response);
	}
}
