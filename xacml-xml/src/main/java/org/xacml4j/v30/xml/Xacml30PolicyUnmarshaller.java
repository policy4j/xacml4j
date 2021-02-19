package org.xacml4j.v30.xml;

import javax.xml.bind.JAXBElement;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.FunctionProvider;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;

public class Xacml30PolicyUnmarshaller extends BaseJAXBUnmarshaller<CompositeDecisionRule>
		implements PolicyUnmarshaller
{
	private final Xacml30PolicyFromJaxbToObjectModelMapper mapper;

	public Xacml30PolicyUnmarshaller(FunctionProvider functionProvider,
	                                 DecisionCombiningAlgorithmProvider combiningAlgorithmProvider)
			throws Exception
	{
		super(JAXBUtils.getInstance());
		this.mapper = new Xacml30PolicyFromJaxbToObjectModelMapper(functionProvider,
		                                                           combiningAlgorithmProvider);
	}

	public Xacml30PolicyUnmarshaller()
			throws Exception
	{
		this(FunctionProvider.builder()
		                     .withStandardFunctions()
		                     .build(),
		      DecisionCombiningAlgorithmProvider.builder()
		                                        .withDefaultAlgorithms()
		                                        .build());
	}

	@Override
	protected CompositeDecisionRule create(JAXBElement jaxbInstance) throws SyntaxException {
		return mapper.create(jaxbInstance);
	}
}
