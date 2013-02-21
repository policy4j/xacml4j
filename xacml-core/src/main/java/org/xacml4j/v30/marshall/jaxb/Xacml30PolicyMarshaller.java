package org.xacml4j.v30.marshall.jaxb;

import java.io.IOException;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.marshall.PolicyMarshaller;

public class Xacml30PolicyMarshaller extends BaseJAXBMarshaller<CompositeDecisionRule>
	implements PolicyMarshaller
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
