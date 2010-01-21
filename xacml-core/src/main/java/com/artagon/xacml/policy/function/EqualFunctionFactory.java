package com.artagon.xacml.policy.function;

import java.util.List;

import com.artagon.xacml.FunctionId;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.ExplicitFunctionSpecBuilder;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.RegularFunction;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.policy.type.AnyURIType;
import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;

public class EqualFunctionFactory extends BaseFunctionFacatory
{
	public enum Functions implements FunctionId
	{
		INTEGER_EQUAL("urn:oasis:names:tc:xacml:1.0:function:integer-equal"),
		ANYURI_EQUAL("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal"),
		BOOLEAN_EQUAL("urn:oasis:names:tc:xacml:1.0:function:boolean-equal"),
		DOUBLE_EQUAL("urn:oasis:names:tc:xacml:1.0:function:double-equal"),
		X500NAME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:x500Name-equal"),
		STRING_IGNORECASE_EQUAL("urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case"),
		STRING_EQUAL("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
		DATE_EQUAL("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
		TIME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:time-equal"),
		DATETIME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
		DAYTIMEDURATION_EQUAL("urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal"),
		YEARMONTHDURATION_EQUAL("urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal"),	 
		RFC833NAME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal"),
		HEXBINARY_EQUAL("urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal"),
		BASE64BINARY_EQUAL("urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal");
		
		private String id;
		
		private Functions(String id){
			this.id = id;
		}
		
		public String toString(){
			return id;
		}
	}
	
	public EqualFunctionFactory()
	{
		add(build(Functions.ANYURI_EQUAL, DataTypes.ANYURI.getType()));
		add(build(Functions.BOOLEAN_EQUAL, DataTypes.BOOLEAN.getType()));
		add(build(Functions.INTEGER_EQUAL, DataTypes.INTEGER.getType()));
		add(build(Functions.DOUBLE_EQUAL, DataTypes.DOUBLE.getType()));
		add(build(Functions.STRING_EQUAL, DataTypes.STRING.getType()));
		add(build(Functions.RFC833NAME_EQUAL, DataTypes.RFC822NAME.getType()));
		add(build(Functions.HEXBINARY_EQUAL, DataTypes.HEXBINARY.getType()));
		add(build(Functions.BASE64BINARY_EQUAL, DataTypes.BASE64BINARY.getType()));
		add(build(Functions.DATE_EQUAL, DataTypes.DATE.getType()));
		add(build(Functions.DATETIME_EQUAL, DataTypes.DATETIME.getType()));
		add(build(Functions.TIME_EQUAL, DataTypes.TIME.getType()));
		add(build(Functions.DAYTIMEDURATION_EQUAL, DataTypes.DAYTIMEDURATION.getType()));
		add(build(Functions.YEARMONTHDURATION_EQUAL, DataTypes.YEARMONTHDURATION.getType()));
		add(build(Functions.X500NAME_EQUAL, DataTypes.X500NAME.getType()));
	}
	
	private FunctionSpec build(FunctionId functionId, AttributeDataType type)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId);

		builder.withParam(type).withParam(type);
		return builder.build(new RegularFunction()
		{
			@Override
			public Value invoke(EvaluationContext context, List<Expression> exp)
					throws PolicyEvaluationException {
				return DataTypes.BOOLEAN.create(exp.get(0).equals(exp.get(1)));
			}
			
			@Override
			public ValueType getReturnType() {
				return DataTypes.BOOLEAN.getType();
			}
		});
	}
	
	@XacmlFunction(id="aaaaa")
	public static BooleanValue eq(AnyURIType.AnyURIValue a, AnyURIType.AnyURIValue b )
	{	
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
}
