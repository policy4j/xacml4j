package com.artagon.xacml.v30;




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
