package org.xacml4j.v30.spi.pip;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.CategoryId;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * A interface for XACML policy data resolvers
 *
 * @param <T>
 * @author Giedrius Trumpickass
 */
public interface Resolver<T> extends Function<ResolverContext, Optional<T>>
{
    /**
     * @see {@link ResolverDescriptor#getId()}
     */
    default String getId(){
        return getDescriptor().getId();
    }

    /**
     * @see {@link ResolverDescriptor#getSupportedCategories()}
     */
    default boolean isCategorySupported(CategoryId categoryId){
        return getDescriptor().getSupportedCategories().contains(categoryId);
    }
    
    /**
     * @see {@link ResolverDescriptor#getSupportedCategories()}
     */
    default Collection<CategoryId> getSupportedCategories(){
        return getDescriptor().getSupportedCategories();
    }

    /**
     * @see {@link ResolverDescriptor#canResolve(AttributeReferenceKey)}
     */
    default boolean canResolve(AttributeReferenceKey referenceKey){
        return getDescriptor().canResolve(referenceKey);
    }

    /**
     * Gets resolver descriptor
     *
     * @return resolver descriptor
     */
    <D extends ResolverDescriptor> D getDescriptor();

    /**
     * Resolves value via this resolver
     *
     * @param resolverContext a resolver context
     * @return {@link Optional<V>}
     */
    Optional<T> resolve(ResolverContext resolverContext);

    @Override
    default Optional<T> apply(ResolverContext resolverContext) {
        return resolve(resolverContext);
    }
}
