package com.artagon.xacml.v30.marshall;

import static com.artagon.xacml.v30.marshall.XMLStreamReaderUtil.getBooleanAttributeValue;
import static com.artagon.xacml.v30.marshall.XMLStreamReaderUtil.getStringAttributeValue;
import static com.artagon.xacml.v30.marshall.XMLStreamReaderUtil.writeAttribute;
import static com.artagon.xacml.v30.marshall.XMLStreamReaderUtil.writeCharacters;
import static com.artagon.xacml.v30.marshall.XMLStreamReaderUtil.writeEmptyElement;
import static com.artagon.xacml.v30.marshall.XMLStreamReaderUtil.writeEndElement;
import static com.artagon.xacml.v30.marshall.XMLStreamReaderUtil.writeStartElement;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Xacml20XPathTo30Transformer;
import com.artagon.xacml.v30.Apply;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeDesignator;
import com.artagon.xacml.v30.AttributeSelector;
import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.DefaultExpressionVisitor;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.FunctionReference;
import com.artagon.xacml.v30.VariableReference;
import com.artagon.xacml.v30.XacmlSyntaxException;
import com.artagon.xacml.v30.marshall.jaxb.XacmlPolicyParsingContext;
import com.google.common.base.Preconditions;

public enum XacmlExpressionNode 
{
	APPLY_XACML30_WD17("Apply", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17") 
	{
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(
					r.getName().equals(getName())
					&& r.isStartElement());
			String funcId = r.getAttributeValue(null, "FunctionId");
			if(funcId == null){
				throw new XacmlSyntaxException(r.getLocation(), 
						"Function identifier can't be null");
			}
			List<Expression> params = new LinkedList<Expression>();
			while(!r.isEndElement()) {
				r.nextTag();
				params.add(parse(r, context));
			}
			r.nextTag();
			return new Apply(context.getFunction(funcId), params);
		}
	},
	APPLY_XACML30("Apply", "urn:oasis:names:tc:xacml:3.0:core:schema") 
	{
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(
					r.getName().equals(getName())
					&& r.isStartElement());
			String funcId = r.getAttributeValue(null, "FunctionId");
			if(funcId == null){
				throw new XacmlSyntaxException(r.getLocation(), 
						"Function identifier can't be null");
			}
			List<Expression> params = new LinkedList<Expression>();
			while(!r.isEndElement()) {
				r.nextTag();
				params.add(parse(r, context));
			}
			r.nextTag();
			return new Apply(context.getFunction(funcId), params);
		}
	},
	APPLY_XACML20("Apply", "urn:oasis:names:tc:xacml:2.0:policy:schema:os") 
	{
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(
					r.getName().equals(getName())
					&& r.isStartElement());
			String funcId = r.getAttributeValue(null, "FunctionId");
			if(funcId == null){
				throw new XacmlSyntaxException(r.getLocation(), 
						"Function identifier can't be null");
			}
			List<Expression> params = new LinkedList<Expression>();
			while(!r.isEndElement()) {
				r.nextTag();
				params.add(parse(r, context));
			}
			r.nextTag();
			return new Apply(context.getFunction(funcId), params);
		}
	},
	VARIABLE_REFERENCE_XACML30_WD17("VariableReference", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			return new VariableReference(
					context.getVariable(getStringAttributeValue("VariableId", r)));
		}
	},
	VARIABLE_REFERENCE_XACML30("VariableReference", "urn:oasis:names:tc:xacml:3.0:core:schema") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			return new VariableReference(
					context.getVariable(getStringAttributeValue("VariableId", r)));
		}
	},
	ATTRIBUTE_SELECTOR_XACML30_WD17("AttributeSelector", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeSelector sel = new AttributeSelector(
					AttributeCategories.parse(getStringAttributeValue(
							"Category", r)),
					getStringAttributeValue("Path", r),
					getStringAttributeValue("ContextSelectorId", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return sel;
		}
	},
	ATTRIBUTE_SELECTOR_XACML30("AttributeSelector", "urn:oasis:names:tc:xacml:3.0:core:schema") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeSelector sel = new AttributeSelector(
					AttributeCategories.parse(getStringAttributeValue(
							"Category", r)),
					getStringAttributeValue("Path", r),
					getStringAttributeValue("ContextSelectorId", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return sel;
		}
	},
	ATTRIBUTE_SELECTOR_XACML20("AttributeSelector", "urn:oasis:names:tc:xacml:2.0:policy:schema:os") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeSelector sel = new AttributeSelector(
					AttributeCategories.RESOURCE,
					Xacml20XPathTo30Transformer.transform20PathTo30(
							getStringAttributeValue("RequestContextPath", r)),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return sel;
		}
	},
	ATTRIBUTE_DESIGNATOR_WD17("AttributeDesignator", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeDesignator desig = new AttributeDesignator(
					AttributeCategories.parse(getStringAttributeValue("Category", r)), 
					getStringAttributeValue("AttributeId", r),
					getStringAttributeValue("Issuer", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return desig;
		}
	},
	ATTRIBUTE_DESIGNATOR_XACML30("AttributeDesignator", "urn:oasis:names:tc:xacml:3.0:core:schema") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeDesignator desig = new AttributeDesignator(
					AttributeCategories.parse(getStringAttributeValue("Category", r)), 
					getStringAttributeValue("AttributeId", r),
					getStringAttributeValue("Issuer", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return desig;
		}
	},
	ATTRIBUTE_DESIGNATOR_XACML30_WD17("AttributeDesignator", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeDesignator desig = new AttributeDesignator(
					AttributeCategories.parse(
							getStringAttributeValue("Category", r)), 
					getStringAttributeValue("AttributeId", r),
					getStringAttributeValue("Issuer", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return desig;
		}
	},
	SUBJECT_ATTRIBUTE_DESIGNATOR_XACML20("SubjectAttributeDesignator", "urn:oasis:names:tc:xacml:2.0:policy:schema:os") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			String subjectCategory = getStringAttributeValue("SubjectCategory", r);
			AttributeDesignator desig = new AttributeDesignator(
					(subjectCategory != null)?AttributeCategories.parse(subjectCategory):AttributeCategories.SUBJECT_ACCESS,
					getStringAttributeValue("AttributeId", r),
					getStringAttributeValue("Issuer", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return desig;
		}
	},
	ACTION_ATTRIBUTE_DESIGNATOR_XACML20("ActionAttributeDesignator", "urn:oasis:names:tc:xacml:2.0:policy:schema:os") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeDesignator desig = new AttributeDesignator(
					AttributeCategories.ACTION, 
					getStringAttributeValue("AttributeId", r),
					getStringAttributeValue("Issuer", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return desig;
		}
	},
	ENVIROMENT_ATTRIBUTE_DESIGNATOR_XACML20("EnviromentAttributeDesignator", "urn:oasis:names:tc:xacml:2.0:policy:schema:os") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeDesignator desig = new AttributeDesignator(
					AttributeCategories.ENVIRONMENT, 
					getStringAttributeValue("AttributeId", r),
					getStringAttributeValue("Issuer", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return desig;
		}
	},
	RESOURCE_ATTRIBUTE_DESIGNATOR_XACML20("ResourceAttributeDesignator", "urn:oasis:names:tc:xacml:2.0:policy:schema:os") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeDesignator desig = new AttributeDesignator(
					AttributeCategories.RESOURCE, 
					getStringAttributeValue("AttributeId", r),
					getStringAttributeValue("Issuer", r),
					context.getType(getStringAttributeValue("DataType", r)),
					getBooleanAttributeValue("MustBePresent", r));
			r.nextTag();
			return desig;
		}
	},
	FUNCTION_REF_XACML30_WD17("Function", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getLocalName().equals(getName())
					&& r.isStartElement());
			FunctionReference func = new FunctionReference(
					context.getFunction(getStringAttributeValue("FunctionId", r)));
			r.nextTag();
			return func;
		}
	},
	FUNCTION_REF_XACML30("Function", "urn:oasis:names:tc:xacml:3.0:core:schema") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getLocalName().equals(getName())
					&& r.isStartElement());
			FunctionReference func = new FunctionReference(
					context.getFunction(getStringAttributeValue("FunctionId", r)));
			r.nextTag();
			return func;
		}
	},
	FUNCTION_REF_XACML20("Function", "urn:oasis:names:tc:xacml:2.0:policy:schema:os") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getLocalName().equals(getName())
					&& r.isStartElement());
			FunctionReference func = new FunctionReference(
					context.getFunction(getStringAttributeValue("FunctionId", r)));
			r.nextTag();
			return func;
		}
	},
	ATTRIBUTE_VALUE_XACML20("AttributeValue", "urn:oasis:names:tc:xacml:2.0:policy:schema:os") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeValueType dataType = context
					.getType(getStringAttributeValue("DataType", r));
			return dataType.fromXacmlString(r.getElementText());
		}
	},
	ATTRIBUTE_VALUE_XACML30_WD17("AttributeValue", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeValueType dataType = context
					.getType(getStringAttributeValue("DataType", r));
			AttributeCategory category = null;
			String categoryValue = getStringAttributeValue("Category", r);
			if (categoryValue != null) {
				category = AttributeCategories.parse(categoryValue);
			}
			AttributeValue v = dataType.fromXacmlString(r.getElementText(),
					category);
			return v;
		}
	},
	ATTRIBUTE_VALUE_XACML30("AttributeValue", "urn:oasis:names:tc:xacml:3.0:core:schema") {
		@Override
		public Expression create(XMLStreamReader r,
				XacmlPolicyParsingContext context) throws XMLStreamException,
				XacmlSyntaxException {
			Preconditions.checkArgument(r.getName().equals(getName())
					&& r.isStartElement());
			AttributeValueType dataType = context
					.getType(getStringAttributeValue("DataType", r));
			AttributeCategory category = null;
			String categoryValue = getStringAttributeValue("Category", r);
			if (categoryValue != null) {
				category = AttributeCategories.parse(categoryValue);
			}
			AttributeValue v = dataType.fromXacmlString(r.getElementText(),
					category);
			return v;
		}
	};

	private final static Logger log = LoggerFactory.getLogger(XacmlExpressionNode.class);
	
	private static final Map<QName, XacmlExpressionNode> BY_NAME = new HashMap<QName, XacmlExpressionNode>();

	static {
		for (XacmlExpressionNode parser : EnumSet.allOf(XacmlExpressionNode.class)) {
			BY_NAME.put(parser.getName(), parser);
		}
	}

	private QName name;

	private XacmlExpressionNode(String name, String namespaceURI) {
		this.name = new QName(namespaceURI, name);
	}

	/**
	 * Gets fully qualified node name
	 * 
	 * @return {@link QName}
	 */
	public QName getName() {
		return name;
	}
	
	public abstract Expression create(XMLStreamReader r, XacmlPolicyParsingContext context) 
		throws XMLStreamException, XacmlSyntaxException;

	/**
	 * Creates an {@link Expression} instance from a
	 * given XML stream
	 * 
	 * @param r an XML stream reader
	 * @param context an parsing context
	 * @return {@link Expression}
	 * @throws XMLStreamException
	 * @throws XacmlSyntaxException
	 */
	public static Expression parse(XMLStreamReader r,
			XacmlPolicyParsingContext context) 
		throws XMLStreamException, XacmlSyntaxException 
	{
		Preconditions.checkArgument(r.isStartElement());
		QName n = r.getName();
		if(log.isDebugEnabled()){
			log.debug("Parsing element localName=\"{}\" " +
					"namespaceURI=\"{}\"", 
					n.getLocalPart(), n.getNamespaceURI());
		}
		XacmlExpressionNode f = BY_NAME.get(n);
		Preconditions.checkState(f != null);
		return f.create(r, context);
	}
	
	/**
	 * Serializes given {@link  Expression} to
	 * a given XML stream
	 * @param exp an XACML expression
	 * @param w an XML stream writer
	 * @throws XMLStreamException
	 */
	public static void write(Expression exp, 
			final XMLStreamWriter w) 
		throws XMLStreamException
	{
		exp.accept(new DefaultExpressionVisitor(){
			@Override
			public void visitEnter(Apply v) {
				writeStartElement(APPLY_XACML30.name, w);
				writeAttribute("FunctionId", v.getFunctionId(), w);
			}
			@Override
			public void visitLeave(Apply v) {
				writeEndElement(w);
			}
			@Override
			public void visitEnter(AttributeValue v) {
				writeStartElement(ATTRIBUTE_VALUE_XACML30.name, w);
				writeAttribute("DataType", v.getType().getDataTypeId(), w);
				writeCharacters(v.toXacmlString(), w);
				writeEndElement(w);
			}
			@Override
			public void visitEnter(FunctionReference v) {
				writeEmptyElement(FUNCTION_REF_XACML30.name, w);
				writeAttribute("FunctionId", v.getFunctionId(), w);
				writeEndElement(w);
			}
			@Override
			public void visitEnter(VariableReference v) {
				writeEmptyElement(VARIABLE_REFERENCE_XACML30.name, w);
				writeAttribute("VariableId", v.getVariableId(), w);
				writeEndElement(w);
			}
			@Override
			public void visitEnter(AttributeSelector v) {
				writeEmptyElement(ATTRIBUTE_SELECTOR_XACML30.name, w);
				writeAttribute("Category", v.getCategory().getId(), w);
				writeAttribute("ContextSelectorId", v.getContextSelectorId(), w);
				writeAttribute("DataTypeId", v.getDataType().getDataTypeId(), w);
				writeAttribute("Path", v.getPath(), w);
				writeAttribute("MustBePresent", Boolean.toString(v.isMustBePresent()), w);
			}
			@Override
			public void visitEnter(AttributeDesignator v) {
				writeEmptyElement(ATTRIBUTE_DESIGNATOR_XACML30.name, w);
				writeAttribute("AttributeId", v.getAttributeId(), w);
				writeAttribute("Category", v.getCategory().getId(), w);
				writeAttribute("DataTypeId", v.getDataType().getDataTypeId(), w);
				writeAttribute("MustBePresent", Boolean.toString(v.isMustBePresent()), w);
			}
		});
	}
}
