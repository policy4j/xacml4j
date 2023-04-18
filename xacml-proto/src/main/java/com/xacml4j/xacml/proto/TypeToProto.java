package com.xacml4j.xacml.proto;


import java.net.URI;

import org.xacml4j.v30.TypeCapability;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.types.AnyURIValue;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;

public interface TypeToProto extends TypeCapability
{
	PValue toProto(Value v);

	Value fromProto(PValue v);


	enum Types implements TypeToProto {
		ANYURI(XacmlTypes.ANYURI) {
			@Override
			public PValue toProto(Value v) {
				AnyURIValue value = (AnyURIValue)v;
				return PValue.newBuilder()
				             .setType(com.xacml4j.xacml.proto.XacmlTypes.ANYURI)
				             .setValue(Any.newBuilder()
				                          .setValue(ByteString
						                                    .copyFromUtf8(value.value().toString())))
				             .build();
			}

			@Override
			public Value fromProto(PValue v) {
				return null;
			}
		};

		private ValueType type;
		Types(ValueType valueType){
			this.type = valueType;
		}

		public ValueType getType(){
			return type;
		}
	}

}
