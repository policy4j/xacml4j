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


import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.internal.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.internal.spi.mapper.JacksonMappingProvider;
import com.sun.tools.doclets.internal.toolkit.util.DocFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.util.ReaderInputStream;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XacmlTypes;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

public class JsonContentParser implements ContentProvider
{
    private final static Logger log = LoggerFactory.getLogger(JsonContentParser.class);

    private final static ImmutableSet<MediaType> MEDIA_TYPES = ImmutableSet.of(MediaType.JSON_UTF_8);

    private final static TypeToken<String> STRING = new TypeToken<String>() {};
    private final static TypeToken<Float> FLOAT = new TypeToken<Float>() {};
    private final static TypeToken<Double> DOUBLE = new TypeToken<Double>() {};
    private final static TypeToken<Byte> BYTE = new TypeToken<Byte>() {};
    private final static TypeToken<Short> SHORT = new TypeToken<Short>() {};
    private final static TypeToken<Integer> INTEGER = new TypeToken<Integer>() {};
    private final static TypeToken<Long> LONG = new TypeToken<Long>() {};
    private final static TypeToken<JsonNode> JSON_NODE = new TypeToken<JsonNode>() {};

    private final static TypeToken<List<?>> LIST_OF_ANY = new TypeToken<List<?>>() {};

    private com.jayway.jsonpath.Configuration config =
            Configuration
                    .builder()
                    .jsonProvider(configureJsonProvider())
                    .mappingProvider(configureMappingProvider())
                    .build();

    private ParseContext parseContext;
    private ObjectMapper mapper;

    @Override
    public Set<MediaType> getSupportedMediaTypes() {
        return MEDIA_TYPES;
    }

    public JsonContentParser(){
        this.parseContext = JsonPath.using(config);
    }

    @Override
    public Content from(Reader source) throws Exception {
        InputStream input = new ReaderInputStream(source, Charsets.UTF_8);
        return new JsonContent(parseContext.parse(input));
    }

    private static JacksonJsonProvider configureJsonProvider(){
        ObjectMapper mapper = new ObjectMapper();
        return new JacksonJsonProvider(mapper);
    }

    private static JacksonMappingProvider configureMappingProvider(){
        ObjectMapper mapper = new ObjectMapper();
        return new JacksonMappingProvider(mapper);
    }

    /**
     * TODO: review implementation
     */
    public class JsonContent implements Content
    {
        private ReadContext context;

        private JsonContent(ReadContext context){
           this.context = context;
        }

        @Override
        public MediaType getMediaType() {
            return MediaType.JSON_UTF_8;
        }

        @Override
        public BagOfAttributeExp select(XPathExp xpath, AttributeExpType type) throws Exception
        {
            Object result = context.read(xpath.getPath());
            if(result == null){
                return type.emptyBag();
            }
            TypeToken<? extends Object> resultType = TypeToken.of(result.getClass());
            if(resultType.isPrimitive()){
                return type.of(result).toBag();
            }
            if(LIST_OF_ANY.isAssignableFrom(resultType)){
                List<Object> resultAsList = (List<Object>)result;
                Object firstElement = Iterables.getFirst(resultAsList, null);
                if(firstElement == null){
                    return type.emptyBag();
                }
                return type.bag().values(resultAsList).build();
            }
            return type.emptyBag();
        }


        @Override
        public Iterable<Content> selectNodes(XPathExp xpath) throws Exception {
            Object result = context.read(xpath.getPath());
            if(result == null){
                return ImmutableList.of();
            }
            TypeToken<? extends Object> resultType = TypeToken.of(result.getClass());
            if(!resultType.isPrimitive()){
                if(LIST_OF_ANY.isAssignableFrom(resultType)){
                    ImmutableList.Builder<Content> nodes = ImmutableList.builder();
                    Object firstElement = Iterables.getFirst((List<Object>)result, null);
                    if(firstElement == null){
                        return ImmutableList.of();
                    }
                    if(JSON_NODE.isAssignableFrom(TypeToken.of(firstElement.getClass()))){
                        List<JsonNode> jJsonNodes = (List<JsonNode>)result;
                        for(JsonNode n : jJsonNodes){
                            nodes.add(new JsonContent(parseContext.parse(n)));
                        }
                        return nodes.build();
                    }
                }
            }
            return ImmutableList.of();
        }

        @Override
        public Content selectNode(XPathExp xpath) throws Exception {
            Object result = context.read(xpath.getPath());
            if (result == null ||
                    !JSON_NODE.isAssignableFrom(
                            TypeToken.of(result.getClass()))) {
                return null;
            }
            return new JsonContent(parseContext.parse(result));

        }


        @Override
        public void to(Writer writer) throws Exception {
            writer.write(config
                    .jsonProvider()
                    .toJson(context.json()));
        }

    }
}
