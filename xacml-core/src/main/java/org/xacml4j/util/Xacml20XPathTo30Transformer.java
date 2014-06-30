package org.xacml4j.util;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XPathExp;


public class Xacml20XPathTo30Transformer
{
	private final static Logger log = LoggerFactory.getLogger(Xacml20XPathTo30Transformer.class);

	private final static String REQUEST_ELEMENT_NAME = "Request";
	private final static String RESOURCE_ELEMENT_NAME = "Resource";
	private final static String RESOURCE_CONTENT_ELEMENT_NAME = "ResourceContent";

	/** Private constructor for utility class */
	private Xacml20XPathTo30Transformer() {}


	public static XPathExp fromXacml20String(StringExp path)
	{
		XPathExp xpathExp = XPathExp.valueOf(transform20PathTo30(path.getValue()),
				Categories.RESOURCE);
		return xpathExp;
	}

	public static String transform20PathTo30(String xpath)
	{
		StringBuilder buf = new StringBuilder(xpath);
		int firstIndex = xpath.indexOf(REQUEST_ELEMENT_NAME);
		if(firstIndex == -1){
			firstIndex = xpath.indexOf(RESOURCE_ELEMENT_NAME);
			if(firstIndex == -1){
				firstIndex = xpath.indexOf(RESOURCE_CONTENT_ELEMENT_NAME);
				if(firstIndex == -1){
					return xpath;
				}
			}
		}
		// found namespace prefix
		if(firstIndex > 0 &&
				buf.charAt(firstIndex - 1) == ':'){
			int index = xpath.indexOf("/");
			if(index == -1){
				firstIndex = 0;
			}
			else
			{
				firstIndex = index;
				while(xpath.charAt(index++) == '/'){
					firstIndex++;
				}
			}
		}
		int lastIndex = xpath.indexOf(RESOURCE_CONTENT_ELEMENT_NAME);
		if(lastIndex == -1){
			throw new IllegalArgumentException(
					String.format("Invalid XACML 2.0 xpath=\"%s\" " +
					"expression, \"ResourceContent\" is missing in the path", xpath));
		}
		lastIndex += RESOURCE_CONTENT_ELEMENT_NAME.length();
		buf.delete(firstIndex, lastIndex + 1);
		String transformedXpath =  buf.toString();
		if(log.isDebugEnabled()){
			log.debug("Original xpath=\"{}\", " +
					"transformed xpath=\"{}\"", xpath, transformedXpath);
		}
		return transformedXpath;
	}
}
