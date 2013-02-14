package org.xacml4j.v30.marshall.jaxb;

import java.io.IOException;

import org.xacml4j.v30.CompositeDecisionRule;

public class Xacml30PolicyMarshaller extends BaseJAXBMarshaller<CompositeDecisionRule>
{
	private Xacml30PolicyFromObjectModelToJaxbMapper mapper = new Xacml30PolicyFromObjectModelToJaxbMapper();
	
	public Xacml30PolicyMarshaller() {
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml30PolicyFromObjectModelToJaxbMapper();
	}
	
	@Override
	public Object marshal(CompositeDecisionRule d) throws IOException {
		return mapper.toJaxb(d);
	}
}
