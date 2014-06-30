package org.xacml4j.v30.types;

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

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.XPathExpression;

public final class XPathExp extends BaseAttributeExp<XPathExpression>
{
	private static final long serialVersionUID = 8576542145890616101L;

	XPathExp(XPathExpression xp){
		super(XacmlTypes.XPATH,
				xp);
	}
	
	public static XPathExp valueOf(XPathExpression xp){
		return new XPathExp(xp);
	}
	
	public static XPathExp valueOf(String xpath, CategoryId category){
		return new XPathExp(new XPathExpression(xpath, category));
	}

	public String getPath(){
		return getValue().getPath();
	}

	public CategoryId getCategory(){
		return getValue().getCategory();
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.XPATH.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.XPATH.bag();
	}
}
