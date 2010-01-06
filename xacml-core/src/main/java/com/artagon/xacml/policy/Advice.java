package com.artagon.xacml.policy;

import java.util.Collection;

public final class Advice extends BaseDecisionResponse
{
	public Advice(String adviceId, Collection<AttributeAssignment> attributes) {
		super(adviceId, attributes);
	}
}
