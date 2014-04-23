package org.xacml4j.v30.spi.pip;

import java.util.List;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.CategoryId;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public abstract class BaseResolverDescriptor
	implements ResolverDescriptor
{
	private String id;
	private String name;

	private CategoryId category;
	private List<AttributeReferenceKey> keyRefs;
	private int cacheTTL;

	protected BaseResolverDescriptor(String id,
			String name,
			CategoryId category,
			List<AttributeReferenceKey> keys){
		this(id, name, category, keys, 0);
	}

	protected BaseResolverDescriptor(
			String id,
			String name,
			CategoryId category,
			List<AttributeReferenceKey> keys,
			int preferredCacheTTL) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.id = id;
		this.name = name;
		this.category = category;
		this.keyRefs = ImmutableList.copyOf(keys);
		this.cacheTTL = (preferredCacheTTL < 0)?0:preferredCacheTTL;
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
	public boolean isCachable() {
		return cacheTTL > 0;
	}

	@Override
	public int getPreferreredCacheTTL() {
		return cacheTTL;
	}

	@Override
	public final CategoryId getCategory() {
		return category;
	}

	@Override
	public List<AttributeReferenceKey> getKeyRefs() {
		return keyRefs;
	}
}
