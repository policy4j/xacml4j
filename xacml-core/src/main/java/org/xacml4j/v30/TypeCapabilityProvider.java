package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A capabilityType capability factory aka provider {@see ServiceLoader}
 *
 * @param <T>
 * @author Giedrius Trumpickas
 */
public interface TypeCapabilityProvider<T extends TypeCapability>
{
    /**
     * Gets capability java capabilityType as
     * {@link Class<T>}  defaultProvider
     *
     */
    Class<T> getCapabilityType();

    /**
     * Gets get capability for a given type
     *
     * @param type a XACML capabilityType
     * @return optional capability defaultProvider
     */
    Optional<T> forType(AttributeValueType type);

    /**
     * Gets all capablities provided by this provider
     *
     * @return collection of capabilities
     */
    Collection<T> getCapabilities();

    /**
     * Base class for implementing {@link TypeCapabilityProvider}
     *
     * @param <T> a capability capabilityType
     */
    class Provider<T extends TypeCapability> implements
            TypeCapabilityProvider<T> {

        private Map<AttributeValueType, T> capabilitiesByType;
        private Class<T> capabilityType;

        /**
         *
         * @param systemCapabilities a standard system capabilities for standard types
         * @param discoveredCapabilities discovered capabilities
         * @param capabilityType a capability type
         */
        protected Provider(Collection<T> systemCapabilities,
                           Collection<T> discoveredCapabilities,
                           Class<T> capabilityType){
            this.capabilityType = capabilityType;
            this.capabilitiesByType = Streams.concat(
                    systemCapabilities.stream(),
                    discoveredCapabilities.stream())
                    .collect(ImmutableMap.toImmutableMap(c->c.getType(), c->c, (a, b)->a));
        }

        public final Class<T> getCapabilityType(){
            return capabilityType;
        }

        public Collection<T> getCapabilities(){
            return capabilitiesByType.values();
        }

        public final Optional<T> forType(AttributeValueType type){
            return Optional.ofNullable(
                    capabilitiesByType.get(type));
        }

        /**
         * Discover {@link TypeCapabilityProvider} for specific {@link TypeCapability} implementations
         * via JDK {@link ServiceLoader} infrastucture and java modules
         * @param capabilityType
         * @param <T>
         * @return
         */
        static <T extends TypeCapability> Collection<T> discoverFrom(Class<T> capabilityType,
                                                                     Supplier<ServiceLoader<? extends TypeCapabilityProvider>> serviceLoaderSupplier){
            return serviceLoaderSupplier
                    .get()
                    .stream()
                    .map(p->p.get())
                    .filter(p->p.getCapabilityType().equals(capabilityType))
                    .map(c->capabilityType.cast(c))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Discover {@link TypeCapabilityProvider} for specific {@link TypeCapability} implementations
     * via JDK {@link ServiceLoader}  and java modules

     * @param capabilityType
     * @param <T>
     * @return
     */
    static <T extends TypeCapability> Collection<T> discover(Class<T> capabilityType, ClassLoader cl){
        return Provider.discoverFrom(capabilityType, ()->ServiceLoader.load(TypeCapabilityProvider.class, cl));
    }

    /**
     * Discover {@link TypeCapabilityProvider} for specific {@link TypeCapability} implementations
     * via JDK {@link ServiceLoader} and java modules

     * @param capabilityType
     * @param <T>
     * @return
     */
    static <T extends TypeCapability> Collection<T> discover(Class<T> capabilityType){
        return Provider.discoverFrom(capabilityType, ()->ServiceLoader.load(TypeCapabilityProvider.class));
    }

}
