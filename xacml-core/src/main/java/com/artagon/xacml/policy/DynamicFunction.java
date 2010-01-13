package com.artagon.xacml.policy;

import java.util.List;

public interface DynamicFunction extends Function
{
	ValueType resolveReturnType(List<Expression> arguments);
}
