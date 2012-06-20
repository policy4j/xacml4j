package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.MatchResult;




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
