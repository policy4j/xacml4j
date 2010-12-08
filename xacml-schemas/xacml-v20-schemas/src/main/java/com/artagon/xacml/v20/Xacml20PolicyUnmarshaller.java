package com.artagon.xacml.v20;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.oasis.xacml.v20.jaxb.policy.ObjectFactory;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.BaseJAXBUnmarshaller;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.combine.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.function.FunctionProvider;

public class Xacml20PolicyUnmarshaller extends 
	BaseJAXBUnmarshaller<CompositeDecisionRule> 
	implements PolicyUnmarshaller
{
	private Xacml20PolicyMapper mapper;
	
	public Xacml20PolicyUnmarshaller(JAXBContext context) 
		throws Exception
	{
		super(context);
		this.mapper = new Xacml20PolicyMapper();
	}
	
	public Xacml20PolicyUnmarshaller() 
		throws Exception
	{
		super(getInstance());
		this.mapper = new Xacml20PolicyMapper();
	}
	
	public Xacml20PolicyUnmarshaller(JAXBContext context, FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms) throws Exception
	{
		super(context);
		this.mapper = new Xacml20PolicyMapper(functions, decisionAlgorithms);
	}
	
	public static JAXBContext getInstance() throws JAXBException
	{
		return JAXBContext.newInstance(
				ObjectFactory.class.getPackage().getName());
	}

	@Override
	protected CompositeDecisionRule create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		return mapper.create(jaxbInstance.getValue());
	}
}
