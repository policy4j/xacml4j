package org.xacml4j.util;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * Simple <code>javax.xml.namespace.NamespaceContext</code> implementation. Follows the standard
 * <code>NamespaceContext</code> contract, and is loadable via a <code>java.util.Map</code> or
 * <code>java.util.Properties</code> object
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SimpleNamespaceContext implements NamespaceContext {

    private Map<String, String> prefixToNamespaceUri = new HashMap<String, String>();

    private Multimap<String, String> namespaceUriToPrefixes = LinkedListMultimap.create();
    
    public SimpleNamespaceContext(){
    }
    
    public SimpleNamespaceContext(Map<String, String> bindings){
    	for(Entry<String, String> e : bindings.entrySet()){
    		bindNamespaceUri(e.getKey(), e.getValue());
    	}
    }

    public String getNamespaceURI(String prefix) {
        Preconditions.checkNotNull(prefix);
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }
        else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        else if (prefixToNamespaceUri.containsKey(prefix)) {
            return prefixToNamespaceUri.get(prefix);
        }
        return XMLConstants.NULL_NS_URI;
    }

    public String getPrefix(String namespaceUri) {
        Collection<String> prefixes = getPrefixesInternal(namespaceUri);
        return prefixes.isEmpty() ? null : prefixes.iterator().next();
    }

    public Iterator<String> getPrefixes(String namespaceUri) {
        return getPrefixesInternal(namespaceUri).iterator();
    }

    /**
     * Sets the bindings for this namespace context. The supplied map must consist of string key value pairs.
     *
     * @param bindings the bindings
     */
    public void setBindings(Map<String, String> bindings) {
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            bindNamespaceUri(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Binds the given namespace as default namespace.
     *
     * @param namespaceUri the namespace uri
     */
    public void bindDefaultNamespaceUri(String namespaceUri) {
        bindNamespaceUri(XMLConstants.DEFAULT_NS_PREFIX, namespaceUri);
    }

    /**
     * Binds the given prefix to the given namespace.
     *
     * @param prefix       the namespace prefix
     * @param namespaceUri the namespace uri
     */
    public void bindNamespaceUri(String prefix, String namespaceUri) {
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
            prefixToNamespaceUri.put(prefix, namespaceUri);
            getPrefixesInternal(namespaceUri).add(prefix);
        }
    }

    /** Removes all declared prefixes. */
    public void clear() {
        prefixToNamespaceUri.clear();
        namespaceUriToPrefixes.clear();
    }

    /**
     * Returns all declared prefixes.
     *
     * @return the declared prefixes
     */
    public Iterator<String> getBoundPrefixes() {
        Set<String> prefixes = new HashSet<String>(prefixToNamespaceUri.keySet());
        prefixes.remove(XMLConstants.DEFAULT_NS_PREFIX);
        return prefixes.iterator();
    }

    private Collection<String> getPrefixesInternal(String namespaceUri) {
        if (XMLConstants.XML_NS_URI.equals(namespaceUri)) {
            return Collections.singletonList(XMLConstants.XML_NS_PREFIX);
        }
        else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri)) {
            return Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE);
        }
        else {
            Collection<String> list = namespaceUriToPrefixes.get(namespaceUri);
            if (list == null) {
                list = new LinkedList<String>();
                namespaceUriToPrefixes.putAll(namespaceUri, list);
            }
            return list;
        }
    }

    /**
     * Removes the given prefix from this context.
     *
     * @param prefix the prefix to be removed
     */
    public void removeBinding(String prefix) {
        String namespaceUri = prefixToNamespaceUri.get(prefix);
        Collection<String> prefixes = getPrefixesInternal(namespaceUri);
        prefixes.remove(prefix);
    }

    public boolean hasBinding(String prefix) {
        return prefixToNamespaceUri.containsKey(prefix);
    }
}