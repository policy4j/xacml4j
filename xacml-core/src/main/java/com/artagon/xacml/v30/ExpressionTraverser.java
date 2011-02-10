package com.artagon.xacml.v30;

public interface ExpressionTraverser 
{
	void traverse(AttributeValue n, ExpressionVisitor v);
	void traverse(BagOfAttributeValues n, ExpressionVisitor v);
	void traverse(Apply n, ExpressionVisitor v);
	void traverse(FunctionReference n, ExpressionVisitor v);
	void traverse(AttributeDesignator n, ExpressionVisitor v);
	void traverse(AttributeSelector n, ExpressionVisitor v);
	void traverse(VariableReference n, ExpressionVisitor v);
}
