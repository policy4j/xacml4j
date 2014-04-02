package org.xacml4j.v30.marshal.jaxb;

import java.io.IOException;

import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.marshal.ResponseMarshaller;

public class Xacml30ResponseContextMarshaller extends
	BaseJAXBMarshaller<ResponseContext>
implements ResponseMarshaller
{
	private final static ObjectFactory factory = new ObjectFactory();
	
	private Xacml30RequestContextFromJaxbToObjectModelMapper mapper;

	public Xacml30ResponseContextMarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	public Object marshal(ResponseContext source) throws IOException {
		ResponseType response = mapper.create(source);
		return factory.createResponse(response);
	}
}
