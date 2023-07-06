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


import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.DNSName;
import org.xacml4j.v30.DateTime;
import org.xacml4j.v30.PortRange;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.TypeCapability;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.types.AnyURIValue;
import org.xacml4j.v30.types.Base64BinaryValue;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.DNSNameValue;
import org.xacml4j.v30.types.DateTimeValue;
import org.xacml4j.v30.types.DoubleValue;
import org.xacml4j.v30.types.HexBinaryValue;
import org.xacml4j.v30.types.IntegerValue;
import org.xacml4j.v30.types.StringValue;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
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
				Any bytes = Any.pack(com.google.protobuf.StringValue.of(value.get()));
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
		DATETIME(XacmlTypes.DATETIME, PXacmlTypes.DATETIME) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				DateTime value = ((DateTimeValue) v).get();
				Any bytes = Any.pack(com.google.protobuf.StringValue.of(value.toString()));
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
		INTEGER(XacmlTypes.INTEGER, PXacmlTypes.INTEGER) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				IntegerValue value = (IntegerValue) v;
				Any bytes = Any.pack(Int64Value.of(value.get()));
				return PValue.newBuilder()
				             .setType(getProtoType())
				             .setValue(bytes)
				             .build();
			}

			@Override
			public Value fromProto(PValue v) {
				assertValueFromProto(v, this.getType(), v.getType());
				try {
					Int64Value value = v.getValue().unpack(Int64Value.class);
					return getType().of(value.getValue());
				} catch (InvalidProtocolBufferException e) {
					throw SyntaxException
							.invalidValueConversionToXacml(v,
							                               v.getType(),
							                               this.getType(), e);
				}
			}
		},
		DNSNAME(XacmlTypes.DNSNAME, PXacmlTypes.DNSNAME) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				DNSName dnsName = ((DNSNameValue)v).get();
				PDNSNameValue.Builder pValueBuilder = PDNSNameValue.newBuilder()
						.setHostname(dnsName.getDomainName());
				if(!dnsName.getPortRange().isUnbound()){
					pValueBuilder.setPortRange(PPortRange.newBuilder()
					          .setLowerPort(dnsName.getPortRange().getLowerBound())
					          .setUpperPort(dnsName.getPortRange().getUpperBound())
					          .build());
				}
				return PValue.newBuilder()
				             .setType(getProtoType())
				             .setValue(Any.pack(pValueBuilder.build()))
				             .build();
			}

			@Override
			public Value fromProto(PValue v) {
				assertValueFromProto(v, this.getType(), v.getType());
				try {
					PDNSNameValue value = v.getValue().unpack(PDNSNameValue.class);
					return getType().of(new DNSName(value.getHostname(),
					                                Optional
							                                .ofNullable(value.getPortRange())
							                                .map(r->PortRange.getRange(r.getLowerPort(),
							                                                           r.getUpperPort()))
							                                .orElse(PortRange.getAnyPort())));
				} catch (InvalidProtocolBufferException e) {
					throw SyntaxException
							.invalidValueConversionToXacml(v,
							                               v.getType(),
							                               this.getType(), e);
				}
			}
		},
		DOUBLE(XacmlTypes.DOUBLE, PXacmlTypes.DOUBLE) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				DoubleValue value = (DoubleValue) v;
				Any bytes = Any.pack(com.google.protobuf.DoubleValue.of(value.get()));
				return PValue.newBuilder()
				             .setType(getProtoType())
				             .setValue(bytes)
				             .build();
			}

			@Override
			public Value fromProto(PValue v) {
				assertValueFromProto(v, this.getType(), v.getType());
				try {
					com.google.protobuf.DoubleValue value = v.getValue().unpack(com.google.protobuf.DoubleValue.class);
					return getType().of(value.getValue());
				} catch (InvalidProtocolBufferException e) {
					throw SyntaxException
							.invalidValueConversionToXacml(v,
							                               v.getType(),
							                               this.getType(), e);
				}
			}
		},
		BOOLEAN(XacmlTypes.BOOLEAN, PXacmlTypes.BOOLEAN) {
			@Override
			public PValue toProto(Value v) {
				assertValueToProto(v, this.getType(), this.getProtoType());
				BooleanValue value = (BooleanValue) v;
				Any bytes = Any.pack(com.google.protobuf.BoolValue.of(value.get()));
				return PValue.newBuilder()
				             .setType(getProtoType())
				             .setValue(bytes)
				             .build();
			}

			@Override
			public Value fromProto(PValue v) {
				assertValueFromProto(v, this.getType(), v.getType());
				try {
					com.google.protobuf.BoolValue value = v.getValue().unpack(com.google.protobuf.BoolValue.class);
					return getType().of(value.getValue());
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
