package org.xacml4j.v30.content;

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

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.PathEvaluationException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.EvaluationListener;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;

/**
 * JSON content abstraction
 *
 * @author Giedrius Trumpickas
 */
public final class JsonContent
        implements Content
{
    private final static Logger LOG = LoggerFactory.getLogger(JsonContent.class);

    final static TypeRef<String> STRING = new TypeRef<>() {};


    private DocumentContext context;
    private DocumentContext pathListContext;
    private Object jsonDocument;

    private JsonContent(Supplier<Object> jsonSupplier, Configuration config){
        Objects.requireNonNull(jsonSupplier, "jsonSupplier");
        Objects.requireNonNull(config, "config");
        this.jsonDocument = jsonSupplier.get();
        this.context = JsonPath
                .using(config
                        .addOptions(
                                Option.REQUIRE_PROPERTIES))
                .parse(jsonDocument);
        this.pathListContext = JsonPath
                .using(config.addOptions(
                        Option.AS_PATH_LIST,
                        Option.SUPPRESS_EXCEPTIONS,
                        Option.DEFAULT_PATH_LEAF_TO_NULL))
                .parse(jsonDocument);
    }


    public static JsonContent of(String jsonDocument){
        return  of(jsonDocument, Configuration.defaultConfiguration());
    }

    public static JsonContent of(String jsonDocument, Configuration config){
        return new JsonContent(()->config.jsonProvider().parse(jsonDocument), config);
    }

    public static JsonContent of(InputStream inputStream, Configuration config){
        return new JsonContent(
                ()->config.jsonProvider().parse(inputStream, "UTF-8"), config);
    }

    public static JsonContent of(InputStream jsonDocument){
        return of(jsonDocument, Configuration
                .defaultConfiguration());
    }

    public static JsonContent of(Object jsonDocument){
        return new JsonContent(()->jsonDocument, Configuration
                .defaultConfiguration());
    }


    @Override
    public String asString() {
        return context.jsonString();
    }

    @Override
    public Type getType() {
        return Type.JSON_UTF8;
    }

    @Override
    public Optional<BagOfValues> resolve(
		    AttributeSelectorKey selectorKey,
		    Supplier<Entity> entitySupplier) {
        return Optional.empty();
    }

    @Override
    public <T> List<T> evaluateToNodeSet(String path) {
        try{
            List<T> nodeSet = context.read(path);
            if(LOG.isDebugEnabled()){
                LOG.debug("JsonPath=\"{}\", Results=\"{}\"", path, nodeSet);
            }
            return nodeSet;
        }catch (InvalidPathException e){
            throw PathEvaluationException.invalidJsonPath(path, e);
        }

    }

    @Override
    public <T> Optional<T> evaluateToNode(String path) {
        return Optional.ofNullable(context.read(path));
    }

    @Override
    public Optional<String> evaluateToNodePath(String path) {
        try{
            List<String> paths = pathListContext.read(path);
            if(LOG.isDebugEnabled()){
                LOG.debug("JsonPath=\"{}\", Results=\"{}\"", path, paths);
            }
            if(paths == null || paths.isEmpty()){
                return Optional.empty();
            }
            return paths
                    .stream()
                    .findFirst();
        }catch (Exception e){
            throw PathEvaluationException.invalidJsonPath(path, e);
        }

    }

    @Override
    public Object toNode() {
        return context.json();
    }

    @Override
    public List<String> evaluateToNodePathList(String path) {
        try{
            List<String> paths = pathListContext.read(path);
            if(LOG.isDebugEnabled()){
                LOG.debug("JsonPath=\"{}\", Results=\"{}\"", path, paths);
            }
            return paths;
        }catch (Exception e){
            throw PathEvaluationException.invalidJsonPath(path, e);
        }

    }

    @Override
    public Optional<String> evaluateToString(String path) {

        return Optional.ofNullable(context.read(path, STRING));
    }

    @Override
    public <T extends Number> Optional<T> evaluateToNumber(
            String path) {
        try{
            List < ? extends Number> result = context.read(path);
            if(LOG.isDebugEnabled()){
                LOG.debug("JsonPath=\"{}\", Results=\"{}\"", path, result);
            }
            if(result == null){
                return Optional.empty();
            }
            return (Optional<T>) result.stream().findFirst();
        }
        catch (Exception e){
            throw PathEvaluationException.invalidJsonPath(path, e);
        }
    }
}
