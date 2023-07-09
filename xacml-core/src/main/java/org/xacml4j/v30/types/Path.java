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

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import org.xacml4j.v30.*;

import javax.xml.xpath.XPathExpression;

/**
 * Represents either XML XPATH or JSON JPATH expression.
 *
 * @author Giedrius Trumpickas
 */
public interface Path extends Value<Path>
{
	Optional<CategoryId> getCategory();
	boolean hasCategory();
	String getPath();
	Content.PathType getPathType();

	default Content.Type getContentType(){
		return getPathType().getContentType();
	}

	static Path xpath(Object xpath, Object ...params){
		if(xpath instanceof StringVal){
			return of(((StringVal)xpath).get(),
					Content.PathType.XPATH,
					getCategory(params).orElse(CategoryId.RESOURCE));
		}
		if(xpath instanceof String){
			return of(((String)xpath),
					Content.PathType.XPATH,
					getCategory(params).orElse(CategoryId.RESOURCE));
		}
		if(xpath instanceof XPathExpression){
			return of(xpath.toString(),
					Content.PathType.XPATH,
					getCategory(params).orElse(CategoryId.RESOURCE));
		}
		throw SyntaxException.invalidAttributeValue(xpath, XacmlTypes.XPATH);
	}

	static Path jpath(Object xpath, Object ...params){
		if(xpath instanceof StringVal){
			return of(((StringVal)xpath).get(),
					Content.PathType.JPATH,
					getCategory(params).orElse(CategoryId.RESOURCE));
		}
		if(xpath instanceof String){
			return of(((String)xpath),
					Content.PathType.JPATH,
					getCategory(params).orElse(CategoryId.RESOURCE));
		}
		throw SyntaxException.invalidAttributeValue(xpath, XacmlTypes.JPATH);
	}

	static Path of(String path, Content.PathType pathType, CategoryId category){
		return new PathValue(path, pathType, category);
	}


	static Optional<CategoryId> getCategory(Object ...params){
		if(params == null || params.length == 0){
			return Optional.empty();
		}
		if(params[0] instanceof CategoryId){
			return Optional.of((CategoryId)params[0]);
		}
		return CategoryId.parse(params[0]);
	}

	final class PathValue extends SelfValue<Path>
			implements Path, Serializable
	{
		private final String path;
		private final CategoryId categoryId;
		private final Content.PathType pathType;

		private static final long serialVersionUID = 8576542145890616101L;

		private PathValue(String path,
						  Content.PathType pathType,
						  CategoryId categoryId){
			super(pathType.getXacmlType());
			this.path = Objects.requireNonNull(path, "path");
			this.pathType = Objects.requireNonNull(pathType, "pathType");
			this.categoryId = categoryId;
		}

		public String getPath(){
			return path;
		}

		public Content.PathType getPathType(){
			return pathType;
		}

		@Override
		public boolean hasCategory() {
			return categoryId != null;
		}

		public Content.Type getContentType(){
			return pathType.getContentType();
		}

		public Optional<CategoryId> getCategory(){
			return Optional.ofNullable(categoryId);
		}

		@Override
		public boolean equals(Object o){
			if(o == null){
				return false;
			}
			if(o == this){
				return true;
			}
			if(!(o instanceof PathValue)){
				return false;
			}
			PathValue xpath = (PathValue) o;
			return  Objects.equals(categoryId, xpath.getCategory().orElse(null))
					&& path.equals(xpath.getPath()) &&
					pathType.equals(xpath.getPathType()) &&
					getEvaluatesTo().equals(xpath.getEvaluatesTo());
		}

		@Override
		protected String formatVal(Path v) {
			return MoreObjects.toStringHelper(v)
					.add("p", v.getPath())
					.add("t", v.getPathType().name())
					.toString();
		}

		@Override
		public int hashCode(){
			return Objects.hash(categoryId, path, pathType);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("type", getEvaluatesTo())
					.add("path", path)
					.add("categoryId", categoryId)
					.add("pathType", pathType)
					.toString();
		}
	}
}
