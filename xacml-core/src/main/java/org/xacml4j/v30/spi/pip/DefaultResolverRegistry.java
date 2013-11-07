package org.xacml4j.v30.spi.pip;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.EvaluationContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * TODO: Implement support for resolver with the same attributes but different issuer
 *
 * @author Giedrius Trumpickas
 */
class DefaultResolverRegistry implements ResolverRegistry
{
	private final static Logger log = LoggerFactory.getLogger(DefaultResolverRegistry.class);

	/**
	 * Resolvers index by category and attribute identifier
	 */
	private Map<AttributeCategory, Map<String, Multimap<String, AttributeResolver>>> attributeResolvers;
	private ConcurrentMap<String, AttributeResolver> attributeResolversById;

	private Multimap<String, AttributeResolver> scopedAttributeResolvers;


	private Map<AttributeCategory, ContentResolver> contentResolvers;

	private ReadWriteLock attributeResolverRWLock;
	private ReadWriteLock scopedAttributeResolverRWLock;

	private long maxWriteLockWait = 100;
	private long maxReadLockWait = 50;

	private TimeUnit timeToWaitUnits = TimeUnit.MILLISECONDS;

	/**
	 * Resolvers index by policy identifier
	 */

	private Multimap<String, ContentResolver> policyScopedContentResolvers;

	private ConcurrentMap<String, ContentResolver> contentResolversById;

	public DefaultResolverRegistry()
	{
		this.attributeResolvers = new LinkedHashMap<AttributeCategory, Map<String, Multimap<String, AttributeResolver>>>();
		this.scopedAttributeResolvers = LinkedHashMultimap.create();
		this.contentResolvers = new ConcurrentHashMap<AttributeCategory, ContentResolver>();
		this.policyScopedContentResolvers = LinkedHashMultimap.create();
		this.attributeResolversById = new ConcurrentHashMap<String, AttributeResolver>();
		this.contentResolversById = new ConcurrentHashMap<String, ContentResolver>();
		this.attributeResolverRWLock = new ReentrantReadWriteLock();
		this.scopedAttributeResolverRWLock = new ReentrantReadWriteLock();
	}

	@Override
	public void addAttributeResolver(AttributeResolver resolver)
	{
		AttributeResolverDescriptor d = resolver.getDescriptor();
		Preconditions.checkArgument(!attributeResolversById.containsKey(d.getId()),
				"Attribute resolver with id=\"{}\" is already registered with this registry", d.getId());
		Lock lock =  attributeResolverRWLock.writeLock();
		try
		{
			if(!lock.tryLock(maxWriteLockWait, timeToWaitUnits)){
				if(log.isWarnEnabled()){
					log.warn("Failed to accuire write lock in=\"{}\" {}",
							maxWriteLockWait, timeToWaitUnits.toString());
				}
				return;
			}
			Map<String, Multimap<String, AttributeResolver>> byCategory = attributeResolvers.get(d.getCategory());
			if(byCategory == null){
				byCategory = new LinkedHashMap<String, Multimap<String, AttributeResolver>>();
				attributeResolvers.put(d.getCategory(), byCategory);
			}
			Multimap<String, AttributeResolver> byIssuer = byCategory.get(d.getIssuer());
			if(byIssuer == null){
				byIssuer = LinkedHashMultimap.create();
				byCategory.put(d.getIssuer(), byIssuer);
			}
			for(String attributeId : d.getProvidedAttributeIds()){
				if(log.isDebugEnabled()){
						log.debug("Indexing resolver id=\"{}\" category=\"{}\", issuer=\"{}\" attributeId=\"{}\"",
								new Object[]{d.getId(), d.getCategory(), d.getIssuer(), attributeId});
				}
				byIssuer.put(attributeId, resolver);
			}
			attributeResolversById.put(d.getId(), resolver);
		}catch(InterruptedException e){
			if(log.isWarnEnabled()){
				log.warn("Interrupted while " +
						"waiting to accuire a write lock", e);
			}
		}
		finally{
			lock.unlock();
		}
	}


	@Override
	public void addContentResolver(ContentResolver r)
	{
		Preconditions.checkArgument(r != null);
		Preconditions.checkState(!contentResolversById.containsKey(r.getDescriptor().getId()));
		ContentResolverDescriptor d = r.getDescriptor();
		if(log.isDebugEnabled()){
			log.debug("Adding root content " +
					"resolver=\"{}\" for category=\"{}\"", d.getId(), d.getCategory());
		}
		contentResolvers.put(d.getCategory(), r);
	}

	@Override
	public void addContentResolver(String policyId, ContentResolver r)
	{
		if(policyId == null){
			addContentResolver(r);
			return;
		}
		ContentResolverDescriptor d = r.getDescriptor();
		Preconditions.checkArgument(r != null);
		Preconditions.checkState(!contentResolversById.containsKey(d.getId()));
		if(log.isDebugEnabled()){
			log.debug("Adding policyId=\"{}\" content " +
					"resolver=\"{}\" for category=\"{}\"",
					new Object[]{policyId, d.getId(), d.getCategory()});
		}
		this.policyScopedContentResolvers.put(policyId, r);
		this.contentResolversById.put(policyId, r);
	}

	@Override
	public void addAttributeResolver(String policyId, AttributeResolver r)
	{
		if(policyId == null){
			addAttributeResolver(r);
			return;
		}
		AttributeResolverDescriptor d = r.getDescriptor();
		if(log.isDebugEnabled()){
			log.debug("Adding policyId=\"{}\" scoped atttribute " +
					"resolver=\"{}\" for category=\"{}\"",
					new Object[]{policyId, d.getId(), d.getCategory()});
		}
		this.scopedAttributeResolvers.put(policyId, r);
		this.attributeResolversById.put(d.getId(), r);
	}



	@Override
	public void addAttributeResolvers(Iterable<AttributeResolver> resolvers) {
		addAttributeResolvers(null, resolvers);
	}

	@Override
	public void addAttributeResolvers(String policyId,
			Iterable<AttributeResolver> resolvers) {
		for(AttributeResolver r : resolvers){
			addAttributeResolver(policyId, r);
		}
	}

	@Override
	public void addContentResolvers(Iterable<ContentResolver> resolvers) {
		addContentResolvers(null, resolvers);
	}

	@Override
	public void addContentResolvers(String policyId,
			Iterable<ContentResolver> resolvers) {
		for(ContentResolver r : resolvers){
			addContentResolver(policyId, r);
		}
	}

	@Override
	public Iterable<AttributeResolver> getMatchingAttributeResolvers(
			EvaluationContext context,
			AttributeDesignatorKey ref){
		List<AttributeResolver> resolvers = new LinkedList<AttributeResolver>();
		findMatchingAttributeResolvers(context, ref, resolvers);
		return resolvers;
	}
	/**
	 * Finds {@link AttributeResolver} for given evaluation context and
	 * {@link AttributeResolver} instance
	 *
	 * @param context an evaluation context
	 * @param ref an attribute reference
	 */
	private void findMatchingAttributeResolvers(
			EvaluationContext context,
			AttributeDesignatorKey ref, List<AttributeResolver> found)
	{
			// stop recursive call if
			// context is null
			if(context == null)
			{
				Lock lock = attributeResolverRWLock.readLock();
				try
				{
					if(!lock.tryLock(maxReadLockWait, timeToWaitUnits)){
						if(log.isWarnEnabled()){
							log.warn("Failed to accuire read lock in=\"{}\" {}",
									maxReadLockWait, timeToWaitUnits.toString());
						}
						return;
					}
					Map<String, Multimap<String, AttributeResolver>> byIssuer = attributeResolvers.get(ref.getCategory());
					if(byIssuer == null ||
							byIssuer.isEmpty()){
						return;
					}
					if(log.isDebugEnabled()){
						log.debug("Found=\"{}\" resolvers for category=\"{}\"",
								byIssuer.size(), ref.getCategory());
					}
					for(Entry<String, Multimap<String, AttributeResolver>> e : byIssuer.entrySet()){
						for(AttributeResolver r : e.getValue().get(ref.getAttributeId())){
							AttributeResolverDescriptor d = r.getDescriptor();
							if(d.canResolve(ref)){
								if(log.isDebugEnabled()){
									log.debug("Found root resolver=\"{}\" " +
											"for a reference=\"{}\"", d.getId(), ref);
								}
								found.add(r);
							}
						}
					}
				}
				catch(InterruptedException e){
					if(log.isWarnEnabled()){
						log.warn("Interrupted while " +
								"waiting to accuire a read lock", e);
					}
				}
				finally{
					lock.unlock();
				}
				return;
			}
			String policyId = getCurrentIdentifier(context);
			if(policyId  == null){
				Preconditions.checkState(context.getParentContext() == null);
				findMatchingAttributeResolvers(null, ref, found);
				return;
			}
			Lock lock  = scopedAttributeResolverRWLock.readLock();
			try
			{
				lock.lock();
				Collection<AttributeResolver> byPolicyId = scopedAttributeResolvers.get(policyId);
				if(log.isDebugEnabled()){
					log.debug("Found \"{}\" resolver " +
							"scoped for a PolicyId=\"{}\"",
							byPolicyId.size(), policyId);
				}
				for(AttributeResolver r : byPolicyId){
					AttributeResolverDescriptor d = r.getDescriptor();
					if(d.canResolve(ref)){
						if(log.isDebugEnabled()){
							log.debug("Found PolicyId=\"{}\" " +
									"scoped resolver for reference=\"{}\"",
									policyId, ref);
						}
						found.add(r);
					}
				}
				findMatchingAttributeResolvers(context.getParentContext(), ref, found);
			}finally{
				lock.unlock();
			}
	}


	/**
	 * Gets matching content resolver for a given
	 * evaluation context and attribute category
	 *
	 * @param context an evaluation context
	 * @param category an attribute category
	 * @return {@link ContentResolver} or {@code null}
	 *
	 */
	@Override
	public ContentResolver getMatchingContentResolver(EvaluationContext context,
			AttributeCategory category)
	{
		// stop recursive call if
		// context is null
		if(context == null){
			return contentResolvers.get(category);
		}
		String policyId = getCurrentIdentifier(context);
		Collection<ContentResolver> found = policyScopedContentResolvers.get(policyId);
		if(log.isDebugEnabled()){
			log.debug("Found \"{}\" resolver " +
					"scoped for a PolicyId=\"{}\"",
					found.size(), policyId);
		}
		for(ContentResolver r : found){
			ContentResolverDescriptor d = r.getDescriptor();
			if(d.canResolve(category)){
				if(log.isDebugEnabled()){
					log.debug("Found PolicyId=\"{}\" scoped resolver", policyId);
				}
				return r;
			}
		}
		return getMatchingContentResolver(context.getParentContext(), category);
	}

	/**
	 * Gets current policy or policy set identifier
	 *
	 * @param context an evaluation context
	 * @return current policy or policy set identifier
	 */
	private String getCurrentIdentifier(EvaluationContext context)
	{
		CompositeDecisionRule currentPolicy = context.getCurrentPolicy();
		if(currentPolicy == null){
			CompositeDecisionRule currentPolicySet = context.getCurrentPolicySet();
			return currentPolicySet != null?currentPolicySet.getId():null;
		}
		return currentPolicy.getId();
	}

	@Override
	public AttributeResolver getAttributeResolver(String id) {
		return attributeResolversById.get(id);
	}

	@Override
	public ContentResolver getContentResolver(String id) {
		return contentResolversById.get(id);
	}
}
