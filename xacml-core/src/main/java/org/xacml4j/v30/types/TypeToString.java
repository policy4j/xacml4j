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

import java.util.*;

import org.xacml4j.v30.TypeCapability;


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
	java.lang.String toString(Value v);

	/**
	 * Converts given string value to a {@link Value}
	 * of given {@link #getType()}
	 *
	 * @param v an attribute string value
	 * @return {@linl AttributeValue}
	 */
	Value fromString(java.lang.String v);

	static Optional<TypeToString> forType(java.lang.String typeId){
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
			public AnyURI fromString(java.lang.String v) {
				return AnyURI.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				AnyURI anyURI = (AnyURI)v;
				return anyURI.toXacmlString();
			}
		},
		BOOLEAN(XacmlTypes.BOOLEAN){
			@Override
			public BooleanVal fromString(java.lang.String v) {
				return BooleanVal.ofAny(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				BooleanVal booleanVal = (BooleanVal)v;
				return booleanVal.toXacmlString();
			}
		},
		BASE64BINARY(XacmlTypes.BASE64BINARY){
			@Override
			public Base64Binary fromString(java.lang.String v) {
				return Base64Binary.ofAny(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				Base64Binary binary = (Base64Binary)v;
				return binary.toXacmlString();
			}
		},
		DATE(XacmlTypes.DATE){
			@Override
			public ISO8601Date fromString(java.lang.String v) {
				return ISO8601Date.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				ISO8601Date d = (ISO8601Date)v;
				return d.toXacmlString();
			}
		},
		DATETIME(XacmlTypes.DATETIME){
			@Override
			public ISO8601DateTime fromString(java.lang.String v) {
				return ISO8601DateTime.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				ISO8601DateTime dateTime = (ISO8601DateTime)v;
				return dateTime.toXacmlString();
			}
		},
		DAYTIMEDURATION(XacmlTypes.DAYTIMEDURATION){
			@Override
			public ISO8601DayTimeDuration fromString(java.lang.String v) {
				return ISO8601DayTimeDuration.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				ISO8601DayTimeDuration duration = (ISO8601DayTimeDuration)v;
				return duration.toXacmlString();
			}
		},
		DNSNAME(XacmlTypes.DNSNAME){
			@Override
			public DNSName fromString(java.lang.String v) {
				return DNSName.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				DNSName name = (DNSName)v;
				return name.toXacmlString();
			}
		},
		DOUBLE(XacmlTypes.DOUBLE){
			@Override
			public DoubleVal fromString(java.lang.String v){
				Objects.requireNonNull(v);
				return DoubleVal.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				Objects.requireNonNull(v);
				DoubleVal d = (DoubleVal)v;
				return d.get().toString();
			}
		},
		HEXBINARY(XacmlTypes.HEXBINARY){
			@Override
			public HexBinary fromString(java.lang.String v) {
				return HexBinary.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				HexBinary d = (HexBinary)v;
				return d.get().toHexEncoded();
			}
		},
		INTEGER(XacmlTypes.INTEGER){
			@Override
			public IntegerVal fromString(java.lang.String v) {
				Objects.requireNonNull(v);
				return IntegerVal.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				Objects.requireNonNull(v);
				IntegerVal d = (IntegerVal)v;
				return d.toString();
			}
		},
		IPADDRESS(XacmlTypes.IPADDRESS){
			@Override
			public IPAddress fromString(java.lang.String v) {
				Objects.requireNonNull(v);
				return IPAddress.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				Objects.requireNonNull(v);
				IPAddress d = (IPAddress)v;
				return d.toXacmlString();
			}
		},
		RFC822NAME(XacmlTypes.RFC822NAME){
			@Override
			public RFC822Name fromString(java.lang.String v) {
				return RFC822Name.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				RFC822Name d = (RFC822Name)v;
				return d.toXacmlString();
			}
		},
		STRING(XacmlTypes.STRING){
			@Override
			public StringVal fromString(java.lang.String v) {
				return StringVal.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				Objects.requireNonNull(v);
				StringVal d = (StringVal) v;
				return d.get();
			}
		},
		TIME(XacmlTypes.TIME){
			@Override
			public ISO8601Time fromString(java.lang.String v) {
				return ISO8601Time.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				ISO8601Time d = (ISO8601Time)v;
				return d.toXacmlString();
			}
		},
		X500NAME(XacmlTypes.X500NAME){
			@Override
			public X500Name fromString(java.lang.String v) {
				Objects.requireNonNull(v);
				return X500Name.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				Objects.requireNonNull(v);
				X500Name d = (X500Name)v;
				return d.toString();
			}
		},
		YEARMONTHDURATION(XacmlTypes.YEARMONTHDURATION){
			@Override
			public ISO8601YearMonthDuration fromString(java.lang.String v) {
				return ISO8601YearMonthDuration.of(v);
			}
			
			@Override
			public java.lang.String toString(Value v) {
				ISO8601YearMonthDuration d = (ISO8601YearMonthDuration)v;
				return d.toXacmlString();
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
