package com.artagon.xacml.v3.marshall.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.jaxb.context.RequestType;


import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;

public class RequestContextUnmarshaller extends 
	BaseJAXBUnmarshaller<RequestContext> 
implements RequestUnmarshaller 
{
	private Xacml20RequestContextFromJaxbToObjectModelMapper mapper20;
	private Xacml30RequestContextFromJaxbToObjectModelMapper mapper30;
	
	public RequestContextUnmarshaller(JAXBContext context){
		super(context);
		this.mapper20 = new Xacml20RequestContextFromJaxbToObjectModelMapper();
		this.mapper30 = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}
	
	public RequestContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper20 = new Xacml20RequestContextFromJaxbToObjectModelMapper();
		this.mapper30 = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		if(jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v20.jaxb.context.RequestType){
			return mapper20.create((org.oasis.xacml.v20.jaxb.context.RequestType)jaxbInstance.getValue());
		}
		if(jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v30.jaxb.RequestType){
			return mapper30.create((org.oasis.xacml.v30.jaxb.RequestType)jaxbInstance.getValue());
		}
		throw new IllegalArgumentException("Unknown JAXB type");
	}
}
