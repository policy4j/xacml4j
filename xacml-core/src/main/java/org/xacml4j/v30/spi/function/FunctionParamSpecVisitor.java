package org.xacml4j.v30.spi.function;

public interface FunctionParamSpecVisitor 
{
	void visit(FunctionParamAnyBagSpec spec);
	void visit(FunctionParamAnyAttributeSpec spec);
	void visit(FunctionParamValueTypeSequenceSpec spec);
	void visit(FunctionParamValueTypeSpec spec);
	void visit(FunctionParamFuncReferenceSpec spec);
}
