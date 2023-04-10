package org.xacml4j.v30.types;

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

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xpath.XPathExpression;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.BagOfValuesType;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.Path;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;

import com.google.common.collect.ImmutableSet;

/**
 * Enumeration of the standard system XACML 3.0 data types
 *
 * @author Giedrius Trumpickas
 */
public enum XacmlTypes implements ValueType
{
	ANYURI("http://www.w3.org/2001/XMLSchema#anyURI", "anyURI") {
		public AnyURIValue of(Object v, Object... params) {
			return AnyURIValue.
					fromObjectWithParams(v, params);
		}
	},
	BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary", "base64Binary") {
		public Base64BinaryValue of(Object v, Object... params) {
			return Base64BinaryValue
					.fromObjectWithParams(v, params);
		}
	},
	BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean", "boolean") {
		public BooleanValue of(Object v, Object... params) {
			return BooleanValue
					.fromObjectWithParams(v, params);
		}
	},
	DATE("http://www.w3.org/2001/XMLSchema#date", "date") {
		public DateValue of(Object v, Object... params) {
			return DateValue
					.fromObjectWithParams(v, params);
		}
	},
	DATETIME("http://www.w3.org/2001/XMLSchema#dateTime", "dateTime") {
		public DateTimeValue of(Object v, Object... params) {
			return DateTimeValue
					.fromObjectWithParams(v, params);
		}
	},
	DAYTIMEDURATION("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "dayTimeDuration",
			"http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration") {
		public DayTimeDurationValue of(Object v, Object... params) {
			return Optional.of(DayTimeDurationValue.of(
					DayTimeDuration.parse(v)))
					.orElseThrow(() -> SyntaxException
							.invalidAttributeValue(v, this));
		}
	},
	DNSNAME("urn:oasis:names:tc:xacml:2.0:data-type:dnsName", "dnsName") {
		public DNSNameValue of(Object v, Object... params) {
			Optional<DNSNameValue> a = Optional.empty();
			if (v instanceof String) {
				a = Optional.of(
						DNSNameValue.of(
								(String) v));
			}
			if (v instanceof StringValue) {
				a = Optional.of(
						DNSNameValue.of(
								((StringValue) v).value()));
			}
			if (v instanceof DNSNameValue) {
				a = Optional.of((DNSNameValue) v);
			}
			return a.orElseThrow(() ->
					SyntaxException
							.invalidAttributeValue(
									v, this));
		}
	},
	DOUBLE("http://www.w3.org/2001/XMLSchema#double", "double") {
		public DoubleValue of(Object v, Object... params) {
			if (v instanceof String) {
				return DoubleValue.of((String) v);
			}
			if (v instanceof StringValue) {
				return DoubleValue.of(((StringValue) v).value());
			}
			return DoubleValue.of((Number) v);
		}
	},
	INTEGER("http://www.w3.org/2001/XMLSchema#integer", "integer") {
		public IntegerValue of(Object v) {
			return of(v, (Object[]) null);
		}

		public IntegerValue of(Object v, Object... params) {
			if (v instanceof String) {
				return IntegerValue.of((String) v);
			}
			if (v instanceof StringValue) {
				return IntegerValue.of(((StringValue) v).value());
			}
			return IntegerValue.of((Number) v);
		}
	},
	HEXBINARY("http://www.w3.org/2001/XMLSchema#hexBinary", "hexBinary") {
		public HexBinaryValue of(Object v, Object... params) {
			if (v instanceof String) {
				return HexBinaryValue.of((String) v);
			}
			if (v instanceof StringValue) {
				return HexBinaryValue.of(((StringValue) v).value());
			}
			if (v instanceof byte[]) {
				return HexBinaryValue.of((byte[]) v);
			}
			return HexBinaryValue.of((Binary) v);
		}
	},
	IPADDRESS("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", "ipAddress") {
		public IPAddressValue of(Object v, Object... params) {
			if (v instanceof String) {
				return IPAddressValue.of((String) v);
			}
			if (v instanceof StringValue) {
				return IPAddressValue.of(((StringValue) v).value());
			}
			return IPAddressValue.of((IPAddress) v);
		}
	},
	STRING("http://www.w3.org/2001/XMLSchema#string", "string") {
		public StringValue of(Object v, Object... params) {
			if (v instanceof StringValue) {
				return StringValue.of(((StringValue) v).value());
			}
			return StringValue.of((String) v);
		}
	},
	RFC822NAME("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", "rfc822Name") {
		public RFC822NameValue of(Object v, Object... params) {
			Optional<RFC822NameValue> a = Optional.empty();
			if (v instanceof String) {
				a = Optional.of(
						RFC822NameValue.of(
								(String) v));
			}
			if (v instanceof StringValue) {
				a = Optional.of(
						RFC822NameValue.of(
								((StringValue) v).value()));
			}
			if (v instanceof RFC822Name) {
				a = Optional.of(
						RFC822NameValue.of(
								(RFC822Name) v));
			}
			return a.orElseThrow(
					() -> SyntaxException.invalidAttributeValue(
							v, this));
		}
	},
	TIME("http://www.w3.org/2001/XMLSchema#time", "time") {
		public TimeValue of(Object v, Object... params) {
			Optional<TimeValue> a = Optional.empty();
			if (v instanceof String) {
				a = Optional.of(
						TimeValue.of((String) v));
			}
			if (v instanceof StringValue) {
				a = Optional.of(
						TimeValue.of(((StringValue) v).value()));
			}
			if (v instanceof XMLGregorianCalendar) {
				a = Optional.of(
						TimeValue.of(
								(XMLGregorianCalendar) v));
			}
			if (v instanceof Calendar) {
				a = Optional.of(
						TimeValue.of((Calendar) v));
			}
			if (v instanceof Time) {
				a = Optional.of(
						TimeValue.of((Time) v));
			}

			if (v instanceof Instant) {
				a = Optional.of(
						TimeValue.of((Time) v));
			}
			return a.orElseThrow(
					() -> SyntaxException
							.invalidAttributeValue(
									v, this));
		}
	},
	X500NAME("urn:oasis:names:tc:xacml:1.0:data-type:x500Name", "x500Name") {
		public X500NameValue of(Object v, Object... params) {
			Optional<X500NameValue> a = Optional.empty();
			if (v instanceof String) {
				a = Optional.of(
						X500NameValue.of(
								String.class.cast(v)));
			}
			if (v instanceof X500NameValue) {
				a = Optional.of(
						X500NameValue.class.cast(v));
			}
			if (v instanceof StringValue) {
				a = Optional.of(
						X500NameValue.of(
								((StringValue) v).value()));
			}
			if (v instanceof X500Principal) {
				a = Optional.of(
						X500NameValue.of(
								(X500Principal) v));
			}
			return a.orElseThrow(
					() -> SyntaxException
							.invalidAttributeValue(
									v, this));
		}
	},
	XPATH("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression",
			"xpathExpression", "urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression",
			"urn:oasis:names:tc:xacml:2.0:data-type:xpath-expression") {
		public PathValue of(Object v, Object... params) {
			CategoryId categoryId = getCategory(params).orElse(CategoryId.RESOURCE);
			Optional<PathValue> a = Optional.empty();
			if (v instanceof String) {
				a = Optional.of(
						PathValue.of(
								Path.of(
										(String) v,
										categoryId,
										Content.PathType.XPATH)));
			}
			if (v instanceof XPathExpression) {
				a = Optional.of(
						PathValue.of(
								Path.of(
										(String) v,
										categoryId,
										Content.PathType.XPATH)));
			}
			if (v instanceof Path &&
					(((Path) v).getType() == Content.PathType.XPATH)) {
				a = Optional.of(
						PathValue.of(
								(Path) v));
			}
			return a.orElseThrow(
					() -> SyntaxException
							.invalidAttributeValue(v, this));
		}
	},
	JPATH("urn:oasis:names:tc:xacml:3.0:data-type:jsonPathExpression",
			"jsonPathExpression") {
		public PathValue of(Object v, Object... params) {
			CategoryId categoryId = getCategory(params)
					.orElse(CategoryId.RESOURCE);
			Optional<PathValue> a = Optional.empty();
			if (v instanceof String) {
				a = Optional.of(
						PathValue.of(
								Path.of(
										(String) v,
										categoryId,
										Content.PathType.JPATH)));
			}
			if ((v instanceof Path) &&
					((Path) v).getType() == Content.PathType.JPATH) {
				a = Optional.of(
						PathValue.of(
								(Path) v));
			}
			return a.orElseThrow(
					() -> SyntaxException
							.invalidAttributeValue(
									v, this));
		}
	},
	YEARMONTHDURATION("http://www.w3.org/2001/XMLSchema#yearMonthDuration",
			"yearMonthDuration", "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration") {
		public YearMonthDurationValue of(Object v, Object... params) {
			if (v instanceof String) {
				return YearMonthDurationValue.of((String) v);
			}
			if (v instanceof StringValue) {
				return YearMonthDurationValue.of(((StringValue) v).value());
			}
			if (v instanceof Duration) {
				return YearMonthDurationValue.of((Duration) v);
			}
			return YearMonthDurationValue.of((YearMonthDuration) v);
		}
	},
	ENTITY("urn:oasis:names:tc:xacml:3.0:data-type:entity", "entity") {
		public EntityValue of(Object v, Object... params) {
			if (v instanceof Value) {
				Entity.Builder b = Entity.builder();
				if (params != null || params.length > 0) {
					b.attributes(
							Arrays.stream(params)
									.filter(p -> p != null && p instanceof Attribute)
									.map(a -> (Attribute) a).collect(Collectors.toList()));
				}
				return EntityValue.of(b.build());
			}
			return EntityValue.of((Entity) v);
		}
	};



	private String typeId;
	private String shortTypeId;
	private Set<String> aliases;
	private BagOfValuesType bagType;


	XacmlTypes(String typeId, String shortTypeId, String... aliases) {
		this.typeId = typeId;
		this.shortTypeId = shortTypeId;
		this.bagType = new BagOfValuesType(this);
		this.aliases = (aliases == null) ? ImmutableSet.<String>of() : ImmutableSet
				.<String>builder()
				.add(shortTypeId)
				.add(aliases)
				.build();
	}

	/**
	 * Gets type via type identifier or alias
	 *
	 * @param typeId a type identifier, supported type identifiers:
	 * {@link String}, {@link URI}, {@link URI}
	 * @return {@link Optional} with resolved type
	 */
	public static Optional<ValueType> getType(Object typeId){
		if(typeId instanceof ValueType){
			return Optional.of((ValueType)typeId);
		}
		if(typeId instanceof String){
			return systemTypes.forType(typeId.toString());
		}
		if(typeId instanceof URI){
			return systemTypes.forType(typeId.toString());
		}
		if(typeId instanceof Value){
			Value a = (Value)typeId;
			if(a.getType().equals(XacmlTypes.STRING) || a.getType().equals(ANYURI)){
				return systemTypes.forType(a.value().toString());
			}
		}
		return Optional.empty();
	}

	/**
	 * Optimized version always returns
	 * @return
	 */
	@Override
	public BagOfValuesType bagType() {
		return bagType;
	}

	@Override
	public final String getDataTypeId() {
		return typeId;
	}

	@Override
	public final String getAbbrevDataTypeId() {
		return shortTypeId;
	}

	public Set<String> getDataTypeIdAliases(){
		return aliases;
	}

	@Override
	public String toString(){
		return shortTypeId;
	}

	public ValueType getDataType(){
		return this;
	}


	static Optional<CategoryId> getCategory(Object ...params){
		return (params != null && params.length > 0)?
				CategoryId.parse(params[0])
				: Optional.empty();
    }

	private final static TypeFactory systemTypes = new SystemTypes();

	static class SystemTypes extends TypeFactory.BaseTypeFactory
	{
		public SystemTypes() {
			super(Arrays.asList(XacmlTypes.values()));
		}
	}

}
