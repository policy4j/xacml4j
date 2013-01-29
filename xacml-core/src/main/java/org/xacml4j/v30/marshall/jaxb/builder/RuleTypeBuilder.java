package org.xacml4j.v30.marshall.jaxb.builder;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.RuleType;

public class RuleTypeBuilder {

	private final static ObjectFactory factory = new ObjectFactory();
	
	private RuleType rule;
	
	private RuleTypeBuilder(){
		this.rule = new RuleType();
	}
	
	public JAXBElement<RuleType> buildAsJAXBElement()
	{
		return factory.createRule(rule);
	}
	
	public RuleType build(){
		return rule;
	}
}
