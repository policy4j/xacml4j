package com.artagon.xacml.v3.spi.pip;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.CacheProvider;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A default implementation of {@link PolicyInformationPoint}
 * 
 * @author Giedrius Trumpickas
 */
public class DefaultPolicyInformationPoint 
	implements PolicyInformationPoint
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyInformationPoint.class);
	
	private CacheProvider attributeCache;
	private CacheProvider contentCache;
	private ResolverRegistry resolvers;
	
	public DefaultPolicyInformationPoint(ResolverRegistry resolvers, 
			CacheProvider attributesCache, CacheProvider contentCache)
	{
		Preconditions.checkNotNull(attributesCache);
		Preconditions.checkNotNull(contentCache);
		Preconditions.checkNotNull(resolvers);
		this.attributeCache = attributesCache;
		this.contentCache = contentCache;
		this.resolvers = resolvers;
	}

	@Override
	public BagOfAttributeValues resolve(final EvaluationContext context,
			AttributeDesignatorKey ref) throws Exception 
	{
		AttributeResolver r = resolvers.getAttributeResolver(context, ref);
		if(r == null){
			return ref.getDataType().emptyBag();
		}
		AttributeResolverDescriptor d = r.getDescriptor();
		Preconditions.checkState(d.canResolve(ref));
		BagOfAttributeValues[] keys = d.resolveKeys(context);
		CacheKey key = new CacheKey(d.getId(), keys);
		Map<String, BagOfAttributeValues> attributes = null;
		if(d.isCachable()){
			attributes = attributeCache.get(key);
			if(attributes != null && 
					log.isDebugEnabled()){
				log.debug("Attributes " +
						"cache hit for key=\"{}\" values=\"{}\"", 
						key, attributes);
			}
		}
		if(attributes != null){
			return attributes.get(ref.getAttributeId());
		}
		attributes = r.resolve(
				new DefaultPolicyInformationPointContext(context, d, keys));
		if(d.isCachable()){
			if(log.isDebugEnabled()){
				log.debug("Caching attributes " +
						"resolver id=\"{}\", ttl=\"{}\"", 
						d.getId(), d.getPreferreredCacheTTL());
			}
			attributeCache.put(key, attributes, d.getPreferreredCacheTTL());
		}
		return attributes.get(ref.getAttributeId());
	}

	@Override
	public Node resolve(final EvaluationContext context, 
			AttributeCategory category)
			throws Exception 
	{
		ContentResolver r = resolvers.getContentResolver(context, category);
		if(r == null){
			return null;
		}
		ContentResolverDescriptor d = r.getDescriptor();
		BagOfAttributeValues[] keys = d.resolveKeys(context);
		CacheKey cacheKey = new CacheKey(d.getId(), keys);
		Node v = null;
		if(d.isCachable()){
			v = contentCache.get(cacheKey);
			if(v != null){
				return v;
			}
		}
		v = r.resolve(
				new DefaultPolicyInformationPointContext(context, d, keys));
		if(d.isCachable()){
			contentCache.put(cacheKey, 
					v, d.getPreferreredCacheTTL());
		}
		Preconditions.checkState(v != null);
		return v;
	}
	

	public final class CacheKey implements Serializable
	{
		private static final long serialVersionUID = -6895205924708410228L;
		
		private String resolverId;
		private BagOfAttributeValues[] keys;
		
		public CacheKey(String resolverId, 
				BagOfAttributeValues[] keys){
			Preconditions.checkNotNull(resolverId);
			this.resolverId = resolverId;
			this.keys = keys;
		}
		
		@Override
		public int hashCode(){
			return Objects.hashCode(resolverId, keys);
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o == this){
				return true;
			}
			if(o == null){
				return false;
			}
			if(!(o instanceof CacheKey)){
				return false;
			}
			CacheKey k = (CacheKey)o;
			return resolverId.equals(k.resolverId) && 
			Arrays.equals(keys, k.keys);
		}
		
		@Override
		public String toString(){
			return Objects.toStringHelper(this)
			.add("id", resolverId)
			.add("keys", Arrays.toString(keys))
			.toString();
		}
	}

}
