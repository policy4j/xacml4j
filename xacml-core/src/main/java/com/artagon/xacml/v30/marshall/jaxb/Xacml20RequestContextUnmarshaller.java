package com.artagon.xacml.v30.marshall.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import com.artagon.xacml.v30.marshall.RequestUnmarshaller;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.XacmlSyntaxException;
import com.google.common.base.Preconditions;

public class Xacml20RequestContextUnmarshaller extends 
	BaseJAXBUnmarshaller<RequestContext> 
implements RequestUnmarshaller 
{
	private Xacml20RequestContextFromJaxbToObjectModelMapper mapper20;
	
	public Xacml20RequestContextUnmarshaller(JAXBContext context){
		super(context);
		this.mapper20 = new Xacml20RequestContextFromJaxbToObjectModelMapper();
	}
	
	public Xacml20RequestContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper20 = new Xacml20RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v20.jaxb.context.RequestType));
		return mapper20.create((org.oasis.xacml.v20.jaxb.context.RequestType)jaxbInstance.getValue());
	}
}
