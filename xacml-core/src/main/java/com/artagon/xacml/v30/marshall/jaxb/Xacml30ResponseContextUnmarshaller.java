package com.artagon.xacml.v30.marshall.jaxb;

import javax.xml.bind.JAXBElement;

import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.XacmlSyntaxException;
import com.artagon.xacml.v30.marshall.ResponseUnmarshaller;
import com.google.common.base.Preconditions;

public class Xacml30ResponseContextUnmarshaller extends 
	BaseJAXBUnmarshaller<ResponseContext> 
implements ResponseUnmarshaller 
{
	private Xacml30RequestContextFromJaxbToObjectModelMapper mapper;
	
	public Xacml30ResponseContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	protected ResponseContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v30.jaxb.ResponseType));
		return mapper.create((org.oasis.xacml.v30.jaxb.ResponseType)jaxbInstance.getValue());
	}
}
