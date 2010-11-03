package com.artagon.xacml.v3.spi.pip;

import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeReferenceKey;
import com.google.common.base.Preconditions;

public final class ContentResolverDescriptorBuilder 
{
	private String id;
	private String name;
	private AttributeCategory category;
	private List<AttributeReferenceKey> requestContextKeys;
	
	private ContentResolverDescriptorBuilder(String id, String name, AttributeCategory category)
	{
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.id = id;
		this.name = name;
		this.category = category;
		this.requestContextKeys = new LinkedList<AttributeReferenceKey>();
	}
	
	public static ContentResolverDescriptorBuilder create(String id, String name, AttributeCategory category){
		return new ContentResolverDescriptorBuilder(id, name, category);
	}
	
	public ContentResolverDescriptor build(){
		return new ContentResolverDescriptorImpl(id, name, category, requestContextKeys);
	}
	
	public class ContentResolverDescriptorImpl 
		extends BaseResolverDescriptor implements ContentResolverDescriptor
	{

		public ContentResolverDescriptorImpl(String id, String name,
				AttributeCategory category,
				List<AttributeReferenceKey> keys) {
			super(id, name, category, keys);
		}

		@Override
		public boolean canResolve(AttributeCategory category) {
			return getCategory().equals(category);
		}
	}
}
