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


import com.google.common.base.Preconditions;
import com.google.common.io.CharSource;
import com.google.common.net.MediaType;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;


public interface ContentFactory
{
    /**
     * Creates {@link org.xacml4j.v30.content.Content} from
     * a given input source
     *
     * @param type a content type
     * @param source a content source
     * @return {@link org.xacml4j.v30.content.Content} instance
     * @throws Exception if an error occurs
     */
    Content from(String type, Reader source) throws Exception, IOException;
    Content from(MediaType type, Reader source) throws Exception, IOException;
    Content from(String type, CharSource source) throws Exception, IOException;
    Content from(MediaType type, CharSource source) throws Exception, IOException;
    Content from(String type, InputStream stream) throws Exception, IOException;
    Content from(MediaType type, InputStream stream) throws Exception, IOException;

}
