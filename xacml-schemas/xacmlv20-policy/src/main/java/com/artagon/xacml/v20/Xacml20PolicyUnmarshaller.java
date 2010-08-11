package com.artagon.xacml.v20;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.BaseJAXBUnmarshaller;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;

public class Xacml20PolicyUnmarshaller extends 
	BaseJAXBUnmarshaller<CompositeDecisionRule> 
	implements PolicyUnmarshaller
{
	private Xacml20PolicyMapper mapper;
	
	public Xacml20PolicyUnmarshaller(JAXBContext context)
	{
		super(context);
		this.mapper = new Xacml20PolicyMapper();
	}

	@Override
	protected CompositeDecisionRule create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		return mapper.create(jaxbInstance.getValue());
	}
	
	
	
}
