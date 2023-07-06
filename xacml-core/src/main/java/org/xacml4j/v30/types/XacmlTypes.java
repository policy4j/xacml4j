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
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.xacml4j.v30.BagOfValuesType;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.SyntaxException;

import com.google.common.collect.ImmutableSet;

/**
 * Enumeration of the standard system XACML 3.0 data types
 *
 * @author Giedrius Trumpickas
 */
public enum XacmlTypes implements ValueType
{
	ANYURI("http://www.w3.org/2001/XMLSchema#anyURI", "anyURI") {
		public AnyURI ofAny(Object v, Object... params) {
			return AnyURI.
					ofAny(v, params);
		}
	},
	BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary", "base64Binary") {
		public Base64Binary ofAny(Object v, Object... params) {
			return Base64Binary
					.ofAny(v, params);
		}
	},
	BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean", "boolean") {
		public BooleanVal ofAny(Object v, Object... params) {
			return BooleanVal
					.ofAny(v, params);
		}
	},
	DATE("http://www.w3.org/2001/XMLSchema#date", "date") {
		public ISO8601Date ofAny(Object v, Object... params) {
			return ISO8601Date
					.ofAny(v, params);
		}
	},
	DATETIME("http://www.w3.org/2001/XMLSchema#dateTime", "dateTime") {
		public ISO8601DateTime ofAny(Object v, Object... params) {
			return ISO8601DateTime
					.ofAny(v, params);
		}
	},
	DAYTIMEDURATION("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "dayTimeDuration",
			"http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration") {
		public ISO8601DayTimeDuration ofAny(Object v, Object... params) {
			return ISO8601DayTimeDuration
					.ofAny(v, params);
		}
	},
	DNSNAME("urn:oasis:names:tc:xacml:2.0:data-type:dnsName", "dnsName") {
		public DNSName ofAny(Object v, Object... params) {
			return DNSName.ofAny(v, params);
		}
	},
	DOUBLE("http://www.w3.org/2001/XMLSchema#double", "double") {
		public DoubleVal ofAny(Object v, Object... params) {
			return DoubleVal
					.ofAny(v, params);
		}
	},
	INTEGER("http://www.w3.org/2001/XMLSchema#integer", "integer") {

		public IntegerVal ofAny(Object v, Object... params) {
			return IntegerVal
					.ofAny(v, params);
		}
	},
	HEXBINARY("http://www.w3.org/2001/XMLSchema#hexBinary", "hexBinary") {
		public HexBinary ofAny(Object v, Object... params) {
			return HexBinary
					.ofAny(v, params);
		}
	},
	IPADDRESS("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", "ipAddress") {
		public IPAddress ofAny(Object v, Object... params) {
			return IPAddress
					.ofAny(v, params);
		}
	},
	STRING("http://www.w3.org/2001/XMLSchema#string", "string") {
		public StringVal ofAny(Object v, Object... params) {
			return StringVal
					.ofAny(v, params);
		}
	},
	RFC822NAME("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", "rfc822Name") {
		public RFC822Name ofAny(Object v, Object... params) {
			return RFC822Name
					.ofAny(v, params);
		}
	},
	TIME("http://www.w3.org/2001/XMLSchema#time", "time") {
		public ISO8601Time ofAny(Object v, Object... params) {
			return ISO8601Time
					.ofAny(v, params);
		}
	},
	X500NAME("urn:oasis:names:tc:xacml:1.0:data-type:x500Name", "x500Name") {
		public X500Name ofAny(Object v, Object... params) {
			return X500Name
					.ofAny(v, params);
		}
	},
	XPATH("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression",
			"xpathExpression", "urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression",
			"urn:oasis:names:tc:xacml:2.0:data-type:xpath-expression") {
		public Path ofAny(Object v, Object... params) {
			return Path
					.xpath(v, params);
		}
	},
	JPATH("urn:oasis:names:tc:xacml:3.0:data-type:jsonPathExpression",
			"jsonPathExpression") {
		public Path ofAny(Object v, Object... params) {
			return Path
					.jpath(v, params);
		}
	},
	YEARMONTHDURATION("http://www.w3.org/2001/XMLSchema#yearMonthDuration",
			"yearMonthDuration", "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration") {
		public ISO8601YearMonthDuration ofAny(Object v, Object... params) {
			return ISO8601YearMonthDuration
					.ofAny(v, params);
		}
	},
	ENTITY("urn:oasis:names:tc:xacml:3.0:data-type:entity", "entity") {
		public Entity ofAny(Object v, Object... params) {
			return Entity
					.ofAny(v, params);
		}
	};


	private java.lang.String typeId;
	private java.lang.String shortTypeId;
	private Set<java.lang.String> aliases;
	private BagOfValuesType bagType;


	XacmlTypes(java.lang.String typeId, java.lang.String shortTypeId, java.lang.String... aliases) {
		this.typeId = typeId;
		this.shortTypeId = shortTypeId;
		this.bagType = new BagOfValuesType(this);
		this.aliases = (aliases == null) ? ImmutableSet.<java.lang.String>of() : ImmutableSet
				.<java.lang.String>builder()
				.add(shortTypeId)
				.add(aliases)
				.build();
	}

	/**
	 * Gets type via type identifier or alias
	 *
	 * @param typeId a type identifier, supported type identifiers:
	 * {@link java.lang.String}, {@link URI}, {@link URI}
	 * @return {@link Optional} with resolved type
	 */
	public static Optional<ValueType> getType(Object typeId){
		if(typeId instanceof ValueType){
			return Optional.of((ValueType)typeId);
		}
		if(typeId instanceof java.lang.String){
			return systemTypes.forType(typeId.toString());
		}
		if(typeId instanceof URI){
			return systemTypes.forType(typeId.toString());
		}
		if(typeId instanceof Value){
			Value a = (Value)typeId;
			if(a.getEvaluatesTo().equals(XacmlTypes.STRING) || a.getEvaluatesTo().equals(ANYURI)){
				return systemTypes.forType(a.get().toString());
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
	public final java.lang.String getTypeId() {
		return typeId;
	}

	@Override
	public final java.lang.String getShortTypeId() {
		return shortTypeId;
	}

	public Set<java.lang.String> getTypeIdAliases(){
		return aliases;
	}

	@Override
	public java.lang.String toString(){
		return shortTypeId;
	}

	public ValueType getValueType(){
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
