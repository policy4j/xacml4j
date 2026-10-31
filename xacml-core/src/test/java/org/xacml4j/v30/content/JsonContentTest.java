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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.Path;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.truth.Truth8;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;


public class JsonContentTest
{
    private final static String JSON_DATA ="{\n" +
            "  \"store\": {\n" +
            "    \"book\": [\n" +
            "      {\n" +
            "        \"category\": \"reference\",\n" +
            "        \"author\": \"Nigel Rees\",\n" +
            "        \"title\": \"Sayings of the Century\",\n" +
            "        \"price\": 8.95\n" +
            "      },\n" +
            "      {\n" +
            "        \"category\": \"fiction\",\n" +
            "        \"author\": \"Evelyn Waugh\",\n" +
            "        \"title\": \"Sword of Honour\",\n" +
            "        \"price\": 12.99\n" +
            "      },\n" +
            "      {\n" +
            "        \"category\": \"fiction\",\n" +
            "        \"author\": \"Herman Melville\",\n" +
            "        \"title\": \"Moby Dick\",\n" +
            "        \"isbn\": \"0-553-21311-3\",\n" +
            "        \"price\": 8.99\n" +
            "      },\n" +
            "      {\n" +
            "        \"category\": \"fiction\",\n" +
            "        \"author\": \"J. R. R. Tolkien\",\n" +
            "        \"title\": \"The Lord of the Rings\",\n" +
            "        \"isbn\": \"0-395-19395-8\",\n" +
            "        \"price\": 22.99\n" +
            "      }\n" +
            "    ],\n" +
            "    \"bicycle\": {\n" +
            "      \"color\": \"red\",\n" +
            "      \"price\": 19.95\n" +
            "    }\n" +
            "  },\n" +
            "  \"expensive\": 10\n" +
            "}";

    private Content content;


    @BeforeEach
    public void setUp()
    {
        this.content = Content.fromString(JSON_DATA).get();
    }

    @Test
    public void testJsonEvaluateToNodeSetAmdNodePathList()
    {
        List<Object> authors0 = content.evaluateToNodeSet("$.store.book[*].author");
        assertThat(authors0).hasSize(4);
        assertThat(authors0).containsExactly(
                "Nigel Rees","Evelyn Waugh","Herman Melville","J. R. R. Tolkien");
        List<String> authors1 = content.evaluateToNodePathList("$.store.book[*].author");
        assertThat(authors1).hasSize(4);
        assertThat(authors1).containsExactly(
                "$['store']['book'][0]['author']",
                "$['store']['book'][1]['author']",
                "$['store']['book'][2]['author']",
                "$['store']['book'][3]['author']");
    }

    @Test
    public void testJsonEvaluateToNodeSetAmdNodePathListWithInvalidPath()
	{
		assertThrows(PathEvaluationException.class, () -> {
			List<Object> authors0 = content.evaluateToNodeSet("$.store--.book[***].author");
		});
	}

    @Test
    public void testJsonEvaluateToNodeSetAmdNodePathListInvalidPath()
	{
		assertThrows(PathEvaluationException.class, () ->
			content.evaluateToNodeSet("$.store.??aa-203(-book[*].author"));
	}

    @Test
    public void testJsonPathEvalToNodePathAndNode(){
        Optional<String> path = content.evaluateToNodePath("$.store.book[0]");
        Truth8.assertThat(path).hasValue("$['store']['book'][0]");
        Optional<Object> node = content.evaluateToNode("$.store.book[0]");
        Truth8.assertThat(node)
              .hasValue(
                      Map.of("category", "reference", "author", "Nigel Rees",
                                      "title", "Sayings of the Century", "price", 8.95));
    }

    @Test
    public void testEvaludateToNumber() {
        Optional<Number> price = content.evaluateToNumber("$.store.book[*].price");
        Truth8.assertThat(price).hasValue(8.95);
    }

    @Test
    public void testInvalidInvalidPath() {
		assertThrows(PathEvaluationException.class, () -> {
			Optional<Number> price = content.evaluateToNumber("$.store.---?book[*].price");
			Truth8.assertThat(price).hasValue(8.95);
		});
	}

    @Test
    public void testGetNode() {
        Optional<Content> content = Content.fromString(JSON_DATA);
        Truth8.assertThat(content).isPresent();
        LoggerFactory.getLogger(JsonContentTest.class)
                     .debug(
                content.map(v->v.toNode()).orElse(null).getClass().toString());
    }

    @Test
    public void selectorResolvesScalarValuesToABag() {
        Truth8.assertThat(content.resolve(selector("$.store.book[*].author", XacmlTypes.STRING)))
              .hasValue(XacmlTypes.STRING.bagBuilder()
                                         .value("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien")
                                         .build());
        Truth8.assertThat(content.resolve(selector("$.expensive", XacmlTypes.INTEGER)))
              .hasValue(XacmlTypes.INTEGER.bagBuilder().value(10).build());
    }

    @Test
    public void selectorPathWithoutAValueResolvesToNothing() {
        Truth8.assertThat(content.resolve(selector("$.store.car.color", XacmlTypes.STRING))).isEmpty();
        Truth8.assertThat(content.resolve(selector("$.store.book[*].publisher", XacmlTypes.STRING))).isEmpty();
    }

    @Test
    public void selectorValueThatDoesNotConvertIsAProcessingError() {
        assertStatus(StatusCodeId.STATUS_PROCESSING_ERROR,
                () -> content.resolve(selector("$.store.book[*].author", XacmlTypes.INTEGER)));
        assertStatus(StatusCodeId.STATUS_PROCESSING_ERROR,
                () -> content.resolve(selector("$.store.bicycle", XacmlTypes.STRING)));
    }

    @Test
    public void contextSelectorPathRunsOnTheValueItSelects() {
        Truth8.assertThat(entity(XacmlTypes.JPATH.ofAny("$.store.book[2]", CategoryId.RESOURCE))
                                  .resolve(contextSelector("$.title")))
              .hasValue(XacmlTypes.STRING.bagBuilder().value("Moby Dick").build());
    }

    @Test
    public void contextSelectorThatSelectsNoValueOrMoreThanOneIsASyntaxError() {
        for (String context : new String[]{"$.store.car", "$.store.book[*]"}) {
            assertStatus(StatusCodeId.SYNTAX_ERROR,
                    () -> entity(XacmlTypes.JPATH.ofAny(context, CategoryId.RESOURCE)).resolve(contextSelector("$.title")));
        }
    }

    @Test
    public void contextSelectorWithoutItsAttributeOrInAnotherCategoryIsAProcessingError() {
        assertStatus(StatusCodeId.STATUS_PROCESSING_ERROR,
                () -> Entity.builder().content(content).build().resolve(contextSelector("$.title")));
        assertStatus(StatusCodeId.STATUS_PROCESSING_ERROR,
                () -> entity(XacmlTypes.JPATH.ofAny("$.store.book[2]", CategoryId.ACTION)).resolve(contextSelector("$.title")));
    }

    @Test
    public void selectorOfATypeWithoutStringFormConvertsOnlyASelectedValue() {
        Content json = JsonContent.of("{\"empty\": null, \"value\": \"x\"}");
        Truth8.assertThat(json.resolve(selector("$.missing", XacmlTypes.ENTITY))).isEmpty();
        Truth8.assertThat(json.resolve(selector("$.empty", XacmlTypes.ENTITY))).isEmpty();
        assertStatus(StatusCodeId.STATUS_PROCESSING_ERROR,
                () -> json.resolve(selector("$.value", XacmlTypes.ENTITY)));
    }

    @Test
    public void selectorsReadValuesWithAPathListConfiguration() {
        Content paths = JsonContent.of(JSON_DATA, Configuration.defaultConfiguration().addOptions(Option.AS_PATH_LIST));
        Truth8.assertThat(paths.resolve(selector("$.store.book[*].author", XacmlTypes.STRING)))
              .hasValue(XacmlTypes.STRING.bagBuilder()
                                         .value("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien")
                                         .build());
        Truth8.assertThat(entity(paths, XacmlTypes.JPATH.ofAny("$.store.book[2]", CategoryId.RESOURCE))
                                  .resolve(contextSelector("$.title")))
              .hasValue(XacmlTypes.STRING.bagBuilder().value("Moby Dick").build());
    }

    private Entity entity(Path contextPath) {
        return entity(content, contextPath);
    }

    private static Entity entity(Content content, Path contextPath) {
        return Entity.builder()
                     .content(content)
                     .attribute(Attribute.builder(XmlContent.CONTENT_SELECTOR).value(contextPath).build())
                     .build();
    }

    private static void assertStatus(StatusCodeId status, Executable resolve) {
        PathEvaluationException e = assertThrows(PathEvaluationException.class, resolve);
        assertThat(e.getStatus().getStatusCode().getValue()).isEqualTo(status);
    }

    private static AttributeSelectorKey selector(String path, ValueType type) {
        return AttributeSelectorKey.builder().jpath(path).dataType(type).build();
    }

    private static AttributeSelectorKey contextSelector(String path) {
        return AttributeSelectorKey.builder()
                                   .category(CategoryId.RESOURCE)
                                   .jpath(path)
                                   .contextSelectorId(XmlContent.CONTENT_SELECTOR)
                                   .dataType(XacmlTypes.STRING)
                                   .build();
    }
}
