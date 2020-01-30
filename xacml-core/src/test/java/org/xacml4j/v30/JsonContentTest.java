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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.truth.Truth8;
import org.junit.Before;
import org.junit.Test;


import java.util.List;
import java.util.Map;
import java.util.Optional;


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


    @Before
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
    public void testJsonPathEvalToNodePathAndNode(){
        Optional<String> path = content.evaluateToNodePath("$.store.book[0]");
        Truth8.assertThat(path).hasValue("$['store']['book'][0]");
        Optional<Object> node = content.evaluateToNode("$.store.book[0]");
        Truth8.assertThat(node).hasValue(
                Map.of(
                "category",
                "reference",
                "author", "Nigel Rees",
                "title", "Sayings of the Century",
                "price", 8.95));
    }

    @Test
    public void testEvaludateToNumber() {
        Optional<Number> price = content.evaluateToNumber("$.store.book[*].price");
        Truth8.assertThat(price).hasValue(8.95);
    }

    @Test
    public void testGetNode() {
        Optional<Content> content = Content.fromString(JSON_DATA);
        Truth8.assertThat(content).isPresent();
        System.out.println(content.map(v->v.toNode()).orElse(null).getClass());
    }
}
