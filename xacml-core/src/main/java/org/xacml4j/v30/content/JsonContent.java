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
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.Path;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.PathEvaluationException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JsonProvider;

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
    private DocumentContext valueListContext;
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
        this.valueListContext = JsonPath
                .using(valueConfiguration(config))
                .parse(jsonDocument);
    }

    /**
     * The given configuration reading values as lists, without {@link Option#AS_PATH_LIST},
     * which would read the paths of the values instead
     */
    private static Configuration valueConfiguration(Configuration config){
        EnumSet<Option> options = EnumSet.of(Option.ALWAYS_RETURN_LIST);
        options.addAll(config.getOptions());
        options.remove(Option.AS_PATH_LIST);
        return config.setOptions(options.toArray(new Option[0]));
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

    /**
     * Resolves the JSONPath of a given selector as {@link XmlContent} resolves an XPath:
     * a path without a value resolves to nothing, whatever the data type, and each selected
     * scalar value converts from its string form to the selector data type. An object, an
     * array, a value that does not convert, and an invalid path are processing errors.
     * A JSON {@code null} is no value.
     * With a context selector, the path runs on the one value that the context selector
     * selects, which is its root {@code $}.
     */
    @Override
    public Optional<BagOfValues> resolve(
		    AttributeSelectorKey selectorKey,
		    Supplier<Entity> entitySupplier) {
        String path = selectorKey.getPath();
        try{
            DocumentContext document = (selectorKey.getContextSelectorId() == null)?valueListContext:
                    JsonPath.using(valueListContext.configuration())
                            .parse(contextValue(selectorKey, entitySupplier.get()));
            ValueType type = selectorKey.getDataType();
            Optional<TypeToString> toString = TypeToString.forType(type);
            JsonProvider json = valueListContext.configuration().jsonProvider();
            BagOfValues.Builder bag = type.bagBuilder();
            for(Object node : json.toIterable(read(document, path))){
                Object value = json.unwrap(node);
                if(value == null){
                    continue;
                }
                if(json.isArray(value) || json.isMap(value)){
                    throw new IllegalArgumentException(
                            String.format("Unsupported JSON value=\"%s\" path=\"%s\"", value, path));
                }
                bag.attribute(toString
                        .orElseThrow(()->new IllegalArgumentException(
                                String.format("Unsupported XACML type=\"%s\"", type.getTypeId())))
                        .fromString(String.valueOf(value)));
            }
            BagOfValues values = bag.build();
            return values.isEmpty()?Optional.empty():Optional.of(values);
        }catch (PathEvaluationException e){
            throw e;
        }catch (Exception e){
            throw PathEvaluationException.invalidJsonPath(path, e);
        }
    }

    /**
     * Gets the value that the context selector of a given selector selects, as {@link XmlContent}
     * gets the context node of an XPath (XACML 3.0 section 7.3.7). The context selector is the
     * first {@link PathType#JPATH} path value of the entity attribute with the
     * {@code ContextSelectorId}, and its category must be the selector category. Path values of
     * both path types have the XPath expression data type. A JSONPath that selects no value or
     * more than one value is a syntax error.
     */
    private Object contextValue(AttributeSelectorKey selectorKey, Entity entity){
        String id = selectorKey.getContextSelectorId();
        Path contextPath = Optional.ofNullable(entity)
                .flatMap(e->e.<Path>findValue(id, XacmlTypes.XPATH, null,
                        v->((Path)v).getPathType() == PathType.JPATH))
                .orElseThrow(()->PathEvaluationException.invalidJsonPathContextSelectorId(selectorKey.getPath(), id));
        CategoryId category = contextPath.getCategory().orElse(null);
        if(!Objects.equals(category, selectorKey.getCategory())){
            throw AttributeReferenceEvaluationException.forSelector(selectorKey,
                    ()->String.format("and ContextSelectorId.Category=\"%s\"", category));
        }
        JsonProvider json = valueListContext.configuration().jsonProvider();
        Object values = read(valueListContext, contextPath.getPath());
        if(json.length(values) != 1){
            throw PathEvaluationException.invalidJsonPath(
                    selectorKey.getPath(), Status.syntaxError().build(), null);
        }
        return json.getArrayIndex(values, 0);
    }

    /**
     * Reads a given path as a JSON array: a path without a value reads as an empty array
     */
    private static Object read(DocumentContext document, String path){
        try{
            return document.read(path);
        }catch (PathNotFoundException e){
            return document.configuration().jsonProvider().createArray();
        }
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
