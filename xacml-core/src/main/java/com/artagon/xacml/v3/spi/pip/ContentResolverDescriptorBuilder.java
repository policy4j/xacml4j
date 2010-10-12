package com.artagon.xacml.v3.spi.pip;

import java.util.HashSet;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategory;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public final class ContentResolverDescriptorBuilder 
{
	private Set<AttributeCategory> categories;
	
	private ContentResolverDescriptorBuilder(){
		this.categories = new HashSet<AttributeCategory>();
	}
	
	public static ContentResolverDescriptorBuilder create(){
		return new ContentResolverDescriptorBuilder();
	}
	
	public ContentResolverDescriptorBuilder category(AttributeCategory category)
	{
		Preconditions.checkArgument(category != null);
		this.categories.add(category);
		return this;
	}
	
	public ContentResolverDescriptor build(){
		return new ContentResolverDescriptor() {
			
			private Set<AttributeCategory> categories;
			
			{
				this.categories = ImmutableSet.copyOf(ContentResolverDescriptorBuilder.this.categories);
			}
			
			@Override
			public Set<AttributeCategory> getSupportedCategories() {
				return categories;
			}
			
			@Override
			public boolean canResolve(AttributeCategory category) {
				return categories.contains(category);
			}
		};
	}
}
