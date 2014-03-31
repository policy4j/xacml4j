package org.xacml4j.v30.spi.pip;

import java.util.LinkedList;
import java.util.List;

import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public final class ContentResolverDescriptorBuilder
{
	private String id;
	private String name;
	private CategoryId category;
	private List<AttributeReferenceKey> keys;
	private int cacheTTL;

	private ContentResolverDescriptorBuilder(String id, String name, CategoryId category)
	{
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.id = id.replace(":", ".");
		this.name = name;
		this.category = category;
		this.keys = new LinkedList<AttributeReferenceKey>();
	}

	public static ContentResolverDescriptorBuilder bulder(String id, String name, CategoryId category){
		return new ContentResolverDescriptorBuilder(id, name, category);
	}

	public ContentResolverDescriptorBuilder designatorRef(CategoryId category,
			String attributeId, AttributeExpType dataType, String issuer)
	{
		this.keys.add(AttributeDesignatorKey
				.builder()
				.category(category)
				.dataType(dataType)
				.attributeId(attributeId)
				.issuer(issuer)
				.build());
		return this;
	}

	public ContentResolverDescriptorBuilder selectorRef(
			CategoryId category,
			String xpath, AttributeExpType dataType,
			String contextAttributeId)
	{
		this.keys.add(AttributeSelectorKey
				.builder()
				.category(category)
				.xpath(xpath)
				.dataType(dataType)
				.contextSelectorId(contextAttributeId)
				.build());
		return this;
	}

	public ContentResolverDescriptorBuilder keys(Iterable<AttributeReferenceKey> keys){
		Iterables.addAll(this.keys, keys);
		return this;
	}

	public ContentResolverDescriptorBuilder noCache(){
		this.cacheTTL = -1;
		return this;
	}

	public ContentResolverDescriptorBuilder cache(int ttl){
		this.cacheTTL = ttl;
		return this;
	}

	public ContentResolverDescriptor build(){
		return new ContentResolverDescriptorImpl();
	}

	public class ContentResolverDescriptorImpl
		extends BaseResolverDescriptor implements ContentResolverDescriptor
	{

		public ContentResolverDescriptorImpl() {
			super(id, name, category, keys, cacheTTL);
		}

		@Override
		public boolean canResolve(CategoryId category) {
			return getCategory().equals(category);
		}
	}
}
