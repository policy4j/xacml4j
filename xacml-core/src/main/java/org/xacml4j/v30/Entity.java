package org.xacml4j.v30;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

public class Entity extends AttributeContainer
{
	private Document content;

	private Entity(Builder b) {
		super(b);
		this.content = DOMUtil.copyNode(b.content);
	}
	
	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Gets content as {@link Node}
	 * instance
	 *
	 * @return a {@link Node} instance or {@code null}
	 */
	public Node getContent(){
		return content;
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributes", attributes)
		.add("content", (content != null)?DOMUtil.toString(content.getDocumentElement()):content)
		.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(attributes, content);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Entity)){
			return false;
		}
		Entity a = (Entity)o;
		return Objects.equal(attributes, a.attributes) &&
				DOMUtil.isEqual(content, a.content);
	}

	public static class Builder
		extends AttributeContainer.Builder<Builder>
	{
		private Node content;

		public Builder content(Node node){
			this.content = node;
			return this;
		}

		public Builder copyOf(Entity a){
			return copyOf(a, Predicates.<Attribute>alwaysTrue());
		}

		public Builder copyOf(Entity a,
				Predicate<Attribute> f){
			Preconditions.checkNotNull(a);
			content(a.getContent());
			attributes(Collections2.filter(a.getAttributes(), f));
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
