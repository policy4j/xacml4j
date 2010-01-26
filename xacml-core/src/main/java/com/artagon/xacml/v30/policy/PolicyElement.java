package com.artagon.xacml.v30.policy;

public interface PolicyElement
{
	void accept(PolicyVisitor v);
}
