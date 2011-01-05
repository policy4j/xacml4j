package com.artagon.xacml.v30;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import com.artagon.xacml.v20.Xacml20PolicyFromJaxbToObjectModelMapper;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.BaseJAXBUnmarshaller;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.spi.combine.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.function.FunctionProvider;

public class Xacml30PolicyUnmarshaller extends BaseJAXBUnmarshaller<CompositeDecisionRule> 
	implements PolicyUnmarshaller
{
	private PolicyFromJaxbToObjectModelMapper v30mapper;
	private Xacml20PolicyFromJaxbToObjectModelMapper v20mapper;
	
	private boolean supportsXacml20Policies;
	
	public Xacml30PolicyUnmarshaller(
			JAXBContext context, 
			FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms, 
			boolean supportsXacml20Policies) throws Exception
	{
		super(context);
		this.supportsXacml20Policies = supportsXacml20Policies;
		this.v30mapper = new PolicyFromJaxbToObjectModelMapper(functions, decisionAlgorithms);
		this.v20mapper = new Xacml20PolicyFromJaxbToObjectModelMapper(functions, decisionAlgorithms);
	}
	
	public Xacml30PolicyUnmarshaller(FunctionProvider functions, 
			DecisionCombiningAlgorithmProvider decisionAlgorithms) 
		throws Exception
	{
		this(JAXBContextUtil.getInstance(), 
				functions, decisionAlgorithms, true);
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
