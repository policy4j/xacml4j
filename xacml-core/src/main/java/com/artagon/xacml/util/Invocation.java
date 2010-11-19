package com.artagon.xacml.util;

public interface Invocation <V>
{
	V invoke(Object ... params) throws Exception;
}
