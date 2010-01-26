package com.artagon.xacml.v3.policy;

public interface Matchable extends PolicyElement
{
	/**
	 * Matches this matchable against given
	 * evaluation context.
	 * 
	 * @param context an evaluation context
	 * @return {@link MatchResult} instance
	 */
	MatchResult match(EvaluationContext context);
}
