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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.xacml4j.v30.TypeCapability;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;


/**
 * A type to/from string capability. Provides a strategy
 * to convert XACML type values to from string representation
 *
 * @author Giedrius Trumpickas
 */
public interface TypeToString extends TypeCapability
{
	/**
	 * Converts given attribute value to string
	 *
	 * @param v an attribute value
	 * @return string representation of the given value
	 */
	String toString(Value v);

	/**
	 * Converts given string value to a {@link Value}
	 * of given {@link #getType()}
	 *
	 * @param v an attribute string value
	 * @return {@linl AttributeValue}
	 */
	Value fromString(String v);

	static Optional<TypeToString> forType(String typeId){
		return XacmlTypes.getType(typeId)
		                 .flatMap(t->forType(t));
	}

	static Optional<TypeToString> forType(ValueType type)
	{
		return Optional.ofNullable(type).map(t->Types.capabilities.get(t));
	}

	/**
	 * Factory for {@code TypeToString} extensions
	 */
	class SystemFactory extends AbstractCapabilityFactory<TypeToString>
			implements TypeToStringFactory
	{
		protected SystemFactory() {
			super(Arrays.asList(TypeToString.Types.values()),
			      TypeToString.class);
		}
	}

	enum Types implements TypeToString
	{
		ANYURI(XacmlTypes.ANYURI){
			@Override
			public AnyURIValue fromString(String v) {
				Objects.requireNonNull(v);
				return XacmlTypes.ANYURI.of(v);
			}
			
			@Override
			public String toString(Value v) {
				AnyURIValue anyUri = (AnyURIValue)v;
				return anyUri.value().toString();
			}
		},
		BOOLEAN(XacmlTypes.BOOLEAN){
			@Override
			public BooleanValue fromString(String v) {
				Objects.requireNonNull(v);
				return XacmlTypes.BOOLEAN.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				BooleanValue boolVal = (BooleanValue)v;
				return boolVal.value().toString();
			}
		},
		BASE64BINARY(XacmlTypes.BASE64BINARY){
			@Override
			public Base64BinaryValue fromString(String v) {
				Objects.requireNonNull(v);
				return XacmlTypes.BASE64BINARY.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				Base64BinaryValue base64Value = (Base64BinaryValue)v;
				return base64Value.value().toBase64Encoded();
			}
		},
		DATE(XacmlTypes.DATE){
			@Override
			public DateValue fromString(String v) {
				Objects.requireNonNull(v);
				return XacmlTypes.DATE.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				DateValue d = (DateValue)v;
				return d.value().toXacmlString();
			}
		},
		DATETIME(XacmlTypes.DATETIME){
			@Override
			public DateTimeValue fromString(String v) {
				Objects.requireNonNull(v);
				return XacmlTypes.DATETIME.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				DateTimeValue d = (DateTimeValue)v;
				return d.value().toXacmlString();
			}
		},
		DAYTIMEDURATION(XacmlTypes.DAYTIMEDURATION){
			@Override
			public DayTimeDurationValue fromString(String v) {
				Objects.requireNonNull(v);
				return DayTimeDurationValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				DayTimeDurationValue d = (DayTimeDurationValue)v;
				return d.value().toXacmlString();
			}
		},
		DNSNAME(XacmlTypes.DNSNAME){
			@Override
			public DNSNameValue fromString(String v) {
				Objects.requireNonNull(v);
				return DNSNameValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				DNSNameValue d = (DNSNameValue)v;
				return d.value().toXacmlString();
			}
		},
		DOUBLE(XacmlTypes.DOUBLE){
			@Override
			public DoubleValue fromString(String v){
				Objects.requireNonNull(v);
				return DoubleValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				DoubleValue d = (DoubleValue)v;
				return d.value().toString();
			}
		},
		HEXBINARY(XacmlTypes.HEXBINARY){
			@Override
			public HexBinaryValue fromString(String v) {
				Objects.requireNonNull(v);
				return HexBinaryValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				HexBinaryValue d = (HexBinaryValue)v;
				return d.value().toHexEncoded();
			}
		},
		INTEGER(XacmlTypes.INTEGER){
			@Override
			public IntegerValue fromString(String v) {
				Objects.requireNonNull(v);
				return IntegerValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				IntegerValue d = (IntegerValue)v;
				return d.value().toString();
			}
		},
		IPADDRESS(XacmlTypes.IPADDRESS){
			@Override
			public IPAddressValue fromString(String v) {
				Objects.requireNonNull(v);
				return IPAddressValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				IPAddressValue d = (IPAddressValue)v;
				return d.value().toXacmlString();
			}
		},
		RFC822NAME(XacmlTypes.RFC822NAME){
			@Override
			public RFC822NameValue fromString(String v) {
				Objects.requireNonNull(v);
				return RFC822NameValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				RFC822NameValue d = (RFC822NameValue)v;
				return d.value().toXacmlString();
			}
		},
		STRING(XacmlTypes.STRING){
			@Override
			public StringValue fromString(String v) {
				Objects.requireNonNull(v);
				return StringValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				StringValue d = (StringValue)v;
				return d.value();
			}
		},
		TIME(XacmlTypes.TIME){
			@Override
			public TimeValue fromString(String v) {
				Objects.requireNonNull(v);
				return TimeValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				TimeValue d = (TimeValue)v;
				return d.value().toXacmlString();
			}
		},
		X500NAME(XacmlTypes.X500NAME){
			@Override
			public X500NameValue fromString(String v) {
				Objects.requireNonNull(v);
				return X500NameValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				X500NameValue d = (X500NameValue)v;
				return d.value().toString();
			}
		},
		YEARMONTHDURATION(XacmlTypes.YEARMONTHDURATION){
			@Override
			public YearMonthDurationValue fromString(String v) {
				return YearMonthDurationValue.of(v);
			}
			
			@Override
			public String toString(Value v) {
				Objects.requireNonNull(v);
				YearMonthDurationValue d = (YearMonthDurationValue)v;
				return d.value().toXacmlString();
			}
		};

		
		private ValueType type;
		
		Types(ValueType type){
			this.type = type;	
		}

		public ValueType getType(){
			return type;
		}

		private final static Map<ValueType, TypeToString> capabilities = TypeCapability.discoverCapabilities(new SystemFactory(),
		                                                                                TypeToString.class,
		                                                                                TypeToStringFactory.class);

	}
}
