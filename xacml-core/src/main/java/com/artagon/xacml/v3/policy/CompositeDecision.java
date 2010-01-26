package com.artagon.xacml.v3.policy;

import java.util.List;

/**
 * An interface for a decisions which contain
 * other decision rules. Example of an implementation
 * of such interface is XACML policy or policy set
 * 
 * @author Giedrius Trumpickas
 */
public interface CompositeDecision  extends Decision
{
	List<? extends Decision> getDecisions();
}
