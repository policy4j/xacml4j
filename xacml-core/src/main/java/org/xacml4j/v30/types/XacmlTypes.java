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
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.BinaryValue;
import org.xacml4j.v30.DNSName;
import org.xacml4j.v30.Date;
import org.xacml4j.v30.DateTime;
import org.xacml4j.v30.DayTimeDuration;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.IPAddress;
import org.xacml4j.v30.RFC822Name;
import org.xacml4j.v30.Time;
import org.xacml4j.v30.XPathExpression;
import org.xacml4j.v30.YearMonthDuration;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Enumeration of XACML 3.0 data types
 *
 * @author Giedrius Trumpickas
 */
public enum XacmlTypes implements AttributeExpType
{
	ANYURI("http://www.w3.org/2001/XMLSchema#anyURI", "anyURI"){
		public AnyURIExp create(Object v){
			if(v instanceof String){
				return AnyURIExp.of((String)v);
			}
			if(v instanceof URL){
				return AnyURIExp.of((URL)v);
			}
			if(v instanceof StringExp){
				return AnyURIExp.of(v.toString());
			}
			return AnyURIExp.of((URI)v);
		}
	},
	BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary", "base64Binary"){
		public Base64BinaryExp create(Object v){
			if(v instanceof String){
				return Base64BinaryExp.of((String)v);
			}
			if(v instanceof byte[]){
				return Base64BinaryExp.of((byte[])v);
			}
			if(v instanceof StringExp){
				return Base64BinaryExp.of(v.toString());
			}
			return Base64BinaryExp.of((BinaryValue)v);
		}
	},
	BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean", "boolean"){
		public BooleanExp create(Object v){
			if(v instanceof String){
				return BooleanExp.of((String)v);
			}
			if(v instanceof StringExp){
				return BooleanExp.of(((StringExp)v).getValue());
			}
			return BooleanExp.valueOf((Boolean)v);
		}
	},
	DATE("http://www.w3.org/2001/XMLSchema#date", "date"){
		public DateExp create(Object v){
			if(v instanceof String){
				return DateExp.of((String)v);
			}
			if(v instanceof StringExp){
				return DateExp.of(((StringExp)v).getValue());
			}
			if(v instanceof Calendar){
				return DateExp.of((Calendar)v);
			}
			if(v instanceof XMLGregorianCalendar){
				return DateExp.of(v.toString());
			}
			return DateExp.of((Date)v);
		}
	},
	DATETIME("http://www.w3.org/2001/XMLSchema#dateTime", "dateTime"){
		public DateTimeExp create(Object v){
			if(v instanceof String){
				return DateTimeExp.of((String)v);
			}
			if(v instanceof StringExp){
				return DateTimeExp.of(((StringExp)v).getValue());
			}
			if(v instanceof Calendar){
				return DateTimeExp.of((Calendar)v);
			}
			if(v instanceof XMLGregorianCalendar){
				return DateTimeExp.of(v.toString());
			}
			return DateTimeExp.of((DateTime)v);
		}
	},
	DAYTIMEDURATION("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "dayTimeDuration",
			"http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"){
		public DayTimeDurationExp create(Object v){
			if(v instanceof String){
				return DayTimeDurationExp.of((String)v);
			}
			if(v instanceof StringExp){
				return DayTimeDurationExp.of(((StringExp)v).getValue());
			}
			if(v instanceof XMLGregorianCalendar){
				return DayTimeDurationExp.of(v.toString());
			}
			return DayTimeDurationExp.of((DayTimeDuration)v);
		}
	},
	DNSNAME("urn:oasis:names:tc:xacml:2.0:data-type:dnsName",  "dnsName"){
		public DNSNameExp create(Object v){
			if(v instanceof String){
				return DNSNameExp.of((String)v);
			}
			if(v instanceof StringExp){
				return DNSNameExp.of(((StringExp)v).getValue());
			}
			return DNSNameExp.of((DNSName)v);
		}
	},
	DOUBLE("http://www.w3.org/2001/XMLSchema#double", "double"){
		public DoubleExp create(Object v){
			if(v instanceof String){
				return DoubleExp.of((String)v);
			}
			if(v instanceof StringExp){
				return DoubleExp.of(((StringExp)v).getValue());
			}
			return DoubleExp.of((Number)v);
		}
	},
	INTEGER("http://www.w3.org/2001/XMLSchema#integer", "integer"){
			public IntegerExp create(Object v){
				if(v instanceof String){
					return IntegerExp.of((String)v);
				}
				if(v instanceof StringExp){
					return IntegerExp.of(((StringExp)v).getValue());
				}
				return IntegerExp.of((Number)v);
			}
	},
	HEXBINARY("http://www.w3.org/2001/XMLSchema#hexBinary", "hexBinary"){
		public HexBinaryExp create(Object v){
			if(v instanceof String){
				return HexBinaryExp.of((String)v);
			}
			if(v instanceof StringExp){
				return HexBinaryExp.of(((StringExp)v).getValue());
			}
			if(v instanceof byte[]){
				return HexBinaryExp.of((byte[])v);
			}
			return HexBinaryExp.of((BinaryValue)v);
		}
	},
	IPADDRESS("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", "ipAddress"){
		public IPAddressExp create(Object v){
			if(v instanceof String){
				return IPAddressExp.of((String)v);
			}
			if(v instanceof StringExp){
				return IPAddressExp.of(((StringExp)v).getValue());
			}
			return IPAddressExp.of((IPAddress)v);
		}
	},
	STRING("http://www.w3.org/2001/XMLSchema#string", "string"){
		public StringExp create(Object v){
			if(v instanceof StringExp){
				return StringExp.of(((StringExp)v).getValue());
			}
			return StringExp.of((String)v);
		}
	},
	RFC822NAME("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", "rfc822Name"){
		public RFC822NameExp create(Object v){
			if(v instanceof String){
				return RFC822NameExp.of((String)v);
			}
			if(v instanceof StringExp){
				return RFC822NameExp.of(((StringExp)v).getValue());
			}
			return RFC822NameExp.of((RFC822Name)v);
		}
	},
	TIME("http://www.w3.org/2001/XMLSchema#time", "time"){
		public TimeExp create(Object v){
			if(v instanceof String){
				return TimeExp.of((String)v);
			}
			if(v instanceof StringExp){
				return TimeExp.of(((StringExp)v).getValue());
			}
			if(v instanceof XMLGregorianCalendar){
				return TimeExp.of((XMLGregorianCalendar)v);
			}
			if(v instanceof Calendar){
				return TimeExp.of((Calendar)v);
			}
			return TimeExp.of((Time)v);
		}
	},
	X500NAME("urn:oasis:names:tc:xacml:1.0:data-type:x500Name", "x500Name"){
		public X500NameExp create(Object v){
			if(v instanceof String){
				return X500NameExp.of((String)v);
			}
			if(v instanceof StringExp){
				return X500NameExp.of(((StringExp)v).getValue());
			}
			return X500NameExp.of((X500Principal)v);
		}
	},
	XPATH("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression",
		  "xpathExpression", "urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression",
		  "urn:oasis:names:tc:xacml:2.0:data-type:xpath-expression"){
		public XPathExp create(Object v){
			return XPathExp.of((XPathExpression)v);
		}
	},
	YEARMONTHDURATION("http://www.w3.org/2001/XMLSchema#yearMonthDuration",
			"yearMonthDuration", "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"){
			public YearMonthDurationExp create(Object v){
				if(v instanceof String){
					return YearMonthDurationExp.of((String)v);
				}
				if(v instanceof StringExp){
					return YearMonthDurationExp.of(((StringExp)v).getValue());
				}
				if(v instanceof Duration){
					return YearMonthDurationExp.of((Duration)v);
				}
				return YearMonthDurationExp.of((YearMonthDuration)v);
			}
	},
	ENTITY("urn:oasis:names:tc:xacml:3.0:data type:entity", "entity"){
			public EntityExp create(Object v){
				return EntityExp.of((Entity)v);
			}
	};


	private final static Map<String, AttributeExpType> TYPES_BY_ID;

	static{
		ImmutableMap.Builder<String, AttributeExpType> b = ImmutableMap.builder();
		for(AttributeExpType t : XacmlTypes.values()){
			b.put(t.getDataTypeId(), t);
			for(String a : t.getDataTypeIdAliases()){
				b.put(a, t);
			}
		}
		TYPES_BY_ID = b.build();
	}

	private String typeId;
	private String shortTypeId;
	private BagOfAttributeExpType bagType;
	private Set<String> aliases;

	private XacmlTypes(String typeId, String shortTypeId, String ...aliases){
		this.typeId = typeId;
		this.shortTypeId = shortTypeId;
		this.bagType = new BagOfAttributeExpType(this);
		this.aliases = (aliases == null)?ImmutableSet.<String>of():ImmutableSet
				.<String>builder()
				.add(shortTypeId)
				.add(aliases)
				.build();
	}

	/**
	 * Gets type via type identifier or alias
	 *
	 * @param typeId a type identifier
	 * @return {@link Optional} with resolved type
	 */
	public static Optional<AttributeExpType> getType(String typeId){
		return Optional.fromNullable(TYPES_BY_ID.get(typeId));
	}

	@Override
	public final String getDataTypeId() {
		return typeId;
	}

	@Override
	public final String getShortDataTypeId() {
		return shortTypeId;
	}

	public Set<String> getDataTypeIdAliases(){
		return aliases;
	}

	@Override
	public final BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
	}

	@Override
	public final BagOfAttributeExpType bagType() {
		return bagType;
	}

	@Override
	public final BagOfAttributeExp bagOf(AttributeExp... attrs) {
		return bagType.create(attrs);
	}

	@Override
	public final BagOfAttributeExp bagOf(Iterable<AttributeExp> values) {
		return bagType.create(values);
	}

	@Override
	public final BagOfAttributeExp emptyBag() {
		return bagType.createEmpty();
	}

	@Override
	public String toString(){
		return typeId;
	}

	@Override
	public boolean isBag() {
		return false;
	}

	public AttributeExpType getDataType(){
		return this;
	}
}
