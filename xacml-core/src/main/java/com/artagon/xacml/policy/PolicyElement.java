package com.artagon.xacml.policy;

public interface PolicyElement
{
	void accept(PolicyVisitor v);
}
