package com.artagon.xacml.v3.policy;

import java.util.List;

/**
 * An interface for a decisions which contain
 * other decision rules. Example of an implementation
 * of such interface is {@link Policy} or {@link PolicySet}
 * 
 * @author Giedrius Trumpickas
 */
public interface CompositeDecisionRule  extends DecisionRule
{
	List<? extends DecisionRule> getDecisions();
}
