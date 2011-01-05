package com.artagon.xacml.v3;

import java.util.Arrays;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Attributes extends BaseAttributeHolder
{
	private String id;
	private AttributeCategory categoryId;
	private Document content;
	
	/**
	 * Constructs an attributes with a given identifier,
	 * category, attributes and XML content.
	 * XML content is deep copied from a given
	 * content node
	 * 
	 * @param id an optional unique identifier
	 * @param categoryId an attribute category
	 * @param content an optional XML content
	 * @param attributes a collection of attributes
	 */
	public Attributes(
			String id, 
			AttributeCategory categoryId, 
			Node content, 
			Iterable<Attribute> attrs){
		super(attrs);
		Preconditions.checkNotNull(categoryId);
		Preconditions.checkNotNull(attrs);
		this.id = id;
		this.categoryId = categoryId;
		this.content = DOMUtil.copyNode(content);
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategory, Node, Collection)
	 */
	public Attributes(String id, AttributeCategory categoryId, 
			Node content, Attribute ...attributes){
		this(id, categoryId, content, Arrays.asList(attributes));
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategory, Node, Collection)
	 */
	public Attributes(AttributeCategory categoryId, 
			Iterable<Attribute> attributes){
		this(null, categoryId, null, attributes);
	}
	
	public Attributes(AttributeCategory categoryId, 
			Node content,
			Iterable<Attribute> attributes){
		this(null, categoryId, content, attributes);
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategory, Node, Collection)
	 */
	public Attributes(String id, 
			AttributeCategory categoryId, 
			Iterable<Attribute> attributes){
		this(id, categoryId, null, attributes);
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategory, Node, Collection)
	 */
	public Attributes(String id, AttributeCategory categoryId, 
			Attribute ...attributes){
		this(id, categoryId, null, Arrays.asList(attributes));
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategory, Node, Collection)
	 */
	public Attributes(AttributeCategory categoryId, 
			Attribute ...attributes){
		this(null, categoryId, null, Arrays.asList(attributes));
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
}
