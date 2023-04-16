package org.xacml4j.v30.content;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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

import org.junit.Ignore;
import org.junit.Test;
import org.xacml4j.v30.Content;

import static com.google.common.truth.Truth.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Content;

import com.google.common.truth.Truth8;

public class ContentTest
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

	private final static String XML_DATA = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"xmlns:md=\"urn:example:med:schemas:record\">" +
			"<md:patient>" +
			"<md:patientDoB md:attrn1=\"test\" attrn2=\"v\" >1991-03-21</md:patientDoB>" +
			"<md:patient-number>66666</md:patient-number>" +
			"</md:patient>" +
			"<md:patient>" +
			"<md:patientDoB md:attrn1=\"test1\" attrn2=\"v1\">1992-01-11</md:patientDoB>" +
			"<md:patient-number>12394</md:patient-number>" +
			"</md:patient>" +
			"</md:record>";

	@Test
	public void testJson(){
		Truth8.assertThat(Content.fromString(JSON_DATA)).isPresent();
	}

	@Test
	public void testJsonBase64(){
		Truth8.assertThat(Content.fromString(Base64.getEncoder()
		                                           .encodeToString(JSON_DATA.getBytes(StandardCharsets.UTF_8)))).isPresent();
	}

	@Test
	public void testXml(){
		Truth8.assertThat(Content.fromString(XML_DATA)).isPresent();
	}

	@Test
	@Ignore
	public void testXmlBase64(){
		String base64 = new String(Base64.getEncoder()
		                      .encode(XML_DATA.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		String decoded = new String(Base64.getDecoder()
		                      .decode(base64));
		Truth8.assertThat(Content.fromString(decoded)).isPresent();
		Truth8.assertThat(Content.fromString(base64)).isPresent();
	}
}
