package com.artagon.xacml.v30;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Attributes extends AttributeContainer
{
	private String id;
	private AttributeCategory categoryId;
	private Document content;

	private Attributes(Builder b){
		super(b);
		this.id = b.id;
		Preconditions.checkNotNull(b.category);
		this.categoryId = b.category;
		this.content = DOMUtil.copyNode(b.content);
	}

	public static Builder builder(AttributeCategory category){
		return new Builder(category);
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * An unique identifier of the attribute in
	 * the request context
	 *
	 * @return unique identifier of the
	 * attribute in the request context
	 */
	public String getId(){
		return id;
	}

	/**
	 * Gets content as {@link Node}
	 * instance
	 *
	 * @return a {@link Node} instance
	 * or <code>null</code>
	 */
	public Node getContent(){
		return content;
	}

	/**
	 * Gets an attribute category
	 *
	 * @return attribute category
	 */
	public AttributeCategory getCategory(){
		return categoryId;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("category", categoryId)
		.add("id", id)
		.add("attributes", attributes)
		.add("content", (content != null)?DOMUtil.toString(content.getDocumentElement()):content)
		.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(categoryId, id, content, attributes);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Attributes)){
			return false;
		}
		Attributes a = (Attributes)o;
		return Objects.equal(categoryId, a.categoryId) &&
		Objects.equal(id, a.id) &&
		DOMUtil.isEqual(content, a.content) &&
		Objects.equal(attributes, a.attributes);
	}

	public static class Builder
		extends AttributeContainerBuilder<Builder>
	{
		private String id;
		private AttributeCategory category;
		private Node content;

		private Builder(AttributeCategory category){
			Preconditions.checkNotNull(category);
			this.category = category;
		}

		private Builder(){
		}

		public Builder content(Node node){
			this.content = node;
			return this;
		}

		public Builder copyOf(Attributes a){
			Preconditions.checkNotNull(a);
			id(a.getId());
			content(a.getContent());
			category(a.getCategory());
			attributes(a.getAttributes());
			return this;
		}

		public Builder id(String id){
			this.id = id;
			return this;
		}

		public Builder category(AttributeCategory category){
			Preconditions.checkNotNull(category);
			this.category = category;
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public Attributes build(){
			return new Attributes(this);
		}
	}
}
