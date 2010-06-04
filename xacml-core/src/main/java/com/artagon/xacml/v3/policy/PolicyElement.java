package com.artagon.xacml.v3.policy;

public interface PolicyElement
{
	void accept(PolicyVisitor v);
}
