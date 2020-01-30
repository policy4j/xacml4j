package org.xacml4j.v30.types;

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

import org.xacml4j.v30.*;

import java.util.Optional;
import java.util.stream.Stream;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#ENTITY} type.
*/
public class EntityValue extends BaseAttributeValue<Entity>
{
    private static final long serialVersionUID = 6188174758603655643L;

    private EntityValue(Entity entity){
        super(XacmlTypes.ENTITY, entity);
    }

    public Stream<Attribute> stream(){
        return value().stream();
    }

    public Optional<BagOfAttributeValues> resolve(AttributeDesignatorKey designatorKey){
        return value().resolve(designatorKey);
    }

    public Optional<BagOfAttributeValues> resolve(AttributeSelectorKey selectorKey){
        return value().resolve(selectorKey);
    }

    public static EntityValue of(Entity entity){
        return new EntityValue(Entity.builder().copyOf(entity).build());
    }

    static EntityValue of(Attribute a, Attribute ...attributes){
        return new EntityValue(Entity.builder().attribute(a).attribute(attributes).build());
    }


    public static EntityValue of(Content content, Attribute ...attrs){
        return new EntityValue(Entity
                .builder()
                .content(content)
                .attribute(attrs)
                .build());
    }
}
