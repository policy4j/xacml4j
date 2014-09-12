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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

/**
 *
 * @author Giedrius Trumpickas
 */
public class NamespaceContextBuilderTest {

    @Test
    public void testTheSameNamespaceDifferentPrefixs(){
       NamespaceContext ctx = NamespaceContextBuilder
                .builder()
                .bind("aaa", "ns:uri0")
                .bind("bbb", "ns:uri0")
                .build();
        assertEquals("aaa", ctx.getPrefix("ns:uri0"));
        Iterator<String> prefixes = ctx.getPrefixes("ns:uri0");
        assertEquals("aaa", prefixes.next());
        assertEquals("bbb", prefixes.next());
    }

    @Test
    public void testBuilderDefaultNs(){
        NamespaceContext ctx = NamespaceContextBuilder
                .builder()
                .bind(XMLConstants.DEFAULT_NS_PREFIX, "ns:uri0")
                .build();
        assertEquals(XMLConstants.DEFAULT_NS_PREFIX, ctx.getPrefix("ns:uri0"));

        ctx = NamespaceContextBuilder
                .builder()
                .defaultNamespace("ns:uri0")
                .build();
        assertEquals(XMLConstants.DEFAULT_NS_PREFIX, ctx.getPrefix("ns:uri0"));
    }
}
