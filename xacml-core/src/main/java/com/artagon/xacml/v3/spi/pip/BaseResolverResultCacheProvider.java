package com.artagon.xacml.v3.spi.pip;

import java.util.List;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.google.common.base.Preconditions;

public class BaseResolverResultCacheProvider implements ResolverResultCacheProvider
{

	@Override
	public final Content getContent(ResolverContext context) {
		ContentResolverDescriptor d = (ContentResolverDescriptor)context.getDescriptor();
		return d.isCachable()?doGetContent(d, context.getKeys()):null;
	}
	
	@Override
	public final void putContent(ResolverContext context, Content content) {
		ContentResolverDescriptor d = (ContentResolverDescriptor)context.getDescriptor();
		Preconditions.checkArgument(context.getDescriptor() == content.getDescriptor());
		if(d.isCachable()){
			doPutContent(d, context.getKeys(), content);
		}
	}

	@Override
	public final AttributeSet getAttributes(ResolverContext context) {
		AttributeResolverDescriptor d = (AttributeResolverDescriptor)context.getDescriptor();
		return d.isCachable()?doGetAttributes(d, context.getKeys()):null;
	}

	@Override
	public final void putAttributes(ResolverContext context, AttributeSet v) {
		AttributeResolverDescriptor d = (AttributeResolverDescriptor)context.getDescriptor();
		Preconditions.checkArgument(context.getDescriptor() == v.getDescriptor());
		if(d.isCachable()){
			doPutAttributes(d, context.getKeys(), v);
		}
	}
	
	protected Content doGetContent(ContentResolverDescriptor d, List<BagOfAttributeValues> keys){
		return null;
	}
	
	protected AttributeSet doGetAttributes(AttributeResolverDescriptor d,
			List<BagOfAttributeValues> keys) {
		return null;
	}
	
	protected void doPutContent(ContentResolverDescriptor d, List<BagOfAttributeValues> keys,
			Content content) {	
	}
	
	protected void doPutAttributes(AttributeResolverDescriptor d, List<BagOfAttributeValues> keys,
			AttributeSet v) {
		
	}
}
