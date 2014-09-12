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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

/**
 * @author Giedrius Trumpickas
 */
public class NamespaceContextBuilder
{
    private ImmutableMap.Builder<String, String> prefixToNamespaceUriBuilder = ImmutableMap.builder();
    private ImmutableMultimap.Builder<String, String> namespaceUriToPrefixBuilder = ImmutableListMultimap.builder();
    private NamespaceContext parentContextRef;

    public static NamespaceContextBuilder builder(){
        return new NamespaceContextBuilder();
    }

    public NamespaceContextBuilder delegate(NamespaceContext context){
        this.parentContextRef = context;
        return this;
    }

    public NamespaceContextBuilder defaultNamespace(String namespaceUri){
        return bind(XMLConstants.DEFAULT_NS_PREFIX, namespaceUri);
    }

    public NamespaceContextBuilder bind(String prefix, String namespaceUri) {
        Preconditions.checkNotNull(prefix);
        Preconditions.checkNotNull(namespaceUri);
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            Preconditions.checkArgument(XMLConstants.XML_NS_URI.equals(namespaceUri),
                    "Prefix \"%s\"\" bound to namespace \"%s\" (should be \"%s\")",
                    prefix, namespaceUri, XMLConstants.XML_NS_URI);
        } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            Preconditions.checkArgument(XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri),
                    "Prefix \"%s\"\" bound to namespace \"%s\" (should be \"%s\")",
                    prefix, namespaceUri, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        }
        else {
            prefixToNamespaceUriBuilder.put(prefix, namespaceUri);
            namespaceUriToPrefixBuilder.put(namespaceUri, prefix);
        }
        return this;
    }

    public NamespaceContext build(){
        final ImmutableMultimap<String, String> namespaceUriToPrefix = namespaceUriToPrefixBuilder.build();
        final ImmutableMap<String, String> prefixToNamespaceUri = prefixToNamespaceUriBuilder.build();
        final NamespaceContext parentContext = parentContextRef;
        return new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                Preconditions.checkNotNull(prefix);
                if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
                    return XMLConstants.XML_NS_URI;
                }
                else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                    return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                }
                else if (prefixToNamespaceUri.containsKey(prefix)) {
                    String nsUri = prefixToNamespaceUri.get(prefix);
                    if(nsUri == null &&
                            parentContext != null){
                        return parentContext.getNamespaceURI(prefix);
                    }
                }
                return XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String namespaceUri) {
                 String prefix = Iterables.getFirst(
                        namespaceUriToPrefix.get(namespaceUri),
                        null);
                if(prefix == null &&
                        parentContext != null){
                    prefix = parentContext.getPrefix(namespaceUri);
                }
                return prefix;
            }

            @Override
            public Iterator getPrefixes(String namespaceUri) {
                Iterable<String> otherPrefixes = (parentContext != null)?
                        (Iterable<String>)parentContext.getPrefixes(namespaceUri):ImmutableList.<String>of();
                Iterable<String> prefixes = namespaceUriToPrefix.get(namespaceUri);
                return FluentIterable
                        .from(Iterables.concat(prefixes, otherPrefixes))
                        .filter(new Predicate<String>() {
                            @Override
                            public boolean apply(String input) {
                                return !XMLConstants.DEFAULT_NS_PREFIX
                                        .equals(input);
                            }
                        })
                        .iterator();
            }
        };
    }
}
