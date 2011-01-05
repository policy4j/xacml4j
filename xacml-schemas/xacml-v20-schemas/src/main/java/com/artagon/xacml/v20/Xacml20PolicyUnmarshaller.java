package com.artagon.xacml.v20;

import java.util.Collections;
import java.util.Map;

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
import com.google.common.base.Preconditions;

public class Xacml20PolicyUnmarshaller extends 
	BaseJAXBUnmarshaller<CompositeDecisionRule> 
	implements PolicyUnmarshaller
{
	private Xacml20PolicyFromJaxbToObjectModelMapper mapper;
	
	public Xacml20PolicyUnmarshaller(
			FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms) throws Exception
	{
		this(Collections.EMPTY_MAP, 
				functions, decisionAlgorithms);
	}
	
	public Xacml20PolicyUnmarshaller(
			Map<String, ?> jaxbProperties, 
			FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms) 
		throws Exception
	{
		super(getInstance(jaxbProperties));
		Preconditions.checkNotNull(functions);
		Preconditions.checkNotNull(decisionAlgorithms);
		this.mapper = new Xacml20PolicyFromJaxbToObjectModelMapper(functions, 
				decisionAlgorithms);
	}
	
	public static JAXBContext getInstance(
			Map<String, ?> jaxbProperties) throws JAXBException
	{
		Preconditions.checkNotNull(jaxbProperties);
		return JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), 
				ObjectFactory.class.getClassLoader(), 
				jaxbProperties);
	}

	@Override
	protected CompositeDecisionRule create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		return mapper.create(jaxbInstance.getValue());
	}
}
