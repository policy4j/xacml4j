package com.artagon.xacml.policy;

import java.util.List;


public interface CompositeDecision  extends Decision
{
	List<? extends Decision> getDecisions();
}
