package org.xacml4j.v30.spi.pip;

import com.google.common.collect.ImmutableSet;
import org.xacml4j.util.Collections;
import org.xacml4j.v30.*;

import java.util.*;
import java.util.function.Function;

public abstract class ResolverDescriptorBuilder<T extends ResolverDescriptorBuilder<?>>
{
    protected String id;
    protected String name;
    protected Set<CategoryId> allCategories;
    protected int cacheTTL;
    protected List<java.util.function.Function<ResolverContext, Optional<BagOfAttributeValues>>> contextKeysResolutionPlan;
    protected List<AttributeReferenceKey> contextReferenceKeys;

    protected ResolverDescriptorBuilder(String id, String name,
                                        CategoryId categoryId, CategoryId ... additionalCategories)
    {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(categoryId, "name");
        this.id = id.replace(":", ".");
        this.name = name;
        this.allCategories = ImmutableSet.copyOf(additionalCategories != null?
                Collections.expand(Arrays.asList(additionalCategories), categoryId):Arrays.asList(categoryId));
        this.contextKeysResolutionPlan = new LinkedList<>();
        this.contextReferenceKeys = new LinkedList<>();
    }

    public T cache(int ttl){
        this.cacheTTL = ttl;
        return getThis();
    }

    protected T contextRefOrElse(
            AttributeReferenceKey a,
                       AttributeReferenceKey b){
        this.contextReferenceKeys.add(a);
        this.contextReferenceKeys.add(b);
        return orElse((context -> context.resolve(a)),
                (context -> context.resolve(b)));
    }

    public T contextRef(AttributeReferenceKey a){
        this.contextReferenceKeys.add(a);
        this.contextKeysResolutionPlan.add((context -> context.resolve(a)));
        return getThis();
    }

    protected T orElse(Function<ResolverContext, Optional<BagOfAttributeValues>> a,
                       Function<ResolverContext, Optional<BagOfAttributeValues>> b){
        this.contextKeysResolutionPlan.add(
                (context)->{
                    return a.apply(context).or(()->b.apply(context));
                });
        return getThis();
    }

    public T noCache(){
        this.cacheTTL = -1;
        return getThis();
    }

    protected abstract T getThis();
}
