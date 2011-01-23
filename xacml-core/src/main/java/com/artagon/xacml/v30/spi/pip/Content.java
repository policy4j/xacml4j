package com.artagon.xacml.v30.spi.pip;

import org.w3c.dom.Node;

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
}
