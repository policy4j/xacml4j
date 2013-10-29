package org.xacml4j.v30.marshall.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshall.PolicyUnmarshaller;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;

public class XacmlPolicyUnmarshaller extends BaseJAXBUnmarshaller<CompositeDecisionRule>
	implements PolicyUnmarshaller
{
	private Xacml30PolicyFromJaxbToObjectModelMapper v30mapper;
	private Xacml20PolicyFromJaxbToObjectModelMapper v20mapper;

	private boolean supportsXacml20Policies;

	public XacmlPolicyUnmarshaller(
			JAXBContext context,
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionAlgorithms,
			boolean supportsXacml20Policies) throws Exception
	{
		super(context);
		this.supportsXacml20Policies = supportsXacml20Policies;
		this.v30mapper = new Xacml30PolicyFromJaxbToObjectModelMapper(functions, decisionAlgorithms);
		this.v20mapper = new Xacml20PolicyFromJaxbToObjectModelMapper(functions, decisionAlgorithms);
	}

	public XacmlPolicyUnmarshaller(FunctionProvider functions,
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
