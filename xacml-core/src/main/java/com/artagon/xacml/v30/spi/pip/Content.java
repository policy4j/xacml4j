package com.artagon.xacml.v30.spi.pip;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class Content 
{
	private Node content;
	private ContentResolverDescriptor d;
	
	public Content(ContentResolverDescriptor d, 
			Node content)
	{
		Preconditions.checkNotNull(d);
		this.d = d;
		this.content = content;
	}
	
	public Node getContent(){
		return content;
	}
	
	public ContentResolverDescriptor getDescriptor(){
		return d;
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("id", d.getId())
		.add("content", DOMUtil.toString((Element)content))
		.toString();
	}
}
