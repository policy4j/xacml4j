package com.artagon.xacml.v3.marshall;

import java.io.IOException;


import com.artagon.xacml.v3.CompositeDecisionRule;

public interface PolicyMarshaller 
{
	Object marshal(CompositeDecisionRule policy) 
		throws IOException;
	
	void marshal(CompositeDecisionRule policy, Object source) 
		throws IOException;
}
