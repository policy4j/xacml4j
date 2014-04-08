package org.xacml4j.v30.marshal.json;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.types.EntityExp;
import org.xacml4j.v30.types.StringExp;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonEntityMarshallingTest 
{
	private  Node sampleContent1() throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		return documentBuilder.parse(new InputSource(new StringReader(
				"<security>\n<through obscurity=\"true\"></through></security>")));
	}
	
	private Gson json;
	
	@Before
	public void init(){
		this.json = new GsonBuilder()
				.registerTypeAdapter(Category.class, new CategoryAdapter())
				.registerTypeAdapter(Attribute.class, new AttributeSerializer())
				.registerTypeAdapter(Attribute.class, new AttributeDeserializer())
				.create();
	}
	
	@Test
	public void testEntityMarshall() throws Exception
	{
		Entity entity = Entity.builder()
				.content(sampleContent1())
				.attribute(Attribute
						.builder("testId3")
						.value(StringExp.valueOf("aaa"))
						.value(StringExp.valueOf("bbbb"))
						.value(StringExp.valueOf("cccc"))
						.build())
				.attribute(Attribute
						.builder("testId4")
						.value(StringExp.valueOf("zzzz"))
						.value(StringExp.valueOf("aaaa"))
						.value(StringExp.valueOf("cccc"))
						.build())
				.build();
		Category a = Category.builder()
		.category(Categories.SUBJECT_ACCESS)
		.entity(Entity.builder()
				.attribute(Attribute
						.builder("testId1")
						.value(EntityExp.valueOf(entity))
						.build())
			    .build())
	    .build();
		JsonElement o = json.toJsonTree(a);
		System.out.println(o.toString());
		Category b = json.fromJson(o, Category.class);
		assertEquals(a,  b);
	}
}
