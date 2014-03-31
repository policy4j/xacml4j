package org.xacml4j.v30.marshal.json;

import java.io.StringReader;

import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.AttributesReference;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.types.EntityExp;
import org.xacml4j.v30.types.StringExp;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
				.registerTypeAdapter(AttributeExp.class, new AttributeExpSerializer())
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
						.build()).build();
		Category a = Category.builder()
		.category(Categories.SUBJECT_ACCESS)
		.entity(Entity.builder()
				.content(sampleContent1())
				.attribute(Attribute
						.builder("testId1")
						.value(EntityExp.valueOf(entity))
						.build())
			    .build())
	    .build();
		JsonElement o = json.toJsonTree(a);
		System.out.println(o.toString());
	}
}
