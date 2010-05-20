package com.artagon.xacml.v3;


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