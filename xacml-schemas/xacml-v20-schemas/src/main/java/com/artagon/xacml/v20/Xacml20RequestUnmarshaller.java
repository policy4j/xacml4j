package com.artagon.xacml.v20;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.jaxb.context.RequestType;

import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.marshall.BaseJAXBUnmarshaller;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.google.common.base.Preconditions;

public class Xacml20RequestUnmarshaller extends 
	BaseJAXBUnmarshaller<RequestContext> 
implements RequestUnmarshaller 
{
	private Xacml20ContextMapper mapper;
	
	public Xacml20RequestUnmarshaller(JAXBContext context){
		super(context);
		this.mapper = new Xacml20ContextMapper();
	}
	
	public Xacml20RequestUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml20ContextMapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue() instanceof RequestType));
		return mapper.create((RequestType)jaxbInstance.getValue());
	}
}
