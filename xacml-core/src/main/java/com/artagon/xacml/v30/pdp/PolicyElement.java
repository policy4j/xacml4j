package com.artagon.xacml.v30.pdp;


public interface PolicyElement
{
	void accept(PolicyVisitor v);
}
