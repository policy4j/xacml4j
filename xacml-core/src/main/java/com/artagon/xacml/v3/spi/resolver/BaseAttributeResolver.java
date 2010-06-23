package com.artagon.xacml.v3.spi.resolver;

import java.util.Collections;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.spi.AttributeResolver;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public abstract class BaseAttributeResolver implements AttributeResolver
{
	private String issuer;
	private Set<String> providedAttributeIds;
	private Set<AttributeCategoryId> providedAttributeCategories;
	
	protected BaseAttributeResolver(String issuer, 
			Set<AttributeCategoryId> providedCategories, 
			Set<String> providedAttributes){
		Preconditions.checkArgument(providedCategories != null);
		Preconditions.checkArgument(providedCategories.size() > 0);
		Preconditions.checkArgument(providedAttributes != null);
		Preconditions.checkArgument(providedAttributes.size() > 0);
		this.issuer = issuer;
		this.providedAttributeCategories = Sets.immutableEnumSet(providedCategories);
		this.providedAttributeIds = Collections.unmodifiableSet(providedAttributes);
	}
	
	protected BaseAttributeResolver(
			Set<AttributeCategoryId> providedCategories, 
			Set<String> providedAttributes){
		this(null, providedCategories, providedAttributes);
	}
	
	@Override
	public final String getIssuer() {
		return issuer;
	}

	@Override
	public final Set<String> getProvidedAttributes() {
		return providedAttributeIds;
	}

	@Override
	public final Set<AttributeCategoryId> getProvidedCategories() {
		return providedAttributeCategories;
	}	
}
