package com.artagon.xacml.invocation;

public interface Invocation <V>
{
	V invoke(Object ... params) throws Exception;
}
