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

import com.google.common.net.MediaType;
import org.xml.sax.InputSource;

import java.io.Reader;
import java.io.Writer;
import java.util.Set;

/**
 * Represents a content provider
 *
 * @author Giedrius Trumpickas
 */
public interface ContentProvider
{
    /**
     * Gets supported content media types
     *
     * @return a set of supported media types
     */
    Set<MediaType> getSupportedMediaTypes();

    /**
     * Parses given {@link java.io.Reader}
     *
     * @param source
     * @return {@link Content} instance
     * @throws Exception
     */
    Content from(Reader source) throws Exception;
}
