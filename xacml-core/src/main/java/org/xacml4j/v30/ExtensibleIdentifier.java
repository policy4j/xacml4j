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


import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import org.slf4j.LoggerFactory;
import org.xacml4j.util.Pair;
import org.xacml4j.util.Reflections;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Represents an extensible XACML identifier
 *
 * @author Giedrius Trumpickas
 */
public class ExtensibleIdentifier implements Serializable
{
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(ExtensibleIdentifier.class);

    protected String id;
    protected String abbreviatedId;

    protected ExtensibleIdentifier(String id, String abbreviatedId){
        this.id = Objects.requireNonNull(id);
        this.abbreviatedId = Objects.requireNonNull(abbreviatedId);
    }

    /**
     * XACML attribute identifier
     *
     * @return attribute identifier
     */
    public final String getId() {
        return id;
    }

    /**
     * XACML attribute abbreviated identifier
     *
     * @return attribute abbreviated identifier
     */
    public final String getAbbreviatedId() {
        return abbreviatedId;
    }


    public boolean equals(Object id){
        if(id == this){
            return true;
        }
        if((id == null) ||
                !(getClass().isInstance(id))){
            return false;
        }
        ExtensibleIdentifier a = (ExtensibleIdentifier)id;
        return this.id.equals(a.getId()) &&
                Objects.equals(abbreviatedId, a.getAbbreviatedId());
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", abbreviatedId)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, abbreviatedId, getClass());
    }

    protected static <T extends ExtensibleIdentifier> Map<String, T> getById(Class<T> type){
        return geById(type, v->v.getId());
    }

    /**
     * Discovers all available instances of extensible identifier of the given type
     * @param type an extensible identifier class
     * @param <T> an extensible identifier type template param
     * @return map of identifier instances mapped by abbriavated identifier
     */
    protected static <T extends ExtensibleIdentifier> Map<String, T> getByAbbrId(Class<T> type){
        return geById(type, v->v.getAbbreviatedId());
    }

    protected static <T extends ExtensibleIdentifier> Map<String, T> geById(Class<T> type, Function<T, String> map){
        Map<String, List<T>> byId =
                Reflections.getDeclaredStaticFields(type, type)
                        .stream()
                        .collect(Collectors.groupingBy(map));
        if(LOG.isDebugEnabled()){
            LOG.debug("Found identifiers=\"{}\" for type=\"{}\"", byId, type);
        }
        return  byId.entrySet()
                .stream()
                .map(v->map(v))
                .collect(Collectors.toMap(
                        v->v.first(),
                        v->v.second(),
                        (a,b)->a, ()->new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                ));
    }

    private static <T extends ExtensibleIdentifier> Pair<String, T> map(
            Map.Entry<String, List<T>> e)
    {
        return Pair.of(e.getKey(),
                e.getValue().stream()
                        .findFirst().orElse(null));
    }


    /**
     * Gets defaultProvider of extensible identifier of given {@link <></>}
     * 
     * @param id a string representing this identifier
     * @param bydId an optional map representing by id cache
     * @param byAbbreviated an optional map representing cache by abbreviated id cache
     * @param valueSupplier a value factory if no cached instances found or if {@see alwaysUseSupplier} is {@code true}
     * @param alwaysUseSupplier a value factory
     * @param <T> a identifier type
     * @return an optional identifier defaultProvider
     */
    protected static <T extends ExtensibleIdentifier>  Optional<T> of(String id, Map<String, T> bydId, Map<String, T> byAbbreviated,
                                                                      Supplier<Optional<T>> valueSupplier, boolean alwaysUseSupplier) {
        return alwaysUseSupplier?valueSupplier.get():
                get(id, bydId)
                        .or(()->get(id, byAbbreviated)
                                .or(valueSupplier));
    }

    private static <T>  Optional<T> get(String key, Map<String, T> map){
        return ofNullable(map).flatMap(v->ofNullable(v.get(key)));
    }
}
