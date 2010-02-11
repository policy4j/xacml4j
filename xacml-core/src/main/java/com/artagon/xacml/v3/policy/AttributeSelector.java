package com.artagon.xacml.v3.policy;

import javax.xml.xpath.XPath;

public interface AttributeSelector extends AttributeReference
{
	XPath getContextPath();
}