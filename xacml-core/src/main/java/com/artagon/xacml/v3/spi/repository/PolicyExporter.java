package com.artagon.xacml.v3.spi.repository;

import javax.xml.soap.Node;
import javax.xml.stream.XMLStreamWriter;

import com.artagon.xacml.v3.CompositeDecisionRule;


/**
 * A policy exported to XACML capability
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyExporter 
	extends PolicyRepositoryCapability
{
	void export(CompositeDecisionRule p, Node node);
	void export(CompositeDecisionRule p, XMLStreamWriter writer);
}
