package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2021 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.MoreObjects;

/**
 * A interface for XACML policy data resolvers
 *
 * @param <T>
 * @author Giedrius Trumpickass
 */
public final class Resolver<T> implements Function<ResolverContext, Optional<T>>
{
    private ResolverDescriptor<T> descriptor;

    public Resolver(ResolverDescriptor<T> descriptor){
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    /**
     * @see {@link ResolverDescriptor#getId()}
     */
    public final String getId(){
        return descriptor.getId();
    }

    /**
     * Gets resolver descriptor
     *
     * @return resolver descriptor
     */
    public ResolverDescriptor<T> getDescriptor(){
        return descriptor;
    }

    /**
     * Resolves value via this resolver
     *
     * @param resolverContext a resolver context
     * @return {@link Optional<V>}
     */
    public Optional<T> resolve(ResolverContext resolverContext){
        return descriptor.getResolverFunction()
                .apply(resolverContext);
    }

    public Optional<T> apply(ResolverContext resolverContext) {
        return resolve(resolverContext);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("descriptor", descriptor)
                .toString();
    }
}
