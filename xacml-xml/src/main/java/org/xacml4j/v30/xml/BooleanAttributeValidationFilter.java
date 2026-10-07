package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.BooleanVal;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/** Checks typed XACML boolean attributes before JAXB's permissive converter sees them. */
final class BooleanAttributeValidationFilter extends XMLFilterImpl
{
	private static final String POLICY20 = "urn:oasis:names:tc:xacml:2.0:policy:schema:os";
	private static final String CORE30 = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";

	BooleanAttributeValidationFilter(ContentHandler handler)
	{
		setContentHandler(handler);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (CORE30.equals(uri)) {
			switch (localName) {
				case "Request":
					validate(attributes, "ReturnPolicyIdList");
					validate(attributes, "CombinedDecision");
					break;
				case "Attribute":
					validate(attributes, "IncludeInResult");
					break;
				case "AttributeDesignator", "AttributeSelector":
					validate(attributes, "MustBePresent");
					break;
			}
		} else if (POLICY20.equals(uri)) {
			switch (localName) {
				case "SubjectAttributeDesignator", "ResourceAttributeDesignator", "ActionAttributeDesignator",
						"EnvironmentAttributeDesignator", "AttributeSelector":
					validate(attributes, "MustBePresent");
					break;
			}
		}
		super.startElement(uri, localName, qName, attributes);
	}

	private static void validate(Attributes attributes, String name) throws SAXException
	{
		String lexical = attributes.getValue("", name);
		if (lexical != null) {
			try {
				// XML Schema Part 2 sections 3.2.2 and 4.3.6: true/false/1/0 with XML whitespace collapse.
				BooleanVal.ofAny(lexical);
			} catch (SyntaxException failure) {
				throw new SAXException(new SyntaxException("Invalid xs:boolean attribute " + name, failure));
			}
		}
	}
}
