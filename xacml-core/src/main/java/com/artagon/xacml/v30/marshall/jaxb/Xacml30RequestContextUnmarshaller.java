package com.artagon.xacml.v30.marshall.jaxb;

import javax.xml.bind.JAXBElement;

import com.artagon.xacml.v30.marshall.RequestUnmarshaller;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.XacmlSyntaxException;
import com.google.common.base.Preconditions;

public class Xacml30RequestContextUnmarshaller extends 
	BaseJAXBUnmarshaller<RequestContext> 
implements RequestUnmarshaller 
{
	private Xacml30RequestContextFromJaxbToObjectModelMapper mapper;
	
	public Xacml30RequestContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v30.jaxb.RequestType));
		return mapper.create((org.oasis.xacml.v30.jaxb.RequestType)jaxbInstance.getValue());
	}
}
