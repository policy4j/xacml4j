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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharSource;
import com.google.common.net.MediaType;
import org.xacml4j.util.ReaderInputStream;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultContentFactory implements ContentFactory
{
    private ConcurrentHashMap<MediaType, ContentProvider> providers =
            new ConcurrentHashMap<MediaType, ContentProvider>();

    public DefaultContentFactory(){
        addProvider(new XmlContentParser());
        addProvider(new JsonContentParser());
    }

    /**
     * Adds new {@link org.xacml4j.v30.content.ContentProvider}
     * to this content factory
     *
     * @param provider a content provider
     */
    public void addProvider(ContentProvider provider){
        Set<MediaType> supportedTypes = provider.getSupportedMediaTypes();
        for(MediaType type : supportedTypes){
            providers.putIfAbsent(type, provider);
        }
    }

    public Content from(String type, CharSource source) throws Exception{
        return from(MediaType.parse(type), source.openBufferedStream());
    }

    public Content from(MediaType type, CharSource source) throws Exception{
        return from(type, source.openBufferedStream());
    }

    public Content from(String type, Reader source) throws Exception{
        return from(MediaType.parse(type), source);
    }

    public Content from(String type, InputStream source) throws Exception{
        return from(MediaType.parse(type), new InputStreamReader(source));
    }

    public Content from(MediaType type, InputStream source) throws Exception{
        return from(type, new InputStreamReader(source));
    }

    public Content from(MediaType type, Reader source) throws Exception{
        ContentProvider provider = providers.get(type);
        if(provider == null){
            throw new IllegalArgumentException(
                    String.format(
                            "Unsupported media type=\"%s\"", type));
        }
        return provider.from(source);
    }



}
