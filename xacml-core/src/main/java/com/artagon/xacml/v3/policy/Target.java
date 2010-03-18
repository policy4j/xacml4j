package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.EvaluationContext;

public interface Target extends Matchable, PolicyElement
{
	/**
	 * Evaluates this target if it matches request
	 * context
	 * 
	 * @param context an evaluation context
	 */
	MatchResult match(EvaluationContext context);
}