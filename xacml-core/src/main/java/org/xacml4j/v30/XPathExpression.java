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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class XPathExpression
{
	private String path;
	private CategoryId categoryId;

	public XPathExpression(String path,
			CategoryId category){
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(category);
		this.path = path;
		this.categoryId = category;
	}

	public CategoryId getCategory(){
		return categoryId;
	}

	public String getPath(){
		return path;
	}

	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof XPathExpression)){
			return false;
		}
		XPathExpression xpath = (XPathExpression)o;
		return categoryId.equals(xpath.categoryId)
				&& path.equals(xpath.path);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("category", categoryId.toString())
				.add("path", path).toString();
	}
}
