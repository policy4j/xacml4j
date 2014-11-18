package org.xacml4j.v30.content;

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


import com.google.common.base.Optional;
import com.google.common.net.MediaType;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.types.XPathExp;

import java.io.Writer;

/**
 * An XACML PDP engine content abstraction,
 * this abstraction should support  XML, JSON
 * and Serialized Java Objects content implementations
 *
 * @author Giedrius Trumpickas
 */
public interface Content
{
    /**
     * Content media type
     *
     * @return {@link com.google.common.net.MediaType}
     */
    MediaType getMediaType();

    /**
     * Executes given {@link org.xacml4j.v30.types.XPathExp}
     *
     * @param xpath an XPATH expression
     * @param type a data type for a bag
     * @return {@link org.xacml4j.v30.BagOfAttributeExp}
     * @throws Exception if an error occurs
     */
    BagOfAttributeExp select(XPathExp xpath,  AttributeExpType type)
            throws Exception;


    /**
     * Selects multiple content nodes
     *
     * @param xpath an XPATH expression
     * @return an iterable over
     * selected nodes
     * @throws Exception if an error occurrs
     */
    Iterable<Content> selectNodes(XPathExp xpath)
            throws Exception;


    /**
     * Selects single node
     *
     * @param xpath an XPATH expression
     * @return a single content node
     * @throws Exception
     */
    Content selectNode(XPathExp xpath)
            throws Exception;


    /**
     * Serializes this content to the given
     * {@link java.io.Writer}
     *
     * @param writer a writer
     * @throws Exception an exception
     */
    void to(Writer writer) throws Exception;
}
