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

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;

import com.google.common.base.Preconditions;

public interface TypeToString extends TypeCapability 
{
	String toString(AttributeExp exp);
	AttributeExp fromString(String v);
	
	public enum Types implements TypeToString
	{
		ANYURI(XacmlTypes.ANYURI){
			@Override
			public AnyURIExp fromString(String v) {
				return AnyURIExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				AnyURIExp anyUri = (AnyURIExp)v;
				return anyUri.getValue().toString();
			}
		},
		BOOLEAN(XacmlTypes.BOOLEAN){
			@Override
			public BooleanExp fromString(String v) {
				return BooleanExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				Preconditions.checkNotNull(v);
				BooleanExp boolVal = (BooleanExp)v;
				return boolVal.getValue().toString();
			}
		},
		BASE64BINARY(XacmlTypes.BASE64BINARY){
			@Override
			public Base64BinaryExp fromString(String v) {
				Preconditions.checkNotNull(v);
				return Base64BinaryExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				Preconditions.checkNotNull(v);
				Base64BinaryExp base64Value = (Base64BinaryExp)v;
				return base64Value.getValue().toBase64Encoded();
			}
		},
		DATE(XacmlTypes.DATE){
			@Override
			public DateExp fromString(String v) {
				return DateExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				DateExp d = (DateExp)v;
				return d.getValue().toXacmlString();
			}
		},
		DATETIME(XacmlTypes.DATETIME){
			@Override
			public DateTimeExp fromString(String v) {
				return DateTimeExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				DateTimeExp d = (DateTimeExp)v;
				return d.getValue().toXacmlString();
			}
		},
		DAYTIMEDURATION(XacmlTypes.DAYTIMEDURATION){
			@Override
			public DayTimeDurationExp fromString(String v) {
				return DayTimeDurationExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				DayTimeDurationExp d = (DayTimeDurationExp)v;
				return d.getValue().toXacmlString();
			}
		},
		DNSNAME(XacmlTypes.DNSNAME){
			@Override
			public DNSNameExp fromString(String v) {
				return DNSNameExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				DNSNameExp d = (DNSNameExp)v;
				return d.getValue().toXacmlString();
			}
		},
		DOUBLE(XacmlTypes.DOUBLE){
			@Override
			public DoubleExp fromString(String v) {
				return DoubleExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				DoubleExp d = (DoubleExp)v;
				return d.getValue().toString();
			}
		},
		HEXBINARY(XacmlTypes.HEXBINARY){
			@Override
			public HexBinaryExp fromString(String v) {
				return HexBinaryExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				HexBinaryExp d = (HexBinaryExp)v;
				return d.getValue().toHexEncoded();
			}
		},
		INTEGER(XacmlTypes.INTEGER){
			@Override
			public IntegerExp fromString(String v) {
				return IntegerExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				IntegerExp d = (IntegerExp)v;
				return d.getValue().toString();
			}
		},
		IPADDRESS(XacmlTypes.IPADDRESS){
			@Override
			public IPAddressExp fromString(String v) {
				return IPAddressExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				IPAddressExp d = (IPAddressExp)v;
				return d.getValue().toXacmlString();
			}
		},
		RFC822NAME(XacmlTypes.RFC822NAME){
			@Override
			public RFC822NameExp fromString(String v) {
				return RFC822NameExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				RFC822NameExp d = (RFC822NameExp)v;
				return d.getValue().toXacmlString();
			}
		},
		STRING(XacmlTypes.STRING){
			@Override
			public StringExp fromString(String v) {
				return StringExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				StringExp d = (StringExp)v;
				return d.getValue();
			}
		},
		TIME(XacmlTypes.TIME){
			@Override
			public TimeExp fromString(String v) {
				return TimeExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				TimeExp d = (TimeExp)v;
				return d.getValue().toXacmlString();
			}
		},
		X500NAME(XacmlTypes.X500NAME){
			@Override
			public X500NameExp fromString(String v) {
				return X500NameExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				X500NameExp d = (X500NameExp)v;
				return d.getValue().toString();
			}
		},
		YEARMONTHDURATION(XacmlTypes.YEARMONTHDURATION){
			@Override
			public YearMonthDurationExp fromString(String v) {
				return YearMonthDurationExp.valueOf(v);
			}
			
			@Override
			public String toString(AttributeExp v) {
				YearMonthDurationExp d = (YearMonthDurationExp)v;
				return d.getValue().toXacmlString();
			}
		};
		
		private final static TypeCapability.Index<TypeToString> INDEX = TypeCapability.Index.<TypeToString>build(values());
		
		private AttributeExpType type;
		
		private Types(AttributeExpType type){
			this.type = type;	
		}
		
		public AttributeExpType getType(){
			return type;
		}
		
		public static TypeCapability.Index<TypeToString> getIndex(){
			return INDEX;
		}
	}
}
