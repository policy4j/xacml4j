package com.artagon.xacml.v30;


public interface PolicyElement
{
	void accept(PolicyVisitor v);
}
