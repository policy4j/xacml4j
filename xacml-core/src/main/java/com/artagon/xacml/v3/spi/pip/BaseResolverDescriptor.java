package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.List;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeReferenceKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.google.common.base.Preconditions;

public abstract class BaseResolverDescriptor 
	implements ResolverDescriptor 
{
	private String id;
	private String name;

	private AttributeCategory category;
	private List<AttributeReferenceKey> requestContextKeys;
	private long preferredCacheTTL;
	
	protected BaseResolverDescriptor(String id,
			String name,
			AttributeCategory category,
			List<AttributeReferenceKey> keys){
		this(id, name, category, keys, 0);
	}
	
	protected BaseResolverDescriptor(
			String id,
			String name,
			AttributeCategory category,
			List<AttributeReferenceKey> keys, 
			long preferredCacheTTL) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.id = id;
		this.name = name;
		this.category = category;
		this.requestContextKeys = Collections.unmodifiableList(keys);
		this.preferredCacheTTL = preferredCacheTTL;
	}
	
	@Override
	public final String getId(){
		return id;
	}
	
	@Override
	public final String getName(){
		return name;
	}
	
	
	@Override
	public boolean isCachingEnabled() {
		return preferredCacheTTL > 0;
	}

	@Override
	public long getPreferreredCacheTTL() {
		return preferredCacheTTL;
	}

	@Override
	public final AttributeCategory getCategory() {
		return category;
	}

	@Override
	public final BagOfAttributeValues[] resolveKeys(EvaluationContext context)
			throws EvaluationException 
	{
		BagOfAttributeValues[] keys = new BagOfAttributeValues[requestContextKeys.size()];
		int i  = 0;
		for(AttributeReferenceKey ref : requestContextKeys){
			BagOfAttributeValues k = ref.resolve(context);
			keys[i++] = k;
		}
		return keys;
	}
}
