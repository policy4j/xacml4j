package com.artagon.xacml.v30;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import com.artagon.xacml.v20.Xacml20PolicyMapper;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.BaseJAXBUnmarshaller;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;

public class Xacml30PolicyUnmarshaller extends BaseJAXBUnmarshaller<CompositeDecisionRule> 
	implements PolicyUnmarshaller
{
	private Xacml30PolicyMapper v30mapper;
	private Xacml20PolicyMapper v20mapper;
	
	private boolean supportsXacml20Policies;
	
	public Xacml30PolicyUnmarshaller(
			JAXBContext context, 
			FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms, 
			boolean supportsXacml20Policies) throws Exception
	{
		super(context);
		this.supportsXacml20Policies = supportsXacml20Policies;
		this.v30mapper = new Xacml30PolicyMapper(functions, decisionAlgorithms);
		this.v20mapper = new Xacml20PolicyMapper(functions, decisionAlgorithms);
	}
	
	public Xacml30PolicyUnmarshaller(FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms) 
		throws Exception
	{
		this(JAXBContextUtil.getInstance(), 
				functions, decisionAlgorithms, true);
	}
	
	public Xacml30PolicyUnmarshaller(boolean supportsXacml20Policies) 
		throws Exception
	{
		this(JAXBContextUtil.getInstance(), 
				null, null, supportsXacml20Policies);
	}
	
	public Xacml30PolicyUnmarshaller() throws Exception{
		this(true);
	}
	
	
	
	@Override
	protected CompositeDecisionRule create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		if(supportsXacml20Policies && 
				jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v20.jaxb.policy.PolicySetType){
			return v20mapper.create(jaxbInstance.getValue());
		}
		if(supportsXacml20Policies &&
				jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v20.jaxb.policy.PolicyType){
			return v20mapper.create(jaxbInstance.getValue());
		}
		if(jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v30.jaxb.PolicyType){
			return v30mapper.create(jaxbInstance.getValue());
		}
		if(jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v30.jaxb.PolicySetType){
			return v30mapper.create(jaxbInstance.getValue());
		}
		throw new IllegalArgumentException(
				String.format("Can not unmarshall=\"%s\" element", 
						jaxbInstance.getName()));
	}	
}
