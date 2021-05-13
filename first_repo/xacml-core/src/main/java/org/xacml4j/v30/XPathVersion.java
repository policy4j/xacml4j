package org.xacml4j.v30;

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

public enum XPathVersion
{
	XPATH1("http://www.w3.org/TR/1999/Rec-xpath-19991116"),
	XPATH2("http://www.w3.org/TR/2007/REC-xpath20-20070123");

	private String versionURI;

	private XPathVersion(String versionURI){
		this.versionURI = versionURI;
	}

	@Override
	public String toString(){
		return versionURI;
	}

	public static XPathVersion parse(String v)
	{
		for(XPathVersion version : XPathVersion.values()){
			if(version.versionURI.equalsIgnoreCase(v)){
				return version;
			}
		}
		return null;
	}
}
