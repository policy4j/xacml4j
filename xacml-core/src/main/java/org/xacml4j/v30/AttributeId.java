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


import java.util.*;

/**
 * XACML attribute identifier
 *
 * @author Giedrius Trumpickas
 */
public class AttributeId extends ExtensibleIdentifier
{
    public final static AttributeId RESOURCE_ID = new AttributeId("urn:oasis:names:tc:xacml:1.0:resource:resource-id", "resource-id");
    public final static AttributeId MDP_MULTIPLE_SELECTOR = new AttributeId("urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector", "multiple:content-selector");
    public final static AttributeId MDP_SCOPE = new AttributeId("urn:oasis:names:tc:xacml:3.0:profile:multiple:scope", "scope");
    public final static AttributeId CONTENT_SELECTOR = new AttributeId("urn:oasis:names:tc:xacml:3.0:content-selector", "content-selector");

    private final static Map<String, AttributeId> By_ID = ExtensibleIdentifier.getById(AttributeId.class);
    private final static Map<String, AttributeId> By_ABBREVIATED_ID = ExtensibleIdentifier.getByAbbrId(AttributeId.class);

    private final static boolean ALWAYS_USE_SUPPLIER = Optional.ofNullable(System
            .getProperty(AttributeId.class.getName() + ".ALWAYS_USE_SUPPLIER"))
            .map(v->Boolean.parseBoolean(v)).orElse(false);

    private AttributeId(String id, String abbreviatedId) {
        super(id, abbreviatedId);
    }
    private AttributeId(String id) {
        super(id, id);
    }

    public final static Optional<AttributeId> of(String id) {
        return of(id, By_ID, By_ABBREVIATED_ID,
                ()->Optional.of(new AttributeId(id)),
                ALWAYS_USE_SUPPLIER);
    }
}

