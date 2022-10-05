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


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.w3c.dom.Node;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeContainer;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.Content;

import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An entity represents a collection of related attributes
 *
 * @author Giedrius Trumpickas
 */
public final class Entity extends AttributeContainer
{
	private final Content content;
	private transient int hashCode = -1;

	private Entity(Builder b) {
		super(b);
		this.content = b.content;

	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Gets entity with all include
	 * in result attributes
	 * @return {@link Entity} with all include in result attributes
	 */
	public Entity getIncludeInResult(){
		return Entity
		.builder()
		.copyOf(this, a -> a.isIncludeInResult())
				.build();
	}

	/**
	 * Gets content as {@link Node}
	 * defaultProvider
	 *
	 * @return a {@link Node} defaultProvider or {@code null}
	 */
	public <C extends Content> java.util.Optional<C> getContent(){
		return Optional.<C>ofNullable((C)content);
	}

	/**
	 * Tests if this entity has content
	 *
	 * @return {@code true} if entity has content; returns {@code false} otherwise
	 */
	public boolean hasContent(){
		return content != null;
	}


	public java.util.Optional<BagOfValues> resolve(AttributeDesignatorKey designatorKey)
    {
		Collection<Value> v = getAttributeValues(designatorKey.getAttributeId(),
		                                         designatorKey.getIssuer(), designatorKey.getDataType());
		if(v.isEmpty()){
			return java.util.Optional.empty();
		}
		return java.util.Optional.of(designatorKey
				.getDataType()
				.bag()
				.attributes(v)
				.build());
	}

	public java.util.Optional<BagOfValues> resolve(AttributeSelectorKey selector){
		return content != null? content.resolve(selector, ()->this)
		                      :Optional.empty();
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("attributes", attributes)
		.add("content", content != null? content.toString():null)
		.toString();
	}

	@Override
	public int hashCode(){
		if(hashCode == -1){
			this.hashCode = Objects.hashCode(attributes, content);
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Entity)) {
			return false;
		}
		Entity a = (Entity) o;
		return Objects.equal(attributes, a.attributes) &&
				Objects.equal(content, a.content);
	}

	public static class Builder
		extends AttributeContainer.Builder<Builder>
	{
		private Content content;

		public Builder content(Content content) {
			this.content = content;
			return this;
		}

		public Builder content(Optional<Content> content) {
			this.content = content.get();
			return this;
		}

		public Builder content(String content) {
			return content(Content.fromString(content));
		}

		public Builder content(InputStream inputStream)  {
			return content(Content.fromStream(inputStream));
		}

		public Builder xmlContent(String xmlContent) {
			return content(Content.fromString(xmlContent, Content.Type.XML_UTF8));
		}

		public Builder xmlContent(Node xmlContent) {
			return content(Content.fromNode(xmlContent, Content.Type.XML_UTF8));
		}


		public Builder contentFrom(Entity entity) {
			this.content = entity.getContent().orElse(null);
			return this;
		}

		public Builder copyOf(Entity a){
			return copyOf(a, (attribute)->true);
		}

		public Builder copyOf(Entity e,
				Predicate<Attribute> f){
			Preconditions.checkNotNull(e);
			contentFrom(e);
			attributes(e.getAttributes()
					.stream()
					.filter(f)
					.collect(Collectors.toList()));
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public Entity build(){
			return new Entity(this);
		}
	}
}
