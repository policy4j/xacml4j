package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.Attribute;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.StringType;

public class ToFromStringFunctionFactory extends BaseFunctionFacatory
{

	public ToFromStringFunctionFactory() 
	{
		add(buildToString(XacmlFunction.STRING_FROM_ANYURI, XacmlDataType.ANYURI.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_BOOLEAN, XacmlDataType.BOOLEAN.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_INTEGER, XacmlDataType.INTEGER.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_DOUBLE, XacmlDataType.DOUBLE.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_DATE, XacmlDataType.DATE.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_DATETIME, XacmlDataType.DATETIME.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_TIME, XacmlDataType.TIME.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_DAYTIMEDURATION, XacmlDataType.DAYTIMEDURATION.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_YEARMONTHDURATION, XacmlDataType.YEARMONTHDURATION.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_DNSNAME, XacmlDataType.DNSNAME.getType()));
		add(buildToString(XacmlFunction.STRING_FROM_IPADDRESS, XacmlDataType.IPADDRESS.getType()));
		
		add(buildFromString(XacmlFunction.ANYURI_FROM_STRING, XacmlDataType.ANYURI.getType()));
		add(buildFromString(XacmlFunction.BOOLEAN_FROM_STRING, XacmlDataType.BOOLEAN.getType()));
		add(buildFromString(XacmlFunction.INTEGER_FROM_STRING, XacmlDataType.INTEGER.getType()));
		add(buildFromString(XacmlFunction.DOUBLE_FROM_STRING, XacmlDataType.DOUBLE.getType()));
		add(buildFromString(XacmlFunction.DATE_FROM_STRING, XacmlDataType.DATE.getType()));
		add(buildFromString(XacmlFunction.DATETIME_FROM_STRING, XacmlDataType.DATETIME.getType()));
		add(buildFromString(XacmlFunction.DAYTIMEDURATION_FROM_STRING, XacmlDataType.DAYTIMEDURATION.getType()));
		add(buildFromString(XacmlFunction.YEARMONTHDURATION_FROM_STRING, XacmlDataType.YEARMONTHDURATION.getType()));
		add(buildFromString(XacmlFunction.TIME_FROM_STRING, XacmlDataType.TIME.getType()));
		add(buildFromString(XacmlFunction.DNSNAME_FROM_STRING, XacmlDataType.DNSNAME.getType()));
		add(buildFromString(XacmlFunction.IPADDRESS_FROM_STRING, XacmlDataType.IPADDRESS.getType()));
	}
	
	private FunctionSpec buildToString(XacmlFunction functionId, final AttributeDataType type)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId.getXacmlId());
		builder.withParam(type);
		return builder.build(new StaticallyTypedFunction() {
			@Override
			public ValueType getReturnType() {
				return XacmlDataType.STRING.getType();
			}
		
			public Value invoke(EvaluationContext context, Expression ...args)
					throws PolicyEvaluationException {
				Attribute v = (Attribute)args[0];
				return XacmlDataType.STRING.create(v.toXacmlString());
			}
		});
	}
	
	private FunctionSpec buildFromString(XacmlFunction functionId, final AttributeDataType returnType)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId.getXacmlId());
		builder.withParam(XacmlDataType.STRING.getType());
		return builder.build(new StaticallyTypedFunction() {
			
			@Override
			public Value invoke(EvaluationContext context, Expression ...args)
					throws PolicyEvaluationException {
				StringType.StringValue v = (StringType.StringValue)args[0];
				return returnType.create(v.getValue());
			}
			
			@Override
			public ValueType getReturnType() {
				return returnType;
			}
		}); 
	}
	
}
