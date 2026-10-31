package org.xacml4j.util;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Node;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * A {@link NamespaceContext} builder
 *
 * @author Giedrius Trumpickas
 */
public final class NamespaceContextBuilder
{
    private java.util.function.Function<Collection<String>, String> prefixSelectionStrategy;
    private Multimap<String, String> namespaceToPrefixes = LinkedListMultimap.create();
    private NamespaceContext parentContext;

    private NamespaceContextBuilder(){}

    public static NamespaceContextBuilder builder(){
        return new NamespaceContextBuilder();
    }

    public NamespaceContextBuilder parentContext(Node node){
        return parentContext(new NodeNamespaceContext(
                Objects.requireNonNull(node, "node")));
    }

    public NamespaceContextBuilder parentContext(NamespaceContext context){
        this.parentContext = Objects.requireNonNull(context, "context");
        return this;
    }

    public NamespaceContextBuilder prefixStrategy(java.util.function.Function<Collection<String>, String> prefixSelectionStrategy){
        this.prefixSelectionStrategy = Objects.requireNonNull(prefixSelectionStrategy, "prefixSelectionStrategy");
        return this;
    }

    /**
     * Binds the given prefix to the given namespace.
     *
     * @param prefix       the namespace prefix
     * @param namespaceUri the namespace uri
     */
    public NamespaceContextBuilder definePrefix(String prefix, String namespaceUri) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(namespaceUri, "namespaceUri");
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            Preconditions.checkArgument(XMLConstants.XML_NS_URI.equals(namespaceUri),
                    "Prefix \"%s\"\" bound to namespace \"%s\" (should be \"%s\")",
                    prefix, namespaceUri, XMLConstants.XML_NS_URI);
        } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            Preconditions.checkArgument(XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri),
                    "Prefix \"%s\"\" bound to namespace \"%s\" (should be \"%s\")",
                    prefix, namespaceUri, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        }
        namespaceToPrefixes.put(namespaceUri, prefix);
        return this;
    }

    public NamespaceContextBuilder defaultNamespace(String namespaceUri){
        return definePrefix(XMLConstants.DEFAULT_NS_PREFIX, namespaceUri);
    }

    public NamespaceContext build(){
        return new DefaultNamespaceContext(parentContext, namespaceToPrefixes.asMap(),
                prefixSelectionStrategy != null?prefixSelectionStrategy:
                        ((prefixes)-> prefixes.stream().findFirst().orElse(null)));
    }


    private static final class DefaultNamespaceContext implements NamespaceContext
    {

        private Map<String, Collection<String>> namespaceUriToPrefixes;
        private Map<String, String> prefixToNamespace;

        private Optional<NamespaceContext> parentContext;

        private java.util.function.Function<Collection<String>, String> prefixSelectionStrategy;

        private DefaultNamespaceContext(
                NamespaceContext parentNamespaceContext,
                Map<String, Collection<String>> namespaceUriToPrefixes,
                Function<Collection<String>, String> prefixSelectionStrategy){
            this.namespaceUriToPrefixes = ImmutableMap.copyOf(namespaceUriToPrefixes);
            this.prefixToNamespace = namespaceUriToPrefixes
                    .keySet().stream().collect(Collectors.toMap(v->prefixSelectionStrategy
                            .apply(namespaceUriToPrefixes.get(v)), v->v));
            this.parentContext = Optional.ofNullable(parentNamespaceContext);
            this.prefixSelectionStrategy = Objects.requireNonNull(
                    prefixSelectionStrategy, "prefixSelectionStrategy");
        }

        private Optional<String> getNamespaceUriInternal(String prefix){
            if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
                return Optional.of(XMLConstants.XML_NS_URI);
            }
            else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                return Optional.of(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
            }
            return Optional.ofNullable(Optional
                    .ofNullable(prefixToNamespace.get(prefix))
                    .orElseGet(()->parentContext.map(v->v.getNamespaceURI(prefix)).orElse(null)));
        }

        private Collection<String> getPrefixesInternal(String namespaceUri){
            if (XMLConstants.XML_NS_URI.equals(namespaceUri)) {
                return Lists.newArrayList(Iterables.concat(
                        Collections.singletonList(XMLConstants.XML_NS_PREFIX),
                        namespaceUriToPrefixes.get(namespaceUri)));
            }
            else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri)) {
                return Lists.newArrayList(Iterables.concat(
                        Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE),
                        namespaceUriToPrefixes.get(namespaceUri)));
            }
            return namespaceUriToPrefixes.get(namespaceUri);
        }

        @Override
        public String getNamespaceURI(String prefix) {
            Preconditions.checkNotNull(prefix, "prefix");
            return getNamespaceUriInternal(prefix)
                    .orElse(XMLConstants.NULL_NS_URI);
        }

        @Override
        public String getPrefix(String namespaceUri) {
            return prefixSelectionStrategy.apply(getPrefixesInternal(namespaceUri));
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceUri) {
            return getPrefixesInternal(namespaceUri)
                    .iterator();
        }
    }

    private static final class NodeNamespaceContext implements NamespaceContext
    {
        private Node node;

        NodeNamespaceContext(Node node){
            Preconditions.checkNotNull(node);
            this.node = node;
        }

        @Override
        public String getNamespaceURI(String prefix){
            String namespaceURI = node.lookupNamespaceURI(prefix);
            return namespaceURI == null?XMLConstants.NULL_NS_URI:namespaceURI;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return node.lookupPrefix(namespaceURI);
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            String prefix = getPrefix(namespaceURI);
            return (prefix == null)?
                    Collections.<String>emptyList().iterator():
                    Collections.singleton(prefix).iterator();
        }
    }
}
