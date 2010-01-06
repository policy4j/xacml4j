package com.artagon.xacml.policy;

import java.util.Collection;

public final class Obligation extends BaseDecisionResponse
{
	public Obligation(String id, Collection<AttributeAssignment> attributes) {
		super(id, attributes);
	}	
}
