package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.BagOfAttributeValues;

public class BaseResolverResultCacheProvider implements ResolverResultCacheProvider
{

	@Override
	public final Node get(ContentResolverDescriptor d, 
			BagOfAttributeValues[] keys) {
		return d.isCachable()?doGetContent(d, keys):null;
	}
	
	@Override
	public final void put(ContentResolverDescriptor d, BagOfAttributeValues[] keys,
			Node content) {
		if(d.isCachable()){
			doPut(d, keys, content);
		}
	}

	@Override
	public final AttributeSet get(AttributeResolverDescriptor d,
			BagOfAttributeValues[] keys) {
		return d.isCachable()?doGet(d, keys):null;
	}

	@Override
	public final void put(AttributeResolverDescriptor d, BagOfAttributeValues[] keys,
			AttributeSet v) {
		if(d.isCachable()){
			doPut(d, keys, v);
		}
	}
	
	protected Node doGetContent(ContentResolverDescriptor d, BagOfAttributeValues[] keys){
		return null;
	}
	
	protected AttributeSet doGet(AttributeResolverDescriptor d,
			BagOfAttributeValues[] keys) {
		return null;
	}
	
	protected void doPut(ContentResolverDescriptor d, BagOfAttributeValues[] keys,
			Node content) {	
	}
	
	protected void doPut(AttributeResolverDescriptor d, BagOfAttributeValues[] keys,
			AttributeSet v) {
		
	}
}
