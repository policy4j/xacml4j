package com.xacml4j.xacml.proto;

/*
 * #%L
 * Xacml4J Google Protbuf 3 based request/response and policy format support
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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


import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.TypeCapability;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.types.AnyURIValue;
import org.xacml4j.v30.types.Base64BinaryValue;
import org.xacml4j.v30.types.HexBinaryValue;
import org.xacml4j.v30.types.StringValue;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;

public interface TypeToProto extends TypeCapability {
	Logger LOG = LoggerFactory.getLogger(TypeToProto.class);

	PXacmlTypes getProtoType();

	PValue toProto(Value v);

	Value fromProto(PValue v);


	enum Types implements TypeToProto {
		ANYURI(XacmlTypes.ANYURI, PXacmlTypes.ANYURI) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				AnyURIValue value = (AnyURIValue) v;
				Any bytes = Any.pack(com.google.protobuf.StringValue.of(value.get().toString()));
				return PValue.newBuilder()
				             .setType(getProtoType())
				             .setValue(bytes)
				             .build();
			}

			@Override
			public Value fromProto(PValue v) {
				assertValueFromProto(v, this.getType(), v.getType());
				try {
					com.google.protobuf.StringValue bytes = v.getValue().unpack(com.google.protobuf.StringValue.class);
					return getType().of(bytes.getValue());
				} catch (InvalidProtocolBufferException e) {
					throw SyntaxException
							.invalidValueConversionToXacml(v,
							                               v.getType(),
							                               this.getType(), e);
				}
			}
		},
		STRING(XacmlTypes.STRING, PXacmlTypes.STRING) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				StringValue value = (StringValue) v;
				Any bytes = Any.pack(com.google.protobuf.StringValue.of(value.get().toString()));
				return PValue.newBuilder()
				             .setType(getProtoType())
				             .setValue(bytes)
				             .build();
			}

			@Override
			public Value fromProto(PValue v) {
				assertValueFromProto(v, this.getType(), v.getType());
				try {
					com.google.protobuf.StringValue bytes = v.getValue().unpack(com.google.protobuf.StringValue.class);
					return getType().of(bytes.getValue());
				} catch (InvalidProtocolBufferException e) {
					throw SyntaxException
							.invalidValueConversionToXacml(v,
							                               v.getType(),
							                               this.getType(), e);
				}
			}
		},
		BASE64BINARY(XacmlTypes.BASE64BINARY, PXacmlTypes.BASE64BINARY) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				Base64BinaryValue value = (Base64BinaryValue) v;
				Any bytes = Any.pack(BytesValue.newBuilder()
				                               .setValue(ByteString.copyFrom(value.get().asByteBuffer()))
				                               .build());
				return PValue.newBuilder()
				             .setType(getProtoType())
				             .setValue(bytes)
				             .build();
			}

			@Override
			public Value fromProto(PValue v) {
				assertValueFromProto(v, this.getType(), v.getType());
				try {
					BytesValue bytes = v.getValue().unpack(BytesValue.class);
					return getType().of(bytes.getValue().toByteArray());
				} catch (InvalidProtocolBufferException e) {
					throw SyntaxException
							.invalidValueConversionToXacml(v,
							                               v.getType(),
							                               this.getType(), e);
				}
			}
		},
		HEXBINARY(XacmlTypes.HEXBINARY, PXacmlTypes.HEXBINARY) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				HexBinaryValue value = (HexBinaryValue) v;
				Any bytes = Any.pack(BytesValue.newBuilder()
				                               .setValue(ByteString.copyFrom(value.get().asByteBuffer()))
				                               .build());
				return PValue.newBuilder()
				             .setType(getProtoType())
				             .setValue(bytes)
				             .build();

			}

			@Override
			public Value fromProto(PValue v) {
				assertValueFromProto(v, this.getType(), v.getType());
				try {
					BytesValue bytes = v.getValue().unpack(BytesValue.class);
					return getType().of(bytes.getValue().toByteArray());
				} catch (InvalidProtocolBufferException e) {
					throw SyntaxException
							.invalidValueConversionToXacml(v,
														v.getType(),
							                            this.getType(), e);
				}
			}
		};

		private ValueType type;
		private PXacmlTypes pype;

		Types(ValueType valueType, PXacmlTypes protoType) {
			this.type = valueType;
			this.pype = protoType;
		}

		public ValueType getType() {
			return type;
		}
		public PXacmlTypes getProtoType() {
			return pype;
		}
	}

	static void assertValueFromProto(PValue v, ValueType toType, PXacmlTypes pType) {
		if (v == null || !v.hasValue() || !v.getType().equals(pType)) {
			throw SyntaxException.invalidValueConversionToXacml(v, pType.name(), toType);
		}
	}

	static void assertValueToProto(Value v, ValueType expectedXacmlType, PXacmlTypes toType) {
		if (!v.getEvaluatesTo().equals(expectedXacmlType)) {
			throw SyntaxException.invalidValueConversionFromXacml(v, expectedXacmlType, toType.name());
		}
	}

}
