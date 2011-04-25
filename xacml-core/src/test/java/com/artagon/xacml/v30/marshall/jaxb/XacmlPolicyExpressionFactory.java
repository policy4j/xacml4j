package com.artagon.xacml.v30.marshall.jaxb;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.XacmlSyntaxException;

public interface XacmlPolicyExpressionFactory
{
	Expression create(XMLStreamReader r, XacmlPolicyParsingContext context) 
		throws XMLStreamException, XacmlSyntaxException;
}
