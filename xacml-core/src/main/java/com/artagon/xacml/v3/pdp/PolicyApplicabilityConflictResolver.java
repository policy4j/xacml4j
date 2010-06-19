package com.artagon.xacml.v3.pdp;

import java.util.Map;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.spi.PolicyRepository;

/**
 * An interface used by {@link PolicyDecisionPoint} to resolve
 * a conflict when more than one applicable policy is found by 
 * PDP via {@link PolicyRepository#findApplicable(com.artagon.xacml.v3.EvaluationContext)
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyApplicabilityConflictResolver 
{
	CompositeDecisionRule resolve(Map<String, CompositeDecisionRule> applicable);
}
