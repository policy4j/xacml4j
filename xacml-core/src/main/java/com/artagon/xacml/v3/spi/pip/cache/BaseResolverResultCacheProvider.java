package com.artagon.xacml.v3.spi.pip.cache;

import java.util.List;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.AttributeSet;
import com.artagon.xacml.v3.spi.pip.ContentResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.ResolverResultCacheProvider;

public class BaseResolverResultCacheProvider implements ResolverResultCacheProvider
{

	@Override
	public final Node get(ContentResolverDescriptor d, 
			List<BagOfAttributeValues> keys) {
		return d.isCachable()?doGetContent(d, keys):null;
	}
	
	@Override
	public final void put(ContentResolverDescriptor d, List<BagOfAttributeValues> keys,
			Node content) {
		if(d.isCachable()){
			doPut(d, keys, content);
		}
	}

	@Override
	public final AttributeSet get(AttributeResolverDescriptor d,
			List<BagOfAttributeValues> keys) {
		return d.isCachable()?doGet(d, keys):null;
	}

	@Override
	public final void put(AttributeResolverDescriptor d, List<BagOfAttributeValues> keys,
			AttributeSet v) {
		if(d.isCachable()){
			doPut(d, keys, v);
		}
	}
	
	protected Node doGetContent(ContentResolverDescriptor d, List<BagOfAttributeValues> keys){
		return null;
	}
	
	protected AttributeSet doGet(AttributeResolverDescriptor d,
			List<BagOfAttributeValues> keys) {
		return null;
	}
	
	protected void doPut(ContentResolverDescriptor d, List<BagOfAttributeValues> keys,
			Node content) {	
	}
	
	protected void doPut(AttributeResolverDescriptor d, List<BagOfAttributeValues> keys,
			AttributeSet v) {
		
	}
}
