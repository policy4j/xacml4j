package com.artagon.xacml.v3;

import java.util.Collection;


public interface Target extends Matchable, PolicyElement
{
	Collection<MatchAnyOf> getAnyOf();
}