package com.artagon.xacml.v3;

import java.util.Collection;

public interface MatchAnyOf extends Matchable
{
	Collection<MatchAllOf> getAllOf();
}