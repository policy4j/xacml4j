package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.ContentType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.TypeCapability;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.types.EntityValue;
import org.xacml4j.v30.types.PathValue;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;

/**
 * XACML mapping to/from  between {@link Value} and
 * {@link org.oasis.xacml.v30.jaxb.AttributeValueType}  strategy for standard XACML types
 *
 * @author Giedrius Trumpickas
 */
public interface TypeToXacml30 extends TypeCapability
{
	/**
	 * Converts given {@link Value} to {@link org.oasis.xacml.v30.jaxb.AttributeValueType}
	 *
	 * @param v an attribute value
	 * @return {@link org.oasis.xacml.v30.jaxb.AttributeValueType}
	 */
	org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v);

	/**
	 * Converts given {@link org.oasis.xacml.v30.jaxb.AttributeValueType} to
	 * {@link ValueType}
	 *
	 * @param v an attribute value
	 * @return {@link Value}
	 */
	Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v);

	TypeToXacml30Factory systemFactory = new DefaultTypeToXacml30Factory();
	Map<ValueType, TypeToXacml30> capabilities = discoverCapabilities();

	static Optional<TypeToXacml30> forType(ValueType valueType){
		return Optional.ofNullable(capabilities.get(valueType));
	}

	static Optional<TypeToXacml30> forType(String valueType){
		return XacmlTypes.getType(valueType).map(t->capabilities.get(t));
	}

	static Map<ValueType, TypeToXacml30> discoverCapabilities(){
		return TypeCapability.discoverCapabilities(new DefaultTypeToXacml30Factory(),
		                                           TypeToXacml30.class, TypeToXacml30Factory.class);
	}


	enum Types implements TypeToXacml30
	{
		ANYURI(XacmlTypes.ANYURI){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(asTypeToString(TypeToString.Types.ANYURI).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() == 0){
					throw SyntaxException.noContentFound();

				}
				String s = (String)(v.getContent().get(0));
				return asTypeToString(TypeToString.Types.ANYURI).fromString(s);
			}
		},
		BOOLEAN(XacmlTypes.BOOLEAN){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(asTypeToString(TypeToString.Types.BOOLEAN).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() == 0){
					throw SyntaxException.noContentFound();
				}
				return asTypeToString(TypeToString.Types.BOOLEAN)
						.fromString((String) v.getContent().get(0));

			}
		},
		BASE64BINARY(XacmlTypes.BASE64BINARY){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.BASE64BINARY).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.BASE64BINARY)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		DATE(XacmlTypes.DATE){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.DATE).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.DATE)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		DATETIME(XacmlTypes.DATETIME){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.DATETIME).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.DATETIME)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		DAYTIMEDURATION(XacmlTypes.DAYTIMEDURATION){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.DAYTIMEDURATION).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.DAYTIMEDURATION)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		DNSNAME(XacmlTypes.DNSNAME){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.DNSNAME).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.DNSNAME)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException("No content found for the attribute value");
			}
		},
		DOUBLE(XacmlTypes.DOUBLE){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.DOUBLE).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.DOUBLE)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		ENTITY(XacmlTypes.ENTITY){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v)
			{
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml30 = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml30.setDataType(v.getEvaluatesTo().getDataTypeId());
				Entity entity = ((EntityValue)v).value();

				JAXBUtils.from(entity.getContent()).
						ifPresent(c->xacml30
				.getContent()
				.add(XACML30_FACTORY
						.createContent(c)));
				for(Attribute a : entity.getAttributes()){
					xacml30.getContent().add(XACML30_FACTORY
							.createAttribute(Types.toXacml30(a)));
				}
				return xacml30;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				Entity.Builder b = Entity.builder();
				if(v.getContent().size() > 0 &&
						v.getContent().get(0) instanceof JAXBElement){
					JAXBElement<?> e = (JAXBElement<?>)v.getContent().get(0);
					int start = 0;
					if(e.getValue() instanceof ContentType){
						ContentType c = (ContentType)e.getValue();
						if(c.getContent().size() > 0){
							Node node = (Node)(c.getContent().get(0));
							if(node.getNodeType() == Node.ELEMENT_NODE){
								b.xmlContent(node);
							}
							if(node.getNodeType() == Node.CDATA_SECTION_NODE){
								b.content(node.getTextContent());
							}
							start++;
						}
					}
					for(int i = start;  i < v.getContent().size(); i++){
						JAXBElement<?> attr = (JAXBElement<?>)v.getContent().get(i);
						Preconditions.checkState(attr.getValue() instanceof AttributeType);
						AttributeType xacml30Attr = (AttributeType)attr.getValue();
						b.attribute(Types.fromXacml30(xacml30Attr));
					}
				}
				return XacmlTypes.ENTITY.of(b.build());
			}
		},
		HEXBINARY(XacmlTypes.HEXBINARY){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.HEXBINARY).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.HEXBINARY)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		INTEGER(XacmlTypes.INTEGER){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.INTEGER).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.INTEGER)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		IPADDRESS(XacmlTypes.IPADDRESS){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.IPADDRESS).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.IPADDRESS)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		RFC822NAME(XacmlTypes.RFC822NAME){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.RFC822NAME).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.RFC822NAME)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		STRING(XacmlTypes.STRING){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(asTypeToString(TypeToString.Types.STRING).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.STRING)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		TIME(XacmlTypes.TIME){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.TIME).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.TIME)
							.fromString((String)v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		X500NAME(XacmlTypes.X500NAME){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.X500NAME).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() > 0){
					return asTypeToString(TypeToString.Types.X500NAME)
							.fromString((String) v.getContent().get(0));
				}
				throw new SyntaxException(
						"No content found for the attribute value");
			}
		},
		XPATH(XacmlTypes.XPATH){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml30 = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				PathValue xpath = (PathValue)v;
				xacml30.setDataType(XacmlTypes.XPATH.getDataTypeId());
				xacml30.getContent().add(xpath.getPath());
				if(xpath.getCategory().isPresent()){
					xacml30.getOtherAttributes().put(XPATH_CATEGORY_ATTR_NAME,
							xpath.getCategory().map(c->c.getId())
									.orElse(null));
				}
				return xacml30;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType a) {
				if(a.getContent().size() == 0){
					throw SyntaxException
							.noContentFound();
				}
				String categoryValue = a.getOtherAttributes().get(XPATH_CATEGORY_ATTR_NAME);
				String xpathValue = (String)a.getContent().get(0);
				Optional<Value> xpath = CategoryId
						.parse(categoryValue)
						.map(v->XacmlTypes.XPATH.of(xpathValue, v));
				return xpath.orElseThrow(()-> SyntaxException
						.invalidCategoryId(categoryValue));
			}
		},
		YEARMONTHDURATION(XacmlTypes.YEARMONTHDURATION){
			@Override
			public org.oasis.xacml.v30.jaxb.AttributeValueType toXacml30(Value v) {
				Preconditions.checkArgument(v.getEvaluatesTo().equals(this.getType()));
				org.oasis.xacml.v30.jaxb.AttributeValueType xacml = new org.oasis.xacml.v30.jaxb.AttributeValueType();
				xacml.setDataType(v.getEvaluatesTo().getDataTypeId());
				xacml.getContent().add(
						asTypeToString(TypeToString.Types.YEARMONTHDURATION).toString(v));
				return xacml;
			}

			@Override
			public Value fromXacml30(org.oasis.xacml.v30.jaxb.AttributeValueType v) {
				if(v.getContent().size() == 0){
					throw SyntaxException.noContentFound();

				}
				final Object firstContent = v.getContent().get(0);
				final String val = firstContent instanceof Element
						? ((Element) firstContent).getTextContent()
						: firstContent.toString();
				return asTypeToString(TypeToString.Types.YEARMONTHDURATION)
						.fromString(val);
			}
		};

		/**
		 * Hack method for Sun's JAVAC compiler to force enum to {@link org.xacml4j.v30.types.TypeToString}
		 * capability. Java 7 does not exhibit this issue.
		 * @param type type to be coerced
		 * @return coerced type
		 */
		private static TypeToString asTypeToString(TypeToString type) {
			return type;
		}


		private final static ObjectFactory XACML30_FACTORY = new ObjectFactory();
		public final static QName XPATH_CATEGORY_ATTR_NAME = new QName("XPathCategory");

		private ValueType type;

		Types(ValueType t){
			this.type = t;
		}

		public ValueType getType(){
			return type;
		}

		private static AttributeType toXacml30(Attribute a){
			AttributeType xacml30 = new AttributeType();
			xacml30.setAttributeId(a.getAttributeId());
			xacml30.setIssuer(a.getIssuer());
			xacml30.setIncludeInResult(a.isIncludeInResult());
			for(Value v : a.getValues()){
				java.util.Optional<TypeToXacml30> toXacml30 = forType(v.getEvaluatesTo());
				Preconditions.checkState(toXacml30.isPresent());
				xacml30.getAttributeValue().add(toXacml30.get().toXacml30(v));
			}
			return xacml30;
		}

		private static Attribute fromXacml30(AttributeType attr) {
			Attribute.Builder b = Attribute.builder(attr.getAttributeId());
			b.issuer(attr.getIssuer());
			b.includeInResult(attr.isIncludeInResult());
			for(org.oasis.xacml.v30.jaxb.AttributeValueType v : attr.getAttributeValue()){
				Optional<TypeToXacml30> toXacml30 = forType(v.getDataType());
				Preconditions.checkState(toXacml30.isPresent());
				b.value(toXacml30.get().fromXacml30(v));
			}
			return b.build();
		}
	}

	final class DefaultTypeToXacml30Factory
			extends TypeCapability.AbstractCapabilityFactory<TypeToXacml30> implements TypeToXacml30Factory
	{
		public DefaultTypeToXacml30Factory() {
			super(Arrays.asList(TypeToXacml30.Types.values()), TypeToXacml30.class);
		}
	}
}
