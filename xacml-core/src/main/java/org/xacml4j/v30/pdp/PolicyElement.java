package org.xacml4j.v30.pdp;


public interface PolicyElement
{
	void accept(PolicyVisitor v);
}
