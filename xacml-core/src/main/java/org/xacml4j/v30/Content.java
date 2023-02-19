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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.v30.content.JsonContent;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.types.Entity;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;

/**
 * XACML {@link Entity} content abstraction
 *
 * @author Giedrius Trumpickas
 */
public interface Content
{
    /**
     * Gets content type
     *
     * @return content type
     */
    Type getType();

    /**
     * Tries to resolve given {@Link AttributeSelectorKey} to a {@link BagOfValues}
     *
     * @param selectorKey an attribute selector
     * @return {@link Optional}
     */
    default Optional<BagOfValues> resolve(
            AttributeSelectorKey selectorKey){
        return resolve(selectorKey, null);
    }

    /**
     * Tries to resolve given {@Link AttributeSelectorKey} to a {@link BagOfValues}
     *
     * @param selectorKey an attribute selector for this content
     * @param attributeCallback an attribute callback
     * @return {@link Optional}
     */
    Optional<BagOfValues> resolve(AttributeSelectorKey selectorKey,
                                  Supplier<Entity> entitySupplier);


    /**
     * Evaluates a given XPath expression to a {@link NodeList}
     *
     * @param path an XPath expression
     * @return {@link NodeList} representing an evaluation result
     * @throws EvaluationException if an error occurs while evaluating
     * given xpath expression
     */
    <NodeType> List<NodeType> evaluateToNodeSet(String path);

    /**
     * Evaluates a given XPath expression
     * to a {@link Node}
     *
     * @param path an XPath expression
     * @return {@link Node} representing an evaluation result
     * @throws EvaluationException if an error occurs while evaluating
     * given xpath expression
     */
    <NodeType> Optional<NodeType> evaluateToNode(String path);

    /**
     * Gets content object model
     *
     * @return content object model
     */
    Object toNode();

    /**
     * Evaluates given path to a list of path expressions
     * for the matching content nodes
     *
     * @param path a path expression
     * @return list of content node paths
     */
    Optional<String> evaluateToNodePath(String path);

    /**
     * Evaluates given path to a list of path expressions
     * for the matching content nodes
     *
     * @param path a path expression
     * @return list of content node paths
     */
    List<String> evaluateToNodePathList(String path);

    /**
     * Evaluates a given XPath expression to a {@link String}
     *
     * @param path an XPath expression
     * @return {@link String} representing an evaluation result
     * @throws EvaluationException if an error occurs while evaluating
     * given xpath expression
     */
    Optional<String> evaluateToString(String path);

    /**
     * Serializes this content to string representation
     *
     * @return content as string
     */
    String asString();
    
    /**
     * Evaluates a given XPath expression to a {@link Number}
     *
     * @param path an XPath expression
     * @return {@link Number} representing an evaluation result
     * @throws EvaluationException if an error occurs while evaluating
     * given xpath expression
     */
    <T extends Number> Optional<T> evaluateToNumber(String path);

    /**
     * Heuristically tries to guess content {@link Type}
     * from the content structure
     *
     * @param content a content
     * @return an optional {@link Type}
     */
    static Optional<Type> resolveTypeFromContent(String content){
        if(content == null){
            return Optional.empty();
        }
        int xmlStartIndex = content.indexOf("<");
        if(xmlStartIndex >= 0){
            if(CharMatcher.whitespace()
                    .matchesAllOf(content.substring(0, xmlStartIndex))){
                return Optional.of(Type.XML_UTF8);
            }
        }
        int jsonStartIndex = content.indexOf("{");
        jsonStartIndex = (jsonStartIndex >= 0)?jsonStartIndex:content.indexOf("[");
        if(jsonStartIndex >=0){
            if(CharMatcher.whitespace()
                    .matchesAllOf(content.substring(0, jsonStartIndex))){
                return Optional.of(Type.JSON_UTF8);
            }
        }
        return Optional.empty();
    }

    static Optional<Content> fromString(String content, Type type){
        switch (type){
            case XML_UTF8: return Optional.of(XmlContent.of(content));
            case JSON_UTF8: return Optional.of(JsonContent.of(content));
        }
        return Optional.empty();
    }

    static Optional<Content> fromString(String content){
        return resolveTypeFromContent(content)
                .flatMap(t->fromString(content, t));
    }

    static Optional<Content> fromStream(InputStream in){

        try{
            DataInputStream input = new DataInputStream(in);
            String content = input.readUTF();
            return resolveTypeFromContent(content)
                    .flatMap(t->fromString(content, t));
        }catch (IOException e){
            throw new IllegalArgumentException(
                    e.getMessage(), e);
        }

    }

    static Optional<Content> fromStream(InputStream content, Type type){
        switch (type){
            case XML_UTF8: return Optional.of(XmlContent.of(content));
            case JSON_UTF8: return Optional.of(JsonContent.of(content));
        }
        return Optional.empty();
    }

    static Optional<Content> fromNode(Object content, Type type){
        switch (type){
            case XML_UTF8: return Optional.of(XmlContent.of((Node) content));
            case JSON_UTF8: return Optional.of(JsonContent.of(content));
        }
        return Optional.empty();
    }

    /**
     * Supported content types
     */
    enum Type
    {
         XML_UTF8(MediaType.XML_UTF_8, MediaType.APPLICATION_XML_UTF_8),
        JSON_UTF8(MediaType.JSON_UTF_8);

        private Set<MediaType> supportedMediaTypes;

        Type(MediaType... type){
            this.supportedMediaTypes = ImmutableSet.copyOf(type);
        }

        public boolean is(final  String type){
            MediaType givenType  = MediaType.parse(type);
            return supportedMediaTypes.stream()
                    .filter(v->v.is(givenType))
                    .findAny().isPresent();
        }

        public static Optional<Type> getByMediaType(String mediaType){

            return Arrays.asList(values()).stream().filter(v->v.is(mediaType)).findFirst();
        }
    }


    /**
     * Supported path expression type for the given content
     *
     * @author Giedrius Trumpickas
     */
    enum PathType
    {
        /**
         * Denotes XPATH expression
         */
        XPATH(Type.XML_UTF8),

        /**
         * Denotes JSON PATH expression
         */
        JPATH(Type.JSON_UTF8);

        private Type type;

        PathType(Type type){
            this.type = type;
        }

        public Type getContentType(){
            return type;
        }
    }
}
