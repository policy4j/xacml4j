package org.xacml4j.v30.marshal.jaxb;

import java.io.IOException;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.marshal.PolicyMarshaller;

public class Xacml30PolicyMarshaller extends BaseJAXBMarshaller<CompositeDecisionRule>
	implements PolicyMarshaller
{
	private Xacml30PolicyFromObjectModelToJaxbMapper mapper;
	
	public Xacml30PolicyMarshaller() {
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml30PolicyFromObjectModelToJaxbMapper();
	}

	@Override
	public Object marshal(CompositeDecisionRule d) throws IOException {
		return mapper.toJaxb(d);
	}
}
