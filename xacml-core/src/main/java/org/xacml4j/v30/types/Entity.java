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


import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.w3c.dom.Node;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.xacml4j.v30.*;

/**
 * An entity represents a collection of related attributes
 *
 * @author Giedrius Trumpickas
 */
public final class Entity extends Value.SelfValue<Entity>
{
	private static final long serialVersionUID = 6188174758603655643L;

	private final Content content;
	private final AttributeIndex attributeIndex;

	private transient int hashCode = -1;

	private Entity(Builder b) {
		super(XacmlTypes.ENTITY);
		this.content = b.content;
		this.attributeIndex = b.attributesBuilder.build();
	}

	private Entity(Content content, AttributeIndex index) {
		super(XacmlTypes.ENTITY);
		this.content = content;
		this.attributeIndex = index != null && !index.isEmpty()?index:AttributeIndex.empty();
	}

	public static Builder builder(){
		return new Builder();
	}

	public <T extends Value<T>> Collection<T> findValues(String id, ValueType type, String issuer){
		return attributeIndex.findValues(id, issuer, type);
	}

	public boolean hasValues(String id, ValueType type, String issuer){
		return attributeIndex.hasValues(id, issuer, type);
	}
	public boolean hasValues(String id, ValueType type){
		return attributeIndex.hasValues(id, null, type);
	}


	public Collection<Attribute> attributes(){
		return attributeIndex.attributes();
	}

	public boolean hasAttributes(){
		return !attributeIndex.isEmpty();
	}

	public Collection<Attribute> find(Predicate<Attribute> predicate){
		return attributeIndex.find(predicate);
	}

	public Collection<Attribute> find(String id){
		return attributeIndex.find(id, (String) null);
	}

	public Optional<Attribute> findFirst(Predicate<Attribute> predicate){
		return attributeIndex.findFirst(predicate);
	}

	public Collection<Attribute> find(String id, Predicate<Attribute> predicate){
		return attributeIndex.find(id, predicate);
	}

	public Collection<Attribute> find(String id, String issuer){
		return attributeIndex.find(id, issuer);
	}

	public <T extends Value<T>> Optional<T> findValue(String id, ValueType type, String issuer){
		return attributeIndex.findValue(id, type, issuer, v->true);
	}

	public <T extends Value<T>> Optional<T> findValue(String id, ValueType type){
		return findValue(id, type, null);
	}

	public <T extends Value<T>> Optional<T> findValue(String id, ValueType type,
													  String issuer, Predicate<T> predicate){
		return attributeIndex.findValue(id, type, issuer, predicate);
	}

	public <T extends Value<T>> Collection<T> findValues(String id, ValueType type){
		return findValues(id, type, null);
	}

	/**
	 * Gets entity with all include
	 * in result attributes
	 * @return {@link Entity} with all include in result attributes
	 */
	public Entity getIncludeInResult(){
		return Entity
		.builder()
		.content(content)
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


	public static Entity of(Content content, AttributeIndex index){
		return new Entity(content, index);
	}

	public static Entity ofAny(Object v, Object... params)
	{
		Entity.Builder builder = Entity.builder();
		if(v instanceof Attribute){
			builder.attribute((Attribute) v);
		}
		if(v instanceof Content){
			builder.content((Content) v);
		}
		if(v instanceof Collection){
			Collection<Object> p = (Collection<Object>)v;
			builder.attributes(fromParams(p));
		}
		builder.attributes(fromParams(params));
		return builder.build();
	}

	private static List<Attribute> fromParams(Collection<Object> params){
		return params.stream()
				.filter(p -> p != null && p instanceof Attribute)
				.map(a -> (Attribute) a).collect(Collectors.toList());

	}

	private static List<Attribute> fromParams(Object... params){
		if (params == null || params.length == 0) {
			return Collections.emptyList();
		}
		return fromParams(Arrays.asList(params));

	}

	public java.util.Optional<BagOfValues> resolve(AttributeDesignatorKey designatorKey)
    {
		Collection<Value> v = attributeIndex.findValues(designatorKey.getAttributeId(),
		                                         designatorKey.getIssuer(), designatorKey.getDataType());
		if(v.isEmpty()){
			return java.util.Optional.empty();
		}
		return java.util.Optional.of(designatorKey
				.getDataType()
				.bagBuilder()
				.attributes(v)
				.build());
	}

	public java.util.Optional<BagOfValues> resolve(AttributeSelectorKey selector){
		return content != null? content.resolve(selector, ()->this)
		                      :Optional.empty();
	}



	@Override
	public int hashCode(){
		if(hashCode == -1){
			this.hashCode = Objects.hashCode(attributeIndex, content);
		}
		return hashCode;
	}

	@Override
	protected String formatVal(Entity v) {
		return MoreObjects.toStringHelper(this)
				.add("content", v.content)
				.add("attributes", v.attributeIndex.attributes())
				.toString();
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
		return attributeIndex.equals(a.attributeIndex) &&
				Objects.equal(content, a.content) &&
				getEvaluatesTo().equals(a.getEvaluatesTo());
	}

	public final static class Builder
	{
		private Content content;

		private AttributeIndex.Builder attributesBuilder = AttributeIndex.builder();

		public Builder content(Content content) {
			this.content = content;
			return this;
		}

		public Builder content(Optional<Content> content) {
			this.content = content.orElse(null);
			return this;
		}

		public Builder attributes(Iterable<Attribute> attributes){
			attributesBuilder.attributes(attributes);
			return this;
		}

		public Builder attribute(Attribute ... attribute){
			attributesBuilder.attribute(attribute);
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
			attributes(e.attributeIndex.attributes()
					.stream()
					.filter(f)
					.collect(Collectors.toList()));
			return this;
		}

		public Entity build(){
			return new Entity(this);
		}
	}
}
