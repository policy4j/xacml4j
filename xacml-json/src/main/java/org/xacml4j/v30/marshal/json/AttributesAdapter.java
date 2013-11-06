package org.xacml4j.v30.marshal.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xml.sax.InputSource;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

class AttributesAdapter implements JsonDeserializer<Attributes>, JsonSerializer<Attributes> {
	private static final String CATEGORY_PROPERTY = "Category";
	private static final String ID_PROPERTY = "Id";
	private static final String CONTENT_PROPERTY = "Content";
	private static final String ATTRIBUTE_PROPERTY = "Attribute";

	private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
	private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

	@Override
	public Attributes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		try {
			JsonObject o = json.getAsJsonObject();
			Collection<Attribute> attr = context.deserialize(o.getAsJsonArray(ATTRIBUTE_PROPERTY),
					new TypeToken<Collection<Attribute>>() {
					}.getType());
			AttributeCategory category = AttributeCategories.parse(GsonUtil.getAsString(o, CATEGORY_PROPERTY, null));
			Node content = stringToNode(GsonUtil.getAsString(o, CONTENT_PROPERTY, null));
			String id = GsonUtil.getAsString(o, ID_PROPERTY, null);

			return Attributes.builder(category).id(id).attributes(attr).content(content).build();
		} catch (XacmlSyntaxException e) {
			throw new JsonParseException(e);
		}
	}

	private Node stringToNode(String str) {
		if (str == null) {
			return null;
		}

		final DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(String.format("Failed to build %s", DocumentBuilder.class.getName()), e);
		}

		InputSource source = new InputSource(new StringReader(str));
		try {
			return documentBuilder.parse(source);
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("Failed to parse DOM from string: \"%s\"", str), e);
		}
	}

	@Override
	public JsonElement serialize(Attributes src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		if (src.getId() != null) {
			o.addProperty(ID_PROPERTY, src.getId());
		}
		o.addProperty(CATEGORY_PROPERTY, src.getCategory().getId());
		o.addProperty(CONTENT_PROPERTY, nodeToString(src.getContent()));
		o.add(ATTRIBUTE_PROPERTY, context.serialize(src.getAttributes()));
		return o;
	}

	private String nodeToString(Node node) {
		if (node == null) {
			return null;
		}

		final Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new IllegalStateException(String.format("Failed to build %s", Transformer.class.getName()), e);
		}

		DOMSource source = new DOMSource(node);
		StreamResult result = new StreamResult(new StringWriter());
		try {
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (TransformerException e) {
			// TODO: should the content serialization be fatal?
			throw new IllegalArgumentException("Failed to serialize Node to String.", e);
		}
	}

}
