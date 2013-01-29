package org.xacml4j.v30.marshall;

import java.io.IOException;

import org.xacml4j.v30.CompositeDecisionRule;


public interface PolicyMarshaller 
{
	Object marshal(CompositeDecisionRule policy) 
		throws IOException;
	
	void marshal(CompositeDecisionRule policy, Object source) 
		throws IOException;
}
