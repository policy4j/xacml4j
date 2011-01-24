package com.artagon.xacml.v30.marshall.jaxb;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.jaxb.context.ResponseType;

import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.XacmlSyntaxException;
import com.artagon.xacml.v30.marshall.ResponseUnmarshaller;

public class Xacml20ResponseContextUnmarshaller 
	extends BaseJAXBUnmarshaller<ResponseContext> 
implements ResponseUnmarshaller
{
	private Xacml20RequestContextFromJaxbToObjectModelMapper mapper;
	
	public Xacml20ResponseContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml20RequestContextFromJaxbToObjectModelMapper();
	}
	
	@Override
	protected ResponseContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		return mapper.create(
				(ResponseType)jaxbInstance.getValue());
	}
}
