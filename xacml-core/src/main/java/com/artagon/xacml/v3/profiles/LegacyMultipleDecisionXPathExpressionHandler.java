package com.artagon.xacml.v3.profiles;

import java.util.Collection;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;

public class LegacyMultipleDecisionXPathExpressionHandler extends AbstractRequestProfileHandler
{
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
	}
}
