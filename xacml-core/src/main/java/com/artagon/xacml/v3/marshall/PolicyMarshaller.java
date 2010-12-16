package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.ReferencableDecisionRule;

public interface PolicyMarshaller 
{
	Object marshal(ReferencableDecisionRule policy) 
		throws IOException;
	
	void marshal(ReferencableDecisionRule policy, Object source) 
		throws IOException;
}
