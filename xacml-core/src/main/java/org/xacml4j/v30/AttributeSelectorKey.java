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

import org.xacml4j.v30.types.PathValue;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Represents XACML attribute selector
 *
 * @author Giedrius Trumpickas
 */
public final class AttributeSelectorKey
	extends AttributeReferenceKey
{
	private String path;
	private String contextSelectorId;
	private int hashCode;
	private Content.PathType type;

	private AttributeSelectorKey(Builder b){
		super(b);
		this.path = java.util.Objects.requireNonNull(b.path);
		this.type = java.util.Objects.requireNonNull(b.type);
		this.contextSelectorId = b.contextSelectorId;
	}

	public static Builder builder(){
		return new Builder();
	}


	public String getPath(){
		return path;
	}

	public Content.Type getContentType(){
		return type.getContentType();
	}
	public Content.PathType getPathType(){
		return type;
	}


	public String getContextSelectorId(){
		return contextSelectorId;
	}


	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("Category", getCategory().getAbbreviatedId())
		.add("Path", path)
		.add("PathType", type)
		.add("DataType", getDataType().getShortTypeId())
		.add("ContextSelectorId", contextSelectorId).toString();
	}

	@Override
	public int hashCode(){
		if(hashCode == 0){
			this.hashCode =  Objects.hashCode(
					category, path, type, dataType, contextSelectorId);
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeSelectorKey)){
			return false;
		}
		AttributeSelectorKey s = (AttributeSelectorKey)o;
		return category.equals(s.category) &&
				dataType.equals(s.dataType) &&
				type.equals(s.type) &&
				path.equals(s.path) &&
				Objects.equal(contextSelectorId,
						s.contextSelectorId);
	}

	public static class Builder extends AttributeReferenceKey.Builder<Builder>
	{
		private String path;
		private String contextSelectorId;
		private Content.PathType type = Content.PathType.XPATH;

		/**
		 * Creates selector with XML path expression aka XPATH
		 *
		 * @param xpath an XPATH expression
		 * @return {@link Builder}
		 */
		public Builder xpath(String xpath){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(xpath));
			this.path = xpath;
			this.type = Content.PathType.XPATH;
			return this;
		}

		/**
		 * Creates selector with JSON path expression
		 *
		 * @param jpath a JSON path expression
		 * @return {@link Builder}
		 */
		public Builder jpath(String jpath){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(jpath));
			this.path = jpath;
			this.type = Content.PathType.JPATH;
			return this;
		}

		public Builder path(String path, Content.PathType type){
			this.path = java.util.Objects.requireNonNull(path);
			this.type = java.util.Objects.requireNonNull(type);
			return this;
		}

		public Builder path(Path p){
			java.util.Objects.requireNonNull(p);
			this.path = p.getPath();
			this.type = p.getType();
			return this;
		}

		public Builder path(PathValue xpath,
                            boolean ignoreCategoryFromPath){
			this.path = xpath.value().getPath();
			this.type = xpath.value().getType();
			return ignoreCategoryFromPath?this:
					category(xpath.getCategory().orElse(null));
		}

		public Builder path(PathValue xpath){
			return path(xpath, false);
		}

		public Builder contextSelectorId(String id){
			this.contextSelectorId = id;
			return this;
		}

		public Builder from(AttributeSelectorKey s){
			return builder()
					.category(s.category)
					.dataType(s.dataType)
					.path(s.getPath(),  s.getPathType())
					.contextSelectorId(s.contextSelectorId);
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public AttributeSelectorKey build(){
			return new AttributeSelectorKey(this);
		}

	}
}
