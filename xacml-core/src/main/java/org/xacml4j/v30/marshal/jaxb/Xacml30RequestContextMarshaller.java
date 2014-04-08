package org.xacml4j.v30.marshal.jaxb;

import java.io.IOException;

import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.RequestType;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.marshal.RequestMarshaller;

public class Xacml30RequestContextMarshaller extends BaseJAXBMarshaller<RequestContext> 
 implements RequestMarshaller
{
private final static ObjectFactory factory = new ObjectFactory();
	
	private Xacml30RequestContextFromJaxbToObjectModelMapper mapper;

	public Xacml30RequestContextMarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	public Object marshal(RequestContext source) throws IOException {
		RequestType response = mapper.create(source);
		return factory.createRequest(response);
	}
}
