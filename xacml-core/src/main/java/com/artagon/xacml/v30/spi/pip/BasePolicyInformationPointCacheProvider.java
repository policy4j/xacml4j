package com.artagon.xacml.v30.spi.pip;

import java.util.List;

import com.artagon.xacml.v30.BagOfAttributeExp;
import com.google.common.base.Preconditions;

public class BasePolicyInformationPointCacheProvider implements PolicyInformationPointCacheProvider
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
		Preconditions.checkArgument(d.getId().equals(v.getDescriptor().getId()));
		if(d.isCachable()){
			doPutAttributes(d, context.getKeys(), v);
		}
	}
	
	protected Content doGetContent(ContentResolverDescriptor d, List<BagOfAttributeExp> keys){
		return null;
	}
	
	protected AttributeSet doGetAttributes(AttributeResolverDescriptor d,
			List<BagOfAttributeExp> keys) {
		return null;
	}
	
	protected void doPutContent(ContentResolverDescriptor d, List<BagOfAttributeExp> keys,
			Content content) {	
	}
	
	protected void doPutAttributes(AttributeResolverDescriptor d, List<BagOfAttributeExp> keys,
			AttributeSet v) {
		
	}
}
