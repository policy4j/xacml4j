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

import static com.google.common.truth.Truth8.assertThat;

import org.junit.Test;

public class ContentTest
{

    private final static String JSON_DATA_NOSPACE ="    {\n" +
            "  \"store\": {\n" +
            "    \"book\": [\n" +
            "      {\n" +
            "        \"category\": \"reference\",\n" +
            "        \"author\": \"Nigel Rees\",\n" +
            "        \"title\": \"Sayings of the Century\",\n" +
            "        \"price\": 8.95\n" +
            "      },\n" +
            "  },\n" +
            "  \"expensive\": 10\n" +
            "}";

    private final static String JSON_DATA_WHITESPACE ="{\n" +
            "  \"store\": {\n" +
            "    \"book\": [\n" +
            "      {\n" +
            "        \"category\": \"reference\",\n" +
            "        \"author\": \"Nigel Rees\",\n" +
            "        \"title\": \"Sayings of the Century\",\n" +
            "        \"price\": 8.95\n" +
            "      },\n" +
            "  },\n" +
            "  \"expensive\": 10\n" +
            "}";

    private final static String XML_DATA__NOSPACE = "<test>aaa</test>";
    private final static String XML_DATA__WHITESPACE = "     <test>aaa</test>";

    private final static String REGULAR_DATA = "Regular Text";

    @Test
    public void testFindType(){
        assertThat(Content.Type.getByMediaType("text/xml")).hasValue(Content.Type.XML_UTF8);
        assertThat(Content.Type.getByMediaType("application/xml")).hasValue(Content.Type.XML_UTF8);
        assertThat(Content.Type.getByMediaType("application/json")).hasValue(Content.Type.JSON_UTF8);

    }

    @Test
    public void testGetContentTypeFromContentJson(){
        assertThat(Content.resolveTypeFromContent(JSON_DATA_NOSPACE)).hasValue(Content.Type.JSON_UTF8);
        assertThat(Content.resolveTypeFromContent(JSON_DATA_WHITESPACE)).hasValue(Content.Type.JSON_UTF8);

    }

    @Test
    public void testGetContentTypeFromContentXml(){
        assertThat(Content.resolveTypeFromContent(XML_DATA__NOSPACE)).hasValue(Content.Type.XML_UTF8);
        assertThat(Content.resolveTypeFromContent(XML_DATA__WHITESPACE)).hasValue(Content.Type.XML_UTF8);
    }

    @Test
    public void testGetContentTypeFromContentText(){
        assertThat(Content.resolveTypeFromContent(REGULAR_DATA)).isEmpty();
    }
}
