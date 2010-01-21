package com.artagon.xacml.policy.function;

import java.util.List;

import com.artagon.xacml.FunctionId;
import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.Attribute;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.ExplicitFunctionSpecBuilder;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.RegularFunction;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.StringType;

public class ToFromStringFunctionFactory extends BaseFunctionFacatory
{

	public ToFromStringFunctionFactory() 
	{
		add(buildToString(Functions.STRING_FROM_ANYURI, DataTypes.ANYURI.getType()));
		add(buildToString(Functions.STRING_FROM_BOOLEAN, DataTypes.BOOLEAN.getType()));
		add(buildToString(Functions.STRING_FROM_INTEGER, DataTypes.INTEGER.getType()));
		add(buildToString(Functions.STRING_FROM_DOUBLE, DataTypes.DOUBLE.getType()));
		add(buildToString(Functions.STRING_FROM_DATE, DataTypes.DATE.getType()));
		add(buildToString(Functions.STRING_FROM_DATETIME, DataTypes.DATETIME.getType()));
		add(buildToString(Functions.STRING_FROM_TIME, DataTypes.TIME.getType()));
		add(buildToString(Functions.STRING_FROM_DAYTIMEDURATION, DataTypes.DAYTIMEDURATION.getType()));
		add(buildToString(Functions.STRING_FROM_YEARMONTHDURATION, DataTypes.YEARMONTHDURATION.getType()));
		add(buildToString(Functions.STRING_FROM_DNSNAME, DataTypes.DNSNAME.getType()));
		add(buildToString(Functions.STRING_FROM_IPADDRESS, DataTypes.IPADDRESS.getType()));
		
		add(buildFromString(Functions.ANYURI_FROM_STRING, DataTypes.ANYURI.getType()));
		add(buildFromString(Functions.BOOLEAN_FROM_STRING, DataTypes.BOOLEAN.getType()));
		add(buildFromString(Functions.INTEGER_FROM_STRING, DataTypes.INTEGER.getType()));
		add(buildFromString(Functions.DOUBLE_FROM_STRING, DataTypes.DOUBLE.getType()));
		add(buildFromString(Functions.DATE_FROM_STRING, DataTypes.DATE.getType()));
		add(buildFromString(Functions.DATETIME_FROM_STRING, DataTypes.DATETIME.getType()));
		add(buildFromString(Functions.DAYTIMEDURATION_FROM_STRING, DataTypes.DAYTIMEDURATION.getType()));
		add(buildFromString(Functions.YEARMONTHDURATION_FROM_STRING, DataTypes.YEARMONTHDURATION.getType()));
		add(buildFromString(Functions.TIME_FROM_STRING, DataTypes.TIME.getType()));
		add(buildFromString(Functions.DNSNAME_FROM_STRING, DataTypes.DNSNAME.getType()));
		add(buildFromString(Functions.IPADDRESS_FROM_STRING, DataTypes.IPADDRESS.getType()));
	}
	
	private FunctionSpec buildToString(FunctionId functionId, final AttributeDataType type)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId);
		builder.withParam(type);
		return builder.build(new RegularFunction() {
			@Override
			public ValueType getReturnType() {
				return DataTypes.STRING.getType();
			}
		
			public Value invoke(EvaluationContext context, List<Expression> exp)
					throws PolicyEvaluationException {
				Attribute v = (Attribute)exp.get(0);
				return DataTypes.STRING.create(v.toXacmlString());
			}
		});
	}
	
	private FunctionSpec buildFromString(FunctionId functionId, final AttributeDataType returnType)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId);
		builder.withParam(DataTypes.STRING.getType());
		return builder.build(new RegularFunction() {
			
			@Override
			public Value invoke(EvaluationContext context, List<Expression> parameters)
					throws PolicyEvaluationException {
				StringType.StringValue v = (StringType.StringValue)parameters.get(0);
				return returnType.create(v.getValue());
			}
			
			@Override
			public ValueType getReturnType() {
				return returnType;
			}
		}); 
	}
	
}
