package com.artagon.xacml.v3;

import java.util.Collection;

public interface MatchAllOf extends Matchable
{
	Collection<Match> getMatch();
}