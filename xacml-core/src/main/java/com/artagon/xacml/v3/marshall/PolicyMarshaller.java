package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.CompositeDecisionRule;

public interface PolicyMarshaller 
{
	Object marshall(CompositeDecisionRule policy) 
		throws IOException;
}
