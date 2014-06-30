package org.xacml4j.v30.marshal.jaxb;

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

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.ContentType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.XPathExpression;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.types.EntityExp;
import org.xacml4j.v30.types.TypeCapability;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public interface TypeToXacml30 extends TypeCapability
{
	AttributeValueType toXacml30(AttributeExp v);
	AttributeExp fromXacml30(AttributeValueType v);

	public enum Types implements TypeToXacml30
	{
		ANYURI(XacmlTypes.ANYURI){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				Preconditions.checkArgument(v.getType().equals(this));
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.Types.ANYURI.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					String s = (String)(v.getContent().get(0));
					return TypeToString.Types.ANYURI.fromString(s);
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		BOOLEAN(XacmlTypes.BOOLEAN){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				Preconditions.checkArgument(v.getType().equals(this));
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.Types.BOOLEAN.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.BOOLEAN
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		BASE64BINARY(XacmlTypes.BASE64BINARY){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.BASE64BINARY.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.BASE64BINARY
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		DATE(XacmlTypes.DATE){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.DATE.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.DATE
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		DATETIME(XacmlTypes.DATETIME){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.DATETIME.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.DATETIME
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		DAYTIMEDURATION(XacmlTypes.DAYTIMEDURATION){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.DAYTIMEDURATION.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.DAYTIMEDURATION
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		DNSNAME(XacmlTypes.DNSNAME){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.DNSNAME.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.DNSNAME
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException("No content found for the attribute value");
			}
		},
		DOUBLE(XacmlTypes.DOUBLE){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.DOUBLE.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.DOUBLE
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		ENTITY(XacmlTypes.ENTITY){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				Preconditions.checkArgument(v.getType().equals(this));
				AttributeValueType xacml30 = new AttributeValueType();
				xacml30.setDataType(v.getType().getDataTypeId());
				Entity entity = ((EntityExp)v).getValue();
				if(entity.getContent() != null){
					ContentType content = new ContentType();
					content.getContent().add(DOMUtil.copyNode(entity.getContent()));
					xacml30.getContent().add(XACML30_FACTORY.createContent(content));
				}
				for(Attribute a : entity.getAttributes()){
					xacml30.getContent().add(XACML30_FACTORY.createAttribute(Types.toXacml30(a)));
				}
				return xacml30;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				Entity.Builder b = Entity.builder();
				if(v.getContent().size() > 0 &&
						v.getContent().get(0) instanceof JAXBElement){
					JAXBElement<?> e = (JAXBElement<?>)v.getContent().get(0);
					int start = 0;
					if(e.getValue() instanceof ContentType){
						ContentType c = (ContentType)e.getValue();
						if(c.getContent().size() > 0){
							b.content((Node)(c.getContent().get(0)));
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
				return EntityExp.valueOf(b.build());
			}
		},
		HEXBINARY(XacmlTypes.HEXBINARY){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.HEXBINARY.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.HEXBINARY
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		INTEGER(XacmlTypes.INTEGER){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.INTEGER.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.INTEGER
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		IPADDRESS(XacmlTypes.IPADDRESS){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.IPADDRESS.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.IPADDRESS
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		RFC822NAME(XacmlTypes.RFC822NAME){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.RFC822NAME.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.RFC822NAME
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		STRING(XacmlTypes.STRING){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.STRING.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.STRING
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		TIME(XacmlTypes.TIME){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.TIME.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.TIME
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		X500NAME(XacmlTypes.X500NAME){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.X500NAME.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.X500NAME
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		XPATH(XacmlTypes.XPATH){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				Preconditions.checkArgument(v.getType().equals(this));
				AttributeValueType xacml30 = new AttributeValueType();
				XPathExp xpath = (XPathExp)v;
				xacml30.setDataType(XacmlTypes.XPATH.getDataTypeId());
				xacml30.getContent().add(xpath.getPath());
				xacml30.getOtherAttributes().put(XPATH_CATEGORY_ATTR_NAME, xpath.getCategory().getId());
				return xacml30;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				CategoryId categoryId = Categories.parse(v.getOtherAttributes().get(XPATH_CATEGORY_ATTR_NAME));
				if(v.getContent().size() > 0){
					if(categoryId == null){
						throw new XacmlSyntaxException(
								"XPath category can not be null");
					}
					return XPathExp.valueOf(
							new XPathExpression((String)(v.getContent().get(0)),
									categoryId));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		},
		YEARMONTHDURATION(XacmlTypes.YEARMONTHDURATION){
			@Override
			public AttributeValueType toXacml30(AttributeExp v) {
				AttributeValueType xacml = new AttributeValueType();
				xacml.setDataType(v.getType().getDataTypeId());
				xacml.getContent().add(TypeToString.
						Types.YEARMONTHDURATION.toString(v));
				return xacml;
			}

			@Override
			public AttributeExp fromXacml30(AttributeValueType v) {
				if(v.getContent().size() > 0){
					return TypeToString.Types.YEARMONTHDURATION
							.fromString((String)v.getContent().get(0));
				}
				throw new XacmlSyntaxException(
						"No content found for the attribute value");
			}
		};


		private final static ObjectFactory XACML30_FACTORY = new ObjectFactory();
		private final static TypeCapability.Index<TypeToXacml30> INDEX = TypeCapability.Index.<TypeToXacml30>build(values());
		public final static QName XPATH_CATEGORY_ATTR_NAME = new QName("XPathCategory");

		private AttributeExpType type;

		private Types(AttributeExpType t){
			this.type = t;
		}

		public AttributeExpType getType(){
			return type;
		}

		public static TypeCapability.Index<TypeToXacml30> getIndex(){
			return INDEX;
		}

		private static AttributeType toXacml30(Attribute a){
			AttributeType xacml30 = new AttributeType();
			xacml30.setAttributeId(a.getAttributeId());
			xacml30.setIssuer(a.getIssuer());
			xacml30.setIncludeInResult(a.isIncludeInResult());
			for(AttributeExp v : a.getValues()){
				Optional<TypeToXacml30> toXacml30 = INDEX.get(v.getType());
				Preconditions.checkState(toXacml30.isPresent());
				xacml30.getAttributeValue().add(toXacml30.get().toXacml30(v));
			}
			return xacml30;
		}

		private static Attribute fromXacml30(AttributeType attr) {
			Attribute.Builder b = Attribute.builder(attr.getAttributeId());
			b.issuer(attr.getIssuer());
			b.includeInResult(attr.isIncludeInResult());
			for(AttributeValueType v : attr.getAttributeValue()){
				Optional<TypeToXacml30> toXacml30 = INDEX.get(v.getDataType());
				Preconditions.checkState(toXacml30.isPresent());
				b.value(toXacml30.get().fromXacml30(v));
			}
			return b.build();
		}
	}
}
