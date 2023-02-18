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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents either XML XPATH or JSON JPATH expression.
 *
 * @author Giedrius Trumpickas
 */
public final class Path implements Externalizable
{
	private String path;
	private CategoryId categoryId;
	private Content.PathType type;

	private Path(String path,
				 CategoryId category,
				 Content.PathType type){
		Preconditions.checkNotNull(path);
		this.path = path;
		this.categoryId = category;
		this.path = java.util.Objects.requireNonNull(path);
		this.type = java.util.Objects.requireNonNull(type);
	}

	public static Path of(String xpath,
						  CategoryId categoryId){
		return of(xpath, categoryId, Content.PathType.XPATH);
	}

	public static Path of(String xpath,
						  CategoryId category,
						  Content.PathType type){
		return new Path(xpath, category, type);
	}

	public Optional<CategoryId> getCategory(){
		return Optional.ofNullable(categoryId);
	}

	public boolean hasCategory(){
		return categoryId != null;
	}

	/**
	 * Gets path expression
	 *
	 * @return path expression
	 */
	public String getPath(){
		return path;
	}

	/**
	 * Gets path type
	 *
	 * @return path type
	 */
	public Content.PathType getType(){
		return type;
	}


	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof Path)){
			return false;
		}
		Path xpath = (Path)o;
		return categoryId.equals(xpath.categoryId)
				&& path.equals(xpath.path) &&
				type.equals(xpath.type);
	}

	@Override
	public int hashCode(){
		return Objects.hash(categoryId, path, type);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("path", path)
				.add("categoryId", categoryId)
				.add("type", type)
				.toString();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(categoryId);
		out.writeObject(type);
		out.writeUTF(path);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.categoryId = (CategoryId)in.readObject();
		this.type = (Content.PathType) in.readObject();
		this.path = in.readUTF();
	}
}
