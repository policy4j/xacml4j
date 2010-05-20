package com.artagon.xacml.v3;

public interface PolicyElement
{
	void accept(PolicyVisitor v);
}
