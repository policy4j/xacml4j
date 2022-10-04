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
import org.xacml4j.v30.Content;

import java.util.Optional;

/**
 * Implementation an XACML {@link Expression}
 *
 * for {@link XacmlTypes#XPATH} type.
 */
public final class PathValue extends BaseValue<Path>
{
    private static final long serialVersionUID = 8576542145890616101L;

    private PathValue(ValueType type, Path path){
        super(type, path);
    }

    public static PathValue of(Path p){
        Optional<PathValue> a = Optional.empty();
        switch (p.getType()) {
            case XPATH:
                a = Optional.of(
                        new PathValue(XacmlTypes.XPATH, p));
                break;
            case JPATH:
                a = Optional.of(
                        new PathValue(XacmlTypes.JPATH, p));
                break;
        }
        return a.orElseThrow(()-> SyntaxException
                .invalidAttributeValue(p));
    }

    public String getPath(){
        return value().getPath();
    }

    public Content.PathType getPathType(){
        return value().getType();
    }

    public Content.Type getContentType(){
        return value().getType().getContentType();
    }

    public Optional<CategoryId> getCategory(){
        return value().getCategory();
    }

}
